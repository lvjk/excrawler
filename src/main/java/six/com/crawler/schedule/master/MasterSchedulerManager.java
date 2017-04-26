package six.com.crawler.schedule.master;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import six.com.crawler.common.DateFormats;
import six.com.crawler.constants.JobConTextConstants;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobRelationship;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotState;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.Node;
import six.com.crawler.node.NodeType;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.DispatchType;
import six.com.crawler.schedule.cache.RedisCacheKeyUtils;
import six.com.crawler.schedule.consts.DownloadContants;
import six.com.crawler.schedule.worker.AbstractWorkerSchedulerManager;
import six.com.crawler.utils.JobTableUtils;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月18日 下午10:32:28 类说明
 *          <p>
 *          警告:所有对job的操作 需要执行分布式锁。保证所有此Job 的操作 顺序执行
 *          </p>
 *          <p>
 *          执行一个爬虫工作流程 :
 *          </p>
 *          <p>
 *          1.提交爬虫job至待执行队列,并将job快照注册至注册中心
 *          </p>
 *          <p>
 *          2.读取待执行任务队列线程读取队列中的job,判断job是否处于运行中，如果运行中重新返回队列，否则做初始化，呼叫执行爬虫任务线程池执行任务
 *          </p>
 *          <p>
 *          3.执行爬虫任务线程池执行爬虫任务
 *          </p>
 *          <p>
 *          4.执行完任务
 *          </p>
 *          <p>
 *          注意:集群 命令调用还需完善
 *          </p>
 */
public class MasterSchedulerManager extends AbstractMasterSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	public static final String JOB_NAME_KEY = "jobName";

	public static final String SCHEDULER_MANAGER_KEY = "scheduleManager";

	private ConcurrentLinkedQueue<Job> pendingExecuteQueue = new ConcurrentLinkedQueue<>();

	private final static Lock waitQueueLock = new ReentrantLock();

	private final static Condition waitQueueCondition = waitQueueLock.newCondition();

	private Map<String, Set<String>> jobRunningWorkerNames = new ConcurrentHashMap<>();

	private Scheduler scheduler;

	private final static String schedulerGroup = "exCrawler";

	private ExecutorService executor;

	protected void doInit() {
		initScheduler();
		// 初始化 读取等待执行任务线程 线程
		int threads = 10;
		executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.execute(() -> {
				loopReadWaitingJob();
			});
		}
		// 加载 当前节点 需要调度的任务
		loadScheduledJob();
	}

	private void loopReadWaitingJob() {
		log.info("start Thread{loop-read-pendingExecuteQueue-thread}");
		Job job = null;
		while (true) {
			job = pendingExecuteQueue.poll();
			// 如果获取到Job的那么 那么execute
			if (null != job) {
				log.info("master node read job[" + job.getName() + "] from queue of pending execute to ready execute");
				doExecute(job);
			} else {// 如果队列里没有Job的话那么 wait 1000 毫秒
				waitQueueLock.lock();
				try {
					try {
						waitQueueCondition.await();
					} catch (InterruptedException e) {
						log.error("queue of pending execute await err", e);
					}
				} finally {
					waitQueueLock.unlock();
				}
			}
		}

	}

	private void initScheduler() {
		Properties props = new Properties();
		props.put("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler");
		props.put("org.quartz.scheduler.rmi.export", false);
		props.put("org.quartz.scheduler.rmi.proxy", false);
		props.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", false);
		props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		props.put("org.quartz.threadPool.threadCount", "1");
		props.put("org.quartz.threadPool.threadPriority", "5");
		props.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", true);
		props.put("org.quartz.jobStore.misfireThreshold", "60000");
		try {
			StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(props);
			scheduler = stdSchedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			log.error("start scheduler err");
			System.exit(1);
		}
	}

	/**
	 * 加载需要调度的job JobChain
	 */
	private void loadScheduledJob() {
		if (NodeType.MASTER == getConfigure().getNodeType() || NodeType.MASTER_WORKER == getConfigure().getNodeType()) {
			log.info("start load scheduled job");
			List<Job> jobs = getJobDao().queryIsScheduled();
			int size = null != jobs ? jobs.size() : 0;
			log.info("load Scheduled job size:" + size);
			for (Job job : jobs) {
				scheduled(job);
			}
		}
	}

	/**
	 * 提交job至等待队列 job 只能提交 job 所属的节点 等待队列 ，由所属节点负责调度触发
	 * 
	 * @param job
	 */
	private void submitWaitQueue(Job job) {
		if (null != job) {
			waitQueueLock.lock();
			try {
				// 如果队里里面没有这个job 才会继续下一步操作
				if (!pendingExecuteQueue.contains(job)) {
					pendingExecuteQueue.add(job);
					waitQueueCondition.signalAll();
				}
			} finally {
				waitQueueLock.unlock();
			}
		}
	}

	/**
	 * 本地执行 由手动执行和定时触发 调用
	 * 
	 * @param job
	 */
	public void execute(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && StringUtils.isNotBlank(jobName)) {
			Job job = getJobDao().query(jobName);
			List<JobParam> params=getJobParamDao().queryJobParams(jobName);
			boolean isSaveRawData=false;
			for (JobParam param:params) {
				if(param.getName().equals("isSaveRawData")){
					isSaveRawData=Integer.parseInt(param.getValue())==1;
					break;
				}
			}
			
			if (null != job) {
				String id = dispatchType.getCurrentTimeMillis();
				JobSnapshot jobSnapshot = new JobSnapshot();
				jobSnapshot.setId(id);
				jobSnapshot.setName(job.getName());
				jobSnapshot.setDispatchType(dispatchType);
				jobSnapshot.setWorkSpaceName(job.getWorkSpaceName());
				jobSnapshot.setDesignatedNodeName(job.getDesignatedNodeName());
				jobSnapshot.setStatus(JobSnapshotState.WAITING_EXECUTED.value());
				jobSnapshot.setSaveRawData(isSaveRawData);
				getScheduleCache().setJobSnapshot(jobSnapshot);
				submitWaitQueue(job);
				log.info("already submit job[" + jobName + "] to queue and it[" + id + "] will to be executed");
			} else {
				log.info("ready to execute job[" + jobName + "] is null");
			}
		}
	}

	/**
	 * call工作节点执行任务
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	private void doExecute(Job job) {
		int callSucceedCount = 0;
		// 判断任务是否在运行
		if (!isRunning(job.getName())) {
			log.info("master node execute job[" + job.getName() + "]");
			List<Node> freeNodes = null;
			String path = getOperationJobLockPath(job.getName());
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				// 先查看任务是否有指定节点名执行
				String designatedNodeName = job.getDesignatedNodeName();
				if (StringUtils.isNotBlank(designatedNodeName)) {
					Node designatedNode = getNodeManager().getWorkerNode(designatedNodeName);
					freeNodes = Arrays.asList(designatedNode);
					log.info("get designated node[" + designatedNodeName + "] to execute job[" + job.getName() + "]");
				} else {
					int needFreeNodeSize = job.getNeedNodes();
					freeNodes = getNodeManager().getFreeWorkerNodes(needFreeNodeSize);
					// 需要运行节点数量减去本地运行节点1
					log.info("get many nodes[" + freeNodes.size() + "] to execute job[" + job.getName() + "]");
				}
				if (null != freeNodes && freeNodes.size() > 0) {
					List<JobParam> jobParams = getJobParamDao().queryJobParams(job.getName());
					job.setParamList(jobParams);
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(job.getName());
					String fixedTableName = job.getParam(JobConTextConstants.FIXED_TABLE_NAME);
					String isSnapshotTable = job.getParam(JobConTextConstants.IS_SNAPSHOT_TABLE);
					String tempTbaleName = null;
					if ("1".equals(isSnapshotTable)) {
						JobSnapshot lastJobSnapshot = getJobSnapshotDao().queryLast(job.getName());
						if (null != lastJobSnapshot && StringUtils.isNotBlank(lastJobSnapshot.getTableName())
								&& lastJobSnapshot.getEnumStatus() != JobSnapshotState.FINISHED) {
							tempTbaleName = lastJobSnapshot.getTableName();
						} else {
							tempTbaleName = JobTableUtils.buildJobTableName(fixedTableName, jobSnapshot.getId());
						}
					} else {
						tempTbaleName = fixedTableName;
					}
					// 任务开始时候 开始时间和结束时间默认是一样的
					jobSnapshot.setStartTime(
							DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
					jobSnapshot
							.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
					jobSnapshot.setTableName(tempTbaleName);
					jobSnapshot.setStatus(JobSnapshotState.EXECUTING.value());
					// 将jobSnapshot更新缓存 这里一定要 saveJobSnapshot
					getScheduleCache().setJob(job);
					getScheduleCache().updateJobSnapshot(jobSnapshot);
					getJobSnapshotDao().save(jobSnapshot);
					AbstractWorkerSchedulerManager workerSchedulerManager = null;
					for (Node freeNode : freeNodes) {
						try {
							workerSchedulerManager = getNodeManager().loolup(freeNode,
									AbstractWorkerSchedulerManager.class);
							workerSchedulerManager.execute(DispatchType.newDispatchTypeByMaster(), job.getName());
							callSucceedCount++;
							log.info("already request worker node[" + freeNode.getName() + "] to execut the job["
									+ job.getName() + "]");
						} catch (Exception e) {
							log.error("call worker node[" + freeNode.getName() + "] to execut the job[" + job.getName()
									+ "] err", e);
						}
					}
					// 如果job成功被调度执行后查询job的 关系列表
					if (callSucceedCount > 0) {
						doJobRelationship(jobSnapshot, JobRelationship.TRIGGER_TYPE_PARALLEL);
					} else {
						List<WorkerSnapshot> workerSnapshots = getScheduleCache().getWorkerSnapshots(job.getName());
						if (null == workerSnapshots || workerSnapshots.isEmpty()) {
							getScheduleCache().delJob(job.getName());
							getScheduleCache().delJobSnapshot(job.getName());
						}
						// 如果请求worker节点执行job技术==0的话，那么stop job避免有漏跑的
						stop(DispatchType.newDispatchTypeByManual(), job.getName());
					}
				} else {
					getScheduleCache().delJobSnapshot(job.getName());
					log.error("there is no node to execute job[" + job.getName() + "]");
				}
			} catch (Exception e) {
				log.error("master node execute job[" + job.getName() + "] err", e);
			} finally {
				distributedLock.unLock();
			}
		} else {
			getScheduleCache().delJobSnapshot(job.getName());
			log.error("the job[" + job.getName() + "] is running");
		}
	}

	private void doJobRelationship(JobSnapshot jobSnapshot, int triggerType) {
		List<JobRelationship> jobRelationships = getJobRelationshipDao().query(jobSnapshot.getName());
		// TODO 这里并发触发的话，需要考虑 是否成功并发执行
		for (JobRelationship jobRelationship : jobRelationships) {
			if (triggerType == jobRelationship.getTriggerType()) {
				execute(DispatchType.newDispatchTypeByJob(jobSnapshot.getName(), jobSnapshot.getId()),
						jobRelationship.getNextJobName());
			}
		}
	}

	/**
	 * call工作节点暂停任务 只有当请求工作节点执行暂停成功数==实际执行任务的工作节点数，才会是完全暂停
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public void suspend(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())) {
			String path = getOperationJobLockPath(jobName);
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				List<Node> nodes = getWorkerNode(jobName);
				int callSuccessedCount = 0;
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class);
						workerSchedulerManager.suspend(DispatchType.newDispatchTypeByMaster(), jobName);
						callSuccessedCount++;
						log.info("Already request worker node[" + node.getName() + "] to suspend the job[" + jobName
								+ "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}

				if (callSuccessedCount == nodes.size()) {
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
					jobSnapshot.setStatus(JobSnapshotState.SUSPEND.value());
					getScheduleCache().updateJobSnapshot(jobSnapshot);
				}
			} finally {
				distributedLock.unLock();
			}
		}
	}

	/**
	 * call工作节点继续任务 只要当请求工作节点继续执行任务的成功数>0，那么就认为它成功
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public void goOn(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())) {
			String path = getOperationJobLockPath(jobName);
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				List<Node> nodes = getWorkerNode(jobName);
				int callSuccessedCount = 0;
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class);
						workerSchedulerManager.goOn(DispatchType.newDispatchTypeByMaster(), jobName);
						callSuccessedCount++;
						log.info(
								"Already request worker node[" + node.getName() + "] to goOn the job[" + jobName + "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
				if (callSuccessedCount > 0) {
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
					jobSnapshot.setStatus(JobSnapshotState.EXECUTING.value());
					getScheduleCache().updateJobSnapshot(jobSnapshot);
				}
			} finally {
				distributedLock.unLock();
			}
		}
	}

	/**
	 * call工作节点继续任务 只有当请求工作节点执行停止成功数==实际执行任务的工作节点数，才会是完全停止
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public void stop(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())) {
			String path = getOperationJobLockPath(jobName);
			DistributedLock distributedLock = getNodeManager().getWriteLock(path);
			try {
				distributedLock.lock();
				List<Node> nodes = getWorkerNode(jobName);
				int callSuccessedCount = 0;
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class);
						workerSchedulerManager.stop(DispatchType.newDispatchTypeByMaster(), jobName);
						callSuccessedCount++;
						log.info(
								"Already request worker node[" + node.getName() + "] to stop the job[" + jobName + "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}

				if (callSuccessedCount == nodes.size()) {
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
					jobSnapshot.setStatus(JobSnapshotState.STOP.value());
					getScheduleCache().updateJobSnapshot(jobSnapshot);
				}
			} finally {
				distributedLock.unLock();
			}
		}
	}

	public synchronized void stopAll(DispatchType dispatchType) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())) {
			List<JobSnapshot> allJobs = getScheduleCache().getJobSnapshots();
			Node currentNode = getNodeManager().getCurrentNode();
			for (JobSnapshot jobSnapshot : allJobs) {
				Job job = getJobDao().query(jobSnapshot.getName());
				List<Node> nodes = getWorkerNode(job.getName());
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					if (!currentNode.equals(node)) {
						try {
							workerSchedulerManager = getNodeManager().loolup(node,
									AbstractWorkerSchedulerManager.class);
							workerSchedulerManager.stop(DispatchType.newDispatchTypeByMaster(), job.getName());
							log.info("Already request worker node[" + node.getName() + "] to stop the job["
									+ job.getName() + "]");
						} catch (Exception e) {
							log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
						}
					}
				}
			}
			List<Node> nodes = getNodeManager().getWorkerNodes();
			AbstractWorkerSchedulerManager workerSchedulerManager = null;
			for (Node node : nodes) {
				if (!currentNode.equals(node)) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class);
						workerSchedulerManager.stopAll(DispatchType.newDispatchTypeByMaster());
						log.info("Already request worker node[" + node.getName() + "] to stop all");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
	}

	public void startWorker(String jobName, String workerName) {
		String path = getOperationJobLockPath(jobName);
		DistributedLock distributedLock = getNodeManager().getWriteLock(path);
		try {
			distributedLock.lock();
			jobRunningWorkerNames.computeIfAbsent(jobName, mapKey -> new ConcurrentHashSet<>()).add(workerName);
		} finally {
			distributedLock.unLock();
		}
	}

	/**
	 * worker结束时调用此方法计数减1，如果计数==0的话那么导出job运行报告
	 */
	public void endWorker(String jobName, String workerName) {
		String path = getOperationJobLockPath(jobName);
		DistributedLock distributedLock = getNodeManager().getWriteLock(path);
		try {
			distributedLock.lock();
			Set<String> jobWorkerNames = jobRunningWorkerNames.get(jobName);
			if (null != jobWorkerNames) {
				jobWorkerNames.remove(workerName);
				if (0 == jobWorkerNames.size()) {
					jobRunningWorkerNames.remove(jobName);
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
					JobSnapshotState state = null;
					List<WorkerSnapshot> workerSnapshots = getScheduleCache().getWorkerSnapshots(jobName);
					int finishedCount = 0;
					for (WorkerSnapshot workerSnapshot : workerSnapshots) {
						if (workerSnapshot.getState() == WorkerLifecycleState.FINISHED) {
							finishedCount++;
						}
					}
					if (workerSnapshots.size() == finishedCount) {
						state = JobSnapshotState.FINISHED;
						jobSnapshot.setStatus(state.value());
					}
					jobSnapshot.setEndTime(DateFormatUtils.format(new Date(), DateFormats.DATE_FORMAT_1));
					totalWorkerSnapshot(jobSnapshot, workerSnapshots);
					reportJobSnapshot(jobSnapshot);
					getScheduleCache().delJob(jobName);
					getScheduleCache().delWorkerSnapshots(jobName);
					getScheduleCache().delJobSnapshot(jobName);
					// 当任务正常完成时 判断是否有当前任务是否有下个执行任务，如果有的话那么直接执行
					if (JobSnapshotState.FINISHED == state) {
						//更新downloadState。
						if(jobSnapshot.isSaveRawData()){
							getJobSnapshotDao().updateDownloadStatus(jobSnapshot.getVersion(), jobSnapshot.getVersion()+1, jobSnapshot.getId(), DownloadContants.DOWN_LOAD_FINISHED);
						}
						
						doJobRelationship(jobSnapshot, JobRelationship.TRIGGER_TYPE_SERIAL);
					}
				}
			} else {
				log.error("did not find job[" + jobName + "]'s WorkerName set");
			}
		} finally {
			distributedLock.unLock();
		}
	}

	@Transactional
	private void reportJobSnapshot(JobSnapshot jobSnapshot) {
		if (null != jobSnapshot) {
			getJobSnapshotDao().update(jobSnapshot);
			List<WorkerSnapshot> workerSnapshots = jobSnapshot.getWorkerSnapshots();
			if (null != workerSnapshots) {
				getWorkerSnapshotDao().batchSave(workerSnapshots);
				for (WorkerSnapshot workerSnapshot : workerSnapshots) {
					if (null != workerSnapshot.getWorkerErrMsgs() && workerSnapshot.getWorkerErrMsgs().size() > 0) {
						getWorkerErrMsgDao().batchSave(workerSnapshot.getWorkerErrMsgs());
					}

				}
			}
		}

	}

	public static class ScheduledJob implements org.quartz.Job {
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			AbstractSchedulerManager scheduleManager = (AbstractSchedulerManager) context.getJobDetail().getJobDataMap()
					.get(SCHEDULER_MANAGER_KEY);
			String jobName = (String) context.getJobDetail().getJobDataMap().get(JOB_NAME_KEY);
			if (!scheduleManager.isRunning(jobName)) {
				scheduleManager.execute(DispatchType.newDispatchTypeByScheduler(), jobName);
			}
		}
	}

	/**
	 * 向调度器注册job
	 * 
	 * @param job
	 */
	public void scheduled(Job job) {
		if (null != job) {
			synchronized (scheduler) {
				JobKey jobKey = new JobKey(job.getName(), schedulerGroup);
				try {
					boolean existed = scheduler.checkExists(jobKey);
					if (existed) {
						return;
					}
				} catch (SchedulerException e1) {
					log.error("scheduler checkExists{" + job.getName() + "} err", e1);
					return;
				}
				try {
					boolean existed = scheduler.checkExists(jobKey);
					if (existed) {
						return;
					}
				} catch (SchedulerException e1) {
					log.error("scheduler checkExists{" + job.getName() + "} err", e1);
					return;
				}
				if (StringUtils.isNotBlank(job.getCronTrigger())) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(job.getName(), schedulerGroup)
							.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronTrigger())).startNow().build();
					JobBuilder jobBuilder = JobBuilder.newJob(ScheduledJob.class);
					jobBuilder.withIdentity(jobKey);
					JobDataMap newJobDataMap = new JobDataMap();
					newJobDataMap.put(JOB_NAME_KEY, job.getName());
					newJobDataMap.put(SCHEDULER_MANAGER_KEY, this);
					jobBuilder.setJobData(newJobDataMap);
					JobDetail jobDetail = jobBuilder.build();
					try {
						scheduler.scheduleJob(jobDetail, trigger);
					} catch (SchedulerException e) {
						log.error("scheduleJob err:" + job.getName());
					}

				}
			}
		}
	}

	/**
	 * 从调度任务中删除 指定job
	 * 
	 * @param job
	 */
	public void cancelScheduled(String jobName) {
		if (StringUtils.isNotBlank(jobName)) {
			synchronized (scheduler) {
				try {
					JobKey key = new JobKey(jobName, schedulerGroup);
					scheduler.deleteJob(key);
				} catch (SchedulerException e) {
					log.error("deleteJobFromScheduled err", e);
				}
			}
		}
	}

	@Override
	public void repair() {
		String patternKey = RedisCacheKeyUtils.getResetPreKey() + "*";
		Set<String> keys = getRedisManager().keys(patternKey);
		for (String key : keys) {
			getRedisManager().del(key);
		}
	}

	/**
	 * 容器结束时调用此销毁方法
	 */
	public void shutdown() {
		if (null != scheduler) {
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				log.error("scheduler shutdown err");
			}
		}
		if (null != executor) {
			executor.shutdown();
		}
	}
}
