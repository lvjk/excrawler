package six.com.crawler.schedule.worker;

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

import com.google.common.collect.Sets;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
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

	private Map<String, Map<String, Worker<?>>> localJobWorkersMap = new ConcurrentHashMap<String, Map<String, Worker<?>>>();

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

		getScheduleDispatchTypeIntercept().intercept(dispatchType, Sets.newHashSet(DispatchType.DISPATCH_TYPE_MASTER),
				getOperationJobLockPath(jobName), () -> {
					Job job = getScheduleCache().getJob(jobName);
					if (null != job) {
						log.info("worker node[" + getNodeManager().getCurrentNode().getName() + "] execute job["
								+ job.getName() + "]");
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
							Worker<?> worker = buildJobWorker(job, jobSnapshot);
							localJobWorkersMap
									.computeIfAbsent(worker.getJob().getName(),
											mapKey -> new ConcurrentHashMap<String, Worker<?>>())
									.put(worker.getName(), worker);
							getNodeManager().getCurrentNode().incrAndGetRunningWorkerSize();
							executor.execute(() -> {
								log.info("start execute job[" + job.getName() + "]'s worker[" + worker.getName() + "]");
								boolean initSuccesed = false;
								try {
									worker.init();
									initSuccesed = true;
								} catch (Exception e) {
									log.error("init job[" + jobName + "]'s worker[" + worker.getName() + "] err", e);
								}
								if (initSuccesed) {
									try {
										worker.start();
									} catch (Exception e) {
										log.error("execute worker [" + worker.getName() + "] err", e);
									} finally {
										worker.destroy();
										getNodeManager().getCurrentNode().decrAndGetRunningWorkerSize();
										Map<String, Worker<?>> jobWorkerMap = localJobWorkersMap.get(jobName);
										localJobWorkersMap.get(jobName).remove(worker.getName());
										if (jobWorkerMap.size() == 0) {
											localJobWorkersMap.remove(jobName);
										}
										try {
											AbstractMasterSchedulerManager masterSchedulerManager = getNodeManager()
													.loolup(getNodeManager().getMasterNode(),
															AbstractMasterSchedulerManager.class);
											masterSchedulerManager.endWorker(DispatchType.newDispatchTypeByWorker(),
													jobName);
										} catch (Exception e) {
											log.error("notice master node job[" + jobName + "]'s worker["
													+ worker.getName() + "] is end err", e);
										}
									}
								}
							});
							log.info("the job[" + job.getName() + "] is be executed by worker[" + worker.getName()
									+ "]");
						}
					}
					return null;
				});

	}

	/**
	 * 工作节点被动暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public void suspend(DispatchType dispatchType, String jobName) {
		getScheduleDispatchTypeIntercept().intercept(dispatchType, Sets.newHashSet(DispatchType.DISPATCH_TYPE_MASTER),
				getOperationJobLockPath(jobName), () -> {
					Map<String, Worker<?>> workers = getWorkers(jobName);
					for (Worker<?> worker : workers.values()) {
						worker.suspend();
					}
					return null;
				});
	}

	/**
	 * 工作节点被动继续任务
	 * 
	 * @param job
	 * @return
	 */
	public void goOn(DispatchType dispatchType, String jobName) {
		getScheduleDispatchTypeIntercept().intercept(dispatchType, Sets.newHashSet(DispatchType.DISPATCH_TYPE_MASTER),
				getOperationJobLockPath(jobName), () -> {
					Map<String, Worker<?>> workers = getWorkers(jobName);
					for (Worker<?> worker : workers.values()) {
						worker.goOn();
					}
					return null;
				});
	}

	/**
	 * 工作节点被动停止任务
	 * 
	 * @param job
	 * @return
	 */
	public void stop(DispatchType dispatchType, String jobName) {
		getScheduleDispatchTypeIntercept().intercept(dispatchType, Sets.newHashSet(DispatchType.DISPATCH_TYPE_MASTER),
				getOperationJobLockPath(jobName), () -> {
					Map<String, Worker<?>> workers = getWorkers(jobName);
					for (Worker<?> worker : workers.values()) {
						worker.stop();
					}
					return null;
				});
	}

	public synchronized void stopAll(DispatchType dispatchType) {
		getScheduleDispatchTypeIntercept().intercept(dispatchType, Sets.newHashSet(DispatchType.DISPATCH_TYPE_MASTER),
				getOperationJobLockPath(null), () -> {
					List<Worker<?>> list = getLocalWorkers();
					for (Worker<?> worker : list) {
						worker.stop();
					}
					return null;
				});
	}

	public Map<String, Worker<?>> getWorkers(String jobName) {
		Map<String, Worker<?>> jobsWorkerMap = localJobWorkersMap.get(jobName);
		if (null == jobsWorkerMap) {
			jobsWorkerMap = Collections.emptyMap();
		}
		return jobsWorkerMap;
	}

	public List<Worker<?>> getLocalWorkers() {
		List<Worker<?>> result = new ArrayList<>();
		Collection<Map<String, Worker<?>>> all = localJobWorkersMap.values();
		for (Map<String, Worker<?>> map : all) {
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
	protected Worker<?> buildJobWorker(Job job, JobSnapshot jobSnapshot) {
		String workerClass = job.getWorkerClass();
		Worker<?> newJobWorker = getWorkerPlugsManager().newWorker(workerClass);
		if(null!=newJobWorker){
			String workerName = getScheduleCache().newWorkerNameByJob(job.getName(),
					getNodeManager().getCurrentNode().getName());
			WorkerSnapshot workerSnapshot = new WorkerSnapshot();
			workerSnapshot.setJobSnapshotId(jobSnapshot.getId());
			workerSnapshot.setJobName(job.getName());
			workerSnapshot.setLocalNode(getNodeManager().getCurrentNode().getName());
			workerSnapshot.setName(workerName);
			workerSnapshot.setWorkerErrMsgs(new ArrayList<WorkerErrMsg>());
			newJobWorker.bindWorkerSnapshot(workerSnapshot);
			newJobWorker.bindConfigure(getConfigure());
			newJobWorker.bindManager(this);
			newJobWorker.bindJob(job);
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
