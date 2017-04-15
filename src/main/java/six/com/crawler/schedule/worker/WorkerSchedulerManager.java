package six.com.crawler.schedule.worker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Site;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.schedule.AbstractSchedulerManager;
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
public class WorkerSchedulerManager extends AbstractWorkerSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	private Map<String, Map<String, Worker>> localJobWorkersMap = new ConcurrentHashMap<String, Map<String, Worker>>();

	private ExecutorService executor;

	protected void doInit() {
		executor = Executors.newFixedThreadPool(getNodeManager().getCurrentNode().getRunningWorkerMaxSize(),
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
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			Job job = getJobDao().query(jobName);
			if (null != job) {
				log.info("worker node[" + getNodeManager().getCurrentNode().getName() + "] execute job[" + job.getName()
						+ "]");
				String path = getOperationJobLockPath(jobName);
				DistributedLock distributedLock = getNodeManager().getWriteLock(path);
				try {
					distributedLock.lock();
					List<JobParam> jobParams = getJobParamDao().queryJobParams(job.getName());
					job.setParamList(jobParams);
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(job.getName());
					if (null == jobSnapshot || jobSnapshot.getId() == null) {
						throw new RuntimeException("the job's jobSnapshot is not be init");
					}
					int needThreads = job.getThreads();
					int freeThreads = getNodeManager().getCurrentNode().getFreeWorkerSize();
					int actualThreads = 0;
					if (needThreads <= freeThreads) {
						actualThreads = needThreads;
					} else {
						actualThreads = freeThreads;
					}
					log.info("get " + actualThreads + " worker to execute job[" + job.getName() + "]");
					for (int i = 0; i < actualThreads; i++) {
						Worker worker = buildJobWorker(job, jobSnapshot);
						executor.execute(() -> {
							log.info("start execute job[" + job.getName() + "]'s worker[" + worker.getName() + "]");
							try {
								worker.init();
								startWorker(worker);
								worker.start();
							} catch (Exception e) {
								log.error("execute worker [" + worker.getName() + "] err", e);
							} finally {
								endWorer(worker);
							}
						});
						log.info("the job[" + job.getName() + "] is be executed by worker[" + worker.getName() + "]");
					}
				} finally {
					distributedLock.unLock();
				}
			}
		}
	}

	/**
	 * 工作节点被动暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public void suspend(DispatchType dispatchType,String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			String path = getOperationJobLockPath(jobName);
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				Map<String, Worker> workers = getWorkers(jobName);
				for (Worker worker : workers.values()) {
					worker.suspend();
				}
			} finally {
				distributedLock.unLock();
			}
		}
	}

	/**
	 * 工作节点被动继续任务
	 * 
	 * @param job
	 * @return
	 */
	public void goOn(DispatchType dispatchType,String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			String path = getOperationJobLockPath(jobName);
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				Map<String, Worker> workers = getWorkers(jobName);
				for (Worker worker : workers.values()) {
					worker.goOn();
				}
			} finally {
				distributedLock.unLock();
			}
		}
	}

	/**
	 * 工作节点被动停止任务
	 * 
	 * @param job
	 * @return
	 */
	public void stop(DispatchType dispatchType,String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			String path = getOperationJobLockPath(jobName);
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				Map<String, Worker> workers = getWorkers(jobName);
				for (Worker worker : workers.values()) {
					worker.stop();
				}
			} finally {
				distributedLock.unLock();
			}
		}
	}
	
	public synchronized void stopAll(DispatchType dispatchType) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			// 然后获取当前节点有关的job worker 然后调用stop
			List<Worker> list = getLocalWorkers();
			for (Worker worker : list) {
				worker.stop();
			}
		}
	}

	private void startWorker(Worker worker) {
		String jobName = worker.getJob().getName();
		String workerName = worker.getName();
		WorkerSnapshot workerSnapshot = worker.getWorkerSnapshot();
		localJobWorkersMap.computeIfAbsent(worker.getJob().getName(), mapKey -> new ConcurrentHashMap<String, Worker>())
				.put(worker.getName(), worker);
		getScheduleCache().updateWorkerSnapshot(workerSnapshot);
		getNodeManager().getCurrentNode().incrAndGetRunningWorkerSize();
		try {
			AbstractMasterSchedulerManager masterSchedulerManager = getNodeManager()
					.loolup(getNodeManager().getMasterNode(), AbstractMasterSchedulerManager.class);
			masterSchedulerManager.startWorker(jobName, workerName);
		} catch (Exception e) {
			log.error("notice master node job[" + jobName + "]'s worker[" + workerName + "] is started err", e);
		}

	}

	/**
	 * worker结束时调用
	 * 
	 * @param worker
	 * @param jobName
	 */
	public void endWorer(Worker worker) {
		String jobName = worker.getJob().getName();
		String workerName = worker.getName();
		worker.destroy();
		Map<String, Worker> jobWorkerMap = localJobWorkersMap.get(jobName);
		jobWorkerMap.remove(workerName);
		if (jobWorkerMap.size() == 0) {
			localJobWorkersMap.remove(jobName);
		}
		getNodeManager().getCurrentNode().decrAndGetRunningWorkerSize();
		try {
			AbstractMasterSchedulerManager masterSchedulerManager = getNodeManager()
					.loolup(getNodeManager().getMasterNode(), AbstractMasterSchedulerManager.class);
			masterSchedulerManager.endWorker(jobName, workerName);
		} catch (Exception e) {
			log.error("notice master node job[" + jobName + "]'s worker[" + workerName + "] is end err", e);
		}
	}

	public Map<String, Worker> getWorkers(String jobName) {
		Map<String, Worker> jobsWorkerMap = localJobWorkersMap.get(jobName);
		if (null == jobsWorkerMap) {
			jobsWorkerMap = Collections.emptyMap();
		}
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
					workerSnapshot.setLocalNode(getNodeManager().getCurrentNode().getName());
					workerSnapshot.setName(workerName);
					workerSnapshot.setWorkerErrMsgs(new ArrayList<WorkerErrMsg>());
					newJobWorker.bindWorkerSnapshot(workerSnapshot);
					newJobWorker.bindManager(this);
					newJobWorker.bindJobSnapshot(jobSnapshot);
					newJobWorker.bindJob(job);
					getScheduleCache().setWorkerSnapshot(workerSnapshot);
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
			} else {
				log.error("did not find worker's constructor:" + workerClass);
				throw new RuntimeException("did not find worker class:" + workerClass);
			}
		} else {
			log.error("did not find worker class:" + workerClass);
			throw new RuntimeException("did not find worker class:" + workerClass);
		}
		return newJobWorker;
	}

	public void shutdown() {
		// 然后获取当前节点有关的job worker 然后调用stop
		stopAll(DispatchType.newDispatchTypeByMaster());
		// 然后shut down worker线程池
		executor.shutdown();
	}

}
