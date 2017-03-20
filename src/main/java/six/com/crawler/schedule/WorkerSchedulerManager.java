package six.com.crawler.schedule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.entity.WorkerErrMsg;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.work.Worker;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:33:37
 */
@Component
public class WorkerSchedulerManager extends AbstractSchedulerManager implements Remote {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	private final static int SAVE_ERR_MSG_MAX = 20;

	private Map<String, Map<String, Worker>> localJobWorkersMap = new ConcurrentHashMap<String, Map<String, Worker>>();

	private AtomicInteger runningWroker = new AtomicInteger(0);

	private int workerRunningMaxSize;

	private ExecutorService executor;
	
	@Autowired
	private MasterSchedulerManagerClient masterSchedulerManagerClient;

	public MasterSchedulerManagerClient getMasterSchedulerManagerClient() {
		return masterSchedulerManagerClient;
	}

	public void setMasterSchedulerManagerClient(MasterSchedulerManagerClient masterSchedulerManagerClient) {
		this.masterSchedulerManagerClient = masterSchedulerManagerClient;
	}

	protected void init() {
		workerRunningMaxSize = getClusterManager().getCurrentNode().getRunningJobMaxSize();
		executor = Executors.newFixedThreadPool(workerRunningMaxSize, new JobWorkerThreadFactory());
		// 初始化 读取等待执行任务线程 线程
	}

	/**
	 * 执行 worker
	 * 
	 * @param crawlerWorker
	 */
	private void executeWorker(Worker worker) {
		String jobName = worker.getJob().getName();
		String workerName = worker.getJob().getName();
		try {
			worker.init();
			startWorker(jobName, workerName, worker);
			worker.start();
		} catch (Exception e) {
			log.error("execute jobWorker {" + worker.getName() + "} err", e);
		} finally {
			endWorer(worker.getJob().getName(), worker.getName());
		}

	}

	/**
	 * 由内部守护线程循环读取等待被执行队列worker 执行此 调度执行job
	 * 
	 * 方法需要加分布式锁保证job的每个worker都是顺序被执行
	 * 
	 * @param job
	 */
	public synchronized void execute(Job job) {
		String key = "workerSchedulerManager_doExecute_" + job.getName();
		getRedisManager().lock(key);
		try {
			log.info("worker node[" + getClusterManager().getCurrentNode().getName() + "] execute job[" + job.getName()
					+ "]");
			log.info("buiild job[" + job.getName() + "] worker");
			List<JobParam> jobParams = getJobService().queryJobParams(job.getName());
			job.setParamList(jobParams);
			JobSnapshot jobSnapshot = getJobSnapshot(job.getName());
			if (null == jobSnapshot || jobSnapshot.getId() == null) {
				throw new RuntimeException("the job's jobSnapshot is not be init");
			}
			Worker worker = buildJobWorker(job, jobSnapshot);
			executor.execute(() -> {
				log.info("start execute job[" + job.getName() + "]'s worker[" + worker.getName() + "]");
				executeWorker(worker);
			});
			// 等待worker.getState() == WorkerLifecycleState.STARTED 时返回true
			log.info("waiting job[" + job.getName() + "]'s worker to started");
			while (worker.getState() == WorkerLifecycleState.READY) {
			}
			log.info("the job[" + job.getName() + "] is be executed by worker[" + worker.getName() + "]");
		} finally {
			getRedisManager().unlock(key);
		}
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
		List<Worker> list = getLocalWorkers();
		for (Worker worker : list) {
			worker.stop();
		}
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

	private void startWorker(String jobName, String workName, Worker worker) {
		WorkerSnapshot workerSnapshot = worker.getWorkerSnapshot();
		String workerSnapshotsKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		try {
			getRedisManager().lock(workerSnapshotsKey);
			localJobWorkersMap
					.computeIfAbsent(worker.getJob().getName(), mapKey -> new ConcurrentHashMap<String, Worker>())
					.put(worker.getName(), worker);
			updateWorkerSnapshot(workerSnapshot);
			getClusterManager().getCurrentNode().setRunningJobSize(runningWroker.incrementAndGet());
			masterSchedulerManagerClient.startWorker(jobName);
		} finally {
			getRedisManager().unlock(workerSnapshotsKey);
		}
	}

	/**
	 * worker结束时调用
	 * 
	 * @param worker
	 * @param jobName
	 */
	public void endWorer(String jobName, String workerName) {
		String workerSnapshotsKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		try {
			getRedisManager().lock(workerSnapshotsKey);
			getClusterManager().getCurrentNode().setRunningJobSize(runningWroker.decrementAndGet());
			Map<String, Worker> jobWorkerMap = localJobWorkersMap.get(jobName);
			if (null != jobWorkerMap) {
				jobWorkerMap.remove(workerName);
				if (jobWorkerMap.size() == 0) {
					localJobWorkersMap.remove(jobName);
				}
			}
			masterSchedulerManagerClient.endWorker(jobName);
		} finally {
			getRedisManager().unlock(workerSnapshotsKey);
		}
	}


	public Map<String, Worker> getWorkers(String jobName) {
		Map<String, Worker> jobsWorkerMap = localJobWorkersMap.get(jobName);
		return jobsWorkerMap;
	}


	public List<Worker> getLocalWorkers() {
		List<Worker> result = new ArrayList<>();
		Collection<Map<String, Worker>> all = localJobWorkersMap.values();
		for (Map<String, Worker> map : all) {
			result.addAll(map.values());
		}
		return result;
	}

	/**
	 * 通知master 调度中心 worker运行结束 任务
	 * 
	 * @param worker
	 * @param jobName
	 */

	// 构建 job worker
	protected Worker buildJobWorker(Job job, JobSnapshot jobSnapshot) {
		Worker newJobWorker = null;
		String workerClass = job.getWorkerClass();
		// 判断是否是htmljob 如果是那么调用 htmlJobWorkerBuilder 构建worker
		Class<?> clz = null;
		Constructor<?> constructor = null;
		try {
			clz = Class.forName(workerClass);
		} catch (ClassNotFoundException e) {
			log.error("ClassNotFoundException  err:" + workerClass, e);
		}
		if (null != clz) {
			try {
				constructor = clz.getConstructor();
			} catch (NoSuchMethodException e) {
				log.error("NoSuchMethodException getConstructor err:" + clz, e);
			} catch (SecurityException e) {
				log.error("SecurityException err" + clz, e);
			}
			if (null != constructor) {
				try {
					newJobWorker = (Worker) constructor.newInstance();
					String workerName = getWorkerNameByJob(job);
					WorkerSnapshot workerSnapshot = new WorkerSnapshot();
					workerSnapshot.setJobSnapshotId(jobSnapshot.getId());
					workerSnapshot.setJobName(job.getName());
					workerSnapshot.setLocalNode(getClusterManager().getCurrentNode().getName());
					workerSnapshot.setName(workerName);
					workerSnapshot.setWorkerErrMsgs(new ArrayList<WorkerErrMsg>());

					newJobWorker.bindWorkerSnapshot(workerSnapshot);
					newJobWorker.bindManager(this);
					newJobWorker.bindJobSnapshot(jobSnapshot);
					newJobWorker.bindJob(job);

				} catch (InstantiationException e) {
					log.error("InstantiationException  err:" + workerClass, e);
				} catch (IllegalAccessException e) {
					log.error("IllegalAccessException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				} catch (IllegalArgumentException e) {
					log.error("IllegalArgumentException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				} catch (InvocationTargetException e) {
					log.error("InvocationTargetException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				}
			}
		}
		return newJobWorker;
	}

	public void updateWorkSnapshot(WorkerSnapshot workerSnapshot, boolean isSaveErrMsg) {
		List<WorkerErrMsg> errMsgs = workerSnapshot.getWorkerErrMsgs();
		if (null != errMsgs && ((isSaveErrMsg && errMsgs.size() > 0) || errMsgs.size() >= SAVE_ERR_MSG_MAX)) {
			getWorkerErrMsgService().batchSave(errMsgs);
			errMsgs.clear();
		}
		updateWorkerSnapshot(workerSnapshot);
	}
}
