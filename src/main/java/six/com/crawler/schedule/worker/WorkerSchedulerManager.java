package six.com.crawler.schedule.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.Node;
import six.com.crawler.node.NodeType;
import six.com.crawler.schedule.DispatchType;
import six.com.crawler.schedule.JobWorkerThreadFactory;
import six.com.crawler.schedule.master.AbstractMasterSchedulerManager;
import six.com.crawler.schedule.master.MasterSchedulerManager;
import six.com.crawler.work.Worker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:33:37
 */
@Component
public class WorkerSchedulerManager extends AbstractWorkerSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	private Map<String, Map<String, Worker<?>>> localJobWorkersMap = new ConcurrentHashMap<String, Map<String, Worker<?>>>();

	private ExecutorService executor;

	private Interner<String> keyLock = Interners.<String>newWeakInterner();

	protected void doInit() {
		executor = Executors.newFixedThreadPool(getClusterManager().getCurrentNode().getRunningWorkerMaxSize(),
				new JobWorkerThreadFactory());
	}

	/**
	 * 由内部守护线程循环读取等待被执行队列worker 执行此 调度执行job
	 * 
	 * 方法需要加分布式锁保证job的每个worker都是顺序被执行
	 * 
	 * @param job
	 */
	public void execute(DispatchType dispatchType, String jobName) {
		Node currentNode = getClusterManager().getCurrentNode();
		if (NodeType.SINGLE == currentNode.getType() || NodeType.WORKER == currentNode.getType()) {
			if (null != dispatchType && StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
					&& StringUtils.isNotBlank(jobName)) {
				synchronized (keyLock.intern(jobName)) {
					Job job = getScheduleCache().getJob(jobName);
					if (null != job) {
						log.info("worker node[" + getClusterManager().getCurrentNode().getName() + "] execute job["
								+ job.getName() + "]");
						JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(job.getName());
						if (null != jobSnapshot && StringUtils.isNotBlank(jobSnapshot.getId())) {
							int needThreads = job.getThreads();
							int freeThreads = getClusterManager().getCurrentNode().getFreeWorkerSize();
							int actualThreads = 0;
							if (needThreads <= freeThreads) {
								actualThreads = needThreads;
							} else {
								actualThreads = freeThreads;
							}
							Worker<?>[] workers = buildJobWorker(job, jobSnapshot, actualThreads);
							log.info("get " + actualThreads + " worker to execute job[" + job.getName() + "]");
							for (Worker<?> worker : workers) {
								executor.execute(() -> {
									doExecute(jobName, worker);
								});
								log.info("the job[" + job.getName() + "] is be executed by worker[" + worker.getName()
										+ "]");
							}
						} else {
							throw new RuntimeException("the job's jobSnapshot is not be init");
						}
					}
				}
			}
		}
	}

	private void doExecute(final String jobName, final Worker<?> worker) {
		final String workerName = worker.getName();
		final Thread workerThread = Thread.currentThread();
		final String systemThreadName = workerThread.getName();
		final String newThreadName = systemThreadName + "-" + workerName;
		workerThread.setName(newThreadName);
		getClusterManager().getCurrentNode().incrAndGetRunningWorkerSize();
		try {
			worker.start();
		} catch (Exception e) {
			log.error("execute worker [" + workerName + "] err", e);
			doErr(e);
		} finally {
			worker.destroy();
			workerThread.setName(systemThreadName);
			getClusterManager().getCurrentNode().decrAndGetRunningWorkerSize();
			try {
				AbstractMasterSchedulerManager masterSchedulerManager = getClusterManager()
						.loolup(getClusterManager().getMasterNodeFromRegister(), AbstractMasterSchedulerManager.class, result -> {
							if(result.isOk()){
								remove(jobName, workerName);
							}
						});
				masterSchedulerManager.endWorker(DispatchType.newDispatchTypeByWorker(), jobName);
			} catch (Exception e) {
				log.error("notice master node job[" + jobName + "]'s worker[" + workerName + "] is end err", e);
			}
		}
		log.info("the job[" + jobName + "] is be executed by worker[" + worker.getName() + "]");
	}

	private void doErr(Exception e) {

	}

	private void remove(String jobName, String workerName) {
		synchronized (keyLock.intern(jobName)) {
			Map<String, Worker<?>> jobWorkerMap = localJobWorkersMap.get(jobName);
			if (null != jobWorkerMap) {
				jobWorkerMap.remove(workerName);
				if (jobWorkerMap.size() == 0) {
					localJobWorkersMap.remove(jobName);
					log.error("the job[" + jobName + "]'s worker[" + workerName
							+ "] end and check jobWorkerMap is empty");
				}
			} else {
				log.error("the job[" + jobName + "]'s worker[" + workerName + "] did not register");
			}
		}
	}

	@Override
	public void suspend(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
				&& StringUtils.isNotBlank(jobName)) {
			synchronized (keyLock.intern(jobName)) {
				Map<String, Worker<?>> workers = getWorkers(jobName);
				for (Worker<?> worker : workers.values()) {
					worker.suspend();
				}
			}
		}
	}

	@Override
	public void rest(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
				&& StringUtils.isNotBlank(jobName)) {
			synchronized (keyLock.intern(jobName)) {
				Map<String, Worker<?>> workers = getWorkers(jobName);
				for (Worker<?> worker : workers.values()) {
					worker.rest();
				}
			}
		}
	}

	/**
	 * 工作节点被动继续任务
	 * 
	 * @param job
	 * @return
	 */
	@Override
	public void goOn(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
				&& StringUtils.isNotBlank(jobName)) {
			synchronized (keyLock.intern(jobName)) {
				Map<String, Worker<?>> workers = getWorkers(jobName);
				for (Worker<?> worker : workers.values()) {
					worker.goOn();
				}
			}
		}
	}

	/**
	 * 工作节点被动停止任务
	 * 
	 * @param job
	 * @return
	 */
	@Override
	public void stop(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
				&& StringUtils.isNotBlank(jobName)) {
			synchronized (keyLock.intern(jobName)) {
				Map<String, Worker<?>> workers = getWorkers(jobName);
				for (Worker<?> worker : workers.values()) {
					worker.stop();
				}
			}
		}
	}

	@Override
	public void finish(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
				&& StringUtils.isNotBlank(jobName)) {
			synchronized (keyLock.intern(jobName)) {
				Map<String, Worker<?>> workers = getWorkers(jobName);
				for (Worker<?> worker : workers.values()) {
					worker.finish();
				}
			}
		}
	}

	@Override
	public void askEnd(String jobName, String workerName) {
		synchronized (keyLock.intern(jobName)) {
			try {
				AbstractMasterSchedulerManager masterSchedulerManager = getClusterManager()
						.loolup(getClusterManager().getMasterNodeFromRegister(), AbstractMasterSchedulerManager.class, result -> {
							if(!result.isOk()){
								stop(DispatchType.newDispatchTypeByMaster(), jobName);
							}
						});
				masterSchedulerManager.askEnd(DispatchType.newDispatchTypeByWorker(), jobName);
			} catch (Exception e) {
				log.error("ask master job[" + jobName + "]'s worker[" + workerName + "] is end", e);
			}
		}
	}

	@Override
	public synchronized void stopAll(DispatchType dispatchType) {
		if (null != dispatchType && (StringUtils.equals(DispatchType.DISPATCH_TYPE_MASTER, dispatchType.getName())
				|| StringUtils.equals(DispatchType.DISPATCH_TYPE_WORKER, dispatchType.getName()))) {
			synchronized (localJobWorkersMap) {
				for (Map<String, Worker<?>> jobWorkerMap : localJobWorkersMap.values()) {
					for (Worker<?> worker : jobWorkerMap.values()) {
						worker.stop();
					}
				}
			}
		}
	}

	private Map<String, Worker<?>> getWorkers(String jobName) {
		Map<String, Worker<?>> jobsWorkerMap = localJobWorkersMap.get(jobName);
		if (null == jobsWorkerMap) {
			jobsWorkerMap = Collections.emptyMap();
		}
		return jobsWorkerMap;
	}

	/**
	 * 通知master 调度中心 worker运行结束 任务
	 * 
	 * @param worker
	 * @param jobName
	 */

	// 构建 job worker
	protected Worker<?>[] buildJobWorker(Job job, JobSnapshot jobSnapshot, int workerSize) {
		Worker<?>[] workers = new Worker<?>[workerSize];
		String workerClass = job.getWorkerClass();
		Map<String, Worker<?>> jobworkersMap = localJobWorkersMap.get(job.getName());
		if (null == jobworkersMap) {
			jobworkersMap = new ConcurrentHashMap<String, Worker<?>>();
			for (int i = 0; i < workerSize; i++) {
				Worker<?> newJobWorker = getWorkerPlugsManager().newWorker(workerClass);
				if (null != newJobWorker) {
					String workerName = "job[" + job.getName() + "]_node[" + getClusterManager().getCurrentNode().getName()
							+ "]_worker_" + i;
					WorkerSnapshot workerSnapshot = new WorkerSnapshot();
					workerSnapshot.setJobSnapshotId(jobSnapshot.getId());
					workerSnapshot.setJobName(job.getName());
					workerSnapshot.setLocalNode(getClusterManager().getCurrentNode().getName());
					workerSnapshot.setName(workerName);
					workerSnapshot.setWorkerErrMsgs(new ArrayList<WorkerErrMsg>());
					newJobWorker.bindWorkerSnapshot(workerSnapshot);
					newJobWorker.bindConfigure(getConfigure());
					newJobWorker.bindManager(this);
					newJobWorker.bindJob(job);
					workers[i] = newJobWorker;
					jobworkersMap.put(workerName, newJobWorker);
				}
			}
			localJobWorkersMap.put(job.getName(), jobworkersMap);
		} else {
			throw new RuntimeException("there is historical map of worker for job[" + job.getName() + "]");
		}
		return workers;
	}

	public void shutdown() {
		// 然后获取当前节点有关的job worker 然后调用stop
		stopAll(DispatchType.newDispatchTypeByWorker());
		// 然后shut down worker线程池
		executor.shutdown();
	}

}
