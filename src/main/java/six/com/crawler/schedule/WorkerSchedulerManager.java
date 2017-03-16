package six.com.crawler.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import okhttp3.Request;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.utils.ScheduleUrlUtils;
import six.com.crawler.work.Worker;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:33:37
 */
@Component
public class WorkerSchedulerManager extends WorkerAbstractSchedulerManager {

	final static Logger LOG = LoggerFactory.getLogger(MasterSchedulerManager.class);

	private Map<String, Map<String, Worker>> allJobWorkerMap = new ConcurrentHashMap<String, Map<String, Worker>>();
	// 记录wait Job
	private Queue<Job> waitingRunQueue = new ConcurrentLinkedQueue<>();

	private final static Lock waitQueueLock = new ReentrantLock();

	private final static Condition waitQueueCondition = waitQueueLock.newCondition();

	// runningWroker使用计数器
	private AtomicInteger runningWroker = new AtomicInteger(0);

	private int workerRunningMaxSize;

	private ExecutorService executor;

	private Thread executeWaitQueueThread;

	protected void init() {
		workerRunningMaxSize = getConfigure().getConfig("worker.running.max.size", 20);
		executor = Executors.newFixedThreadPool(workerRunningMaxSize, new JobWorkerThreadFactory());
		// 初始化 读取等待执行任务线程 线程
		executeWaitQueueThread = new Thread(() -> {
			loopReadWaitingJob();
		}, "loop-read-waitingJob-thread");
		executeWaitQueueThread.setDaemon(true);
		// 启动读取等待执行任务线程 线程
		executeWaitQueueThread.start();

	}

	private void loopReadWaitingJob() {
		LOG.info("start Thread{loop-read-waitingJob-thread}");
		Job job = null;
		while (true) {
			job = waitingRunQueue.poll();
			// 如果获取到Job的那么 那么execute
			if (null != job) {
				LOG.info("worker node read job[" + job.getName() + "] from waitingQueue to ready execute");
				doExecute(job);
			} else {// 如果队列里没有Job的话那么 wait 1000 毫秒
				waitQueueLock.lock();
				try {
					try {
						waitQueueCondition.await();
					} catch (InterruptedException e) {
						LOG.error("waitQueueCondition await err", e);
					}
				} finally {
					waitQueueLock.unlock();
				}
			}
		}

	}

	/**
	 * 执行 worker
	 * 
	 * @param crawlerWorker
	 */
	private void executeWorker(Worker worker) {
		addWorker(worker);
		try {
			worker.init();
			worker.start();
		} catch (Exception e) {
			LOG.error("execute jobWorker {" + worker.getName() + "} err", e);
		} finally {
			endJob(worker);
		}

	}

	/**
	 * 提交job至等待队列 job 只能提交 job 所属的节点 等待队列 ，由所属节点负责调度触发
	 * 
	 * @param job
	 */
	private void submitWaitQueue(Job job) {
		if (null == job) {
			throw new NullPointerException();
		}
		waitQueueLock.lock();
		try {
			// 如果队里里面没有这个job 才会继续下一步操作
			if (!waitingRunQueue.contains(job)) {
				waitingRunQueue.add(job);
				waitQueueCondition.signalAll();
			}
		} finally {
			waitQueueLock.unlock();
		}
	}

	/**
	 * 由内部守护线程循环读取等待被执行队列worker 执行此 调度执行job
	 * 
	 * 方法需要加分布式锁保证job的每个worker都是顺序被执行
	 * 
	 * @param job
	 */
	private void doExecute(Job job) {
		String key = "workerSchedulerManager_doExecute_" + job.getName();
		getRedisManager().lock(key);
		try {
			LOG.info("worker node[" + getClusterManager().getCurrentNode().getName() + "] execute job[" + job.getName()
					+ "]");
			LOG.info("buiild job[" + job.getName() + "] worker");
			List<JobParam> jobParams = getJobService().queryJobParams(job.getName());
			job.setParamList(jobParams);
			JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(job.getName());
			if (null == jobSnapshot || jobSnapshot.getId() == null) {
				throw new RuntimeException("the job's jobSnapshot is not be init");
			}
			Worker worker = buildJobWorker(job, jobSnapshot);
			executor.execute(() -> {
				LOG.info("start execute job[" + job.getName() + "]'s worker[" + worker.getName() + "]");
				executeWorker(worker);
			});
			// 等待worker.getState() == WorkerLifecycleState.STARTED 时返回true
			LOG.info("waiting job[" + job.getName() + "]'s worker to started");
			while (worker.getState() == WorkerLifecycleState.READY) {
			}
			LOG.info("the job[" + job.getName() + "] is be executed by worker[" + worker.getName() + "]");
		} finally {
			getRedisManager().unlock(key);
		}
	}

	/**
	 * 工作节点被动执行任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void execute(Job job) {
		submitWaitQueue(job);
	}

	/**
	 * 工作节点被动暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void suspend(Job job) {
		String jobName = job.getName();
		Map<String, Worker> workers = getWorkers(jobName);
		for (Worker worker : workers.values()) {
			worker.suspend();
		}
	}

	/**
	 * 工作节点被动继续任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void goOn(Job job) {
		String jobName = job.getName();
		Map<String, Worker> workers = getWorkers(jobName);
		for (Worker worker : workers.values()) {
			worker.goOn();
		}
	}

	/**
	 * 工作节点被动停止任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void stop(Job job) {
		String jobName = job.getName();
		Map<String, Worker> workers = getWorkers(jobName);
		for (Worker worker : workers.values()) {
			worker.stop();
		}
	}

	public synchronized void stopAll() {
		// 然后获取当前节点有关的job worker 然后调用stop
		List<Worker> list = getAllWorkers();
		for (Worker worker : list) {
			worker.stop();
		}
	}

	public boolean workerIsAllWaited(String jobName) {
		List<WorkerSnapshot> workerSnapshots = getRegisterCenter().getWorkerSnapshots(jobName);
		boolean result = true;
		for (WorkerSnapshot workerSnapshot : workerSnapshots) {
			// 判断其他工人是否还在运行
			if (workerSnapshot.getState() != WorkerLifecycleState.WAITED) {
				result = false;
				break;
			}
		}
		return result;
	}

	public int getRunningWorkerCount() {
		return runningWroker.get();
	}

	/**
	 * 容器结束时调用此销毁方法
	 */
	@PreDestroy
	public void destroy() {
		// 然后获取当前节点有关的job worker 然后调用stop
		stopAll();
		// 然后shut down worker线程池
		executor.shutdown();
	}

	private List<Worker> getAllWorkers() {
		List<Worker> allWorkers = new ArrayList<>();
		for (String jobName : allJobWorkerMap.keySet()) {
			Map<String, Worker> jobWorkers = allJobWorkerMap.get(jobName);
			for (Worker worker : jobWorkers.values()) {
				allWorkers.add(worker);
			}
		}
		return allWorkers;
	}

	private Map<String, Worker> getWorkers(String jobName) {
		Map<String, Worker> jobsWorkerMap = allJobWorkerMap.get(jobName);
		return jobsWorkerMap;
	}

	private void addWorker(Worker worker) {
		getRegisterCenter().registerWorker(worker);
		String jobName = worker.getJob().getName();
		allJobWorkerMap.computeIfAbsent(jobName, mapKey -> new ConcurrentHashMap<String, Worker>())
				.put(worker.getName(), worker);
		runningWroker.incrementAndGet();
		getClusterManager().getCurrentNode().setRunningJobSize(runningWroker.get());
	}

	/**
	 * worker结束时调用
	 * 
	 * @param worker
	 * @param jobName
	 */
	private void endJob(Worker worker) {
		String jobName = worker.getJob().getName();
		allJobWorkerMap.get(jobName).remove(worker.getName());
		getClusterManager().getCurrentNode().setRunningJobSize(runningWroker.decrementAndGet());
		worker.destroy();
		Node masterNode = getClusterManager().getMasterNode();
		String callUrl = ScheduleUrlUtils.getEndJob(masterNode, jobName);
		Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
		getHttpClient().executeRequest(Request);
	}
}
