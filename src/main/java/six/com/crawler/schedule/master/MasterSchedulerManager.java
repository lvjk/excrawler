package six.com.crawler.schedule.master;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import six.com.crawler.common.DateFormats;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobRelationship;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotState;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.Node;
import six.com.crawler.node.NodeType;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.DispatchType;
import six.com.crawler.schedule.consts.DownloadContants;
import six.com.crawler.schedule.worker.AbstractWorkerSchedulerManager;
import six.com.crawler.work.CrawlerJobParamKeys;
import six.com.crawler.work.space.WorkSpace;
import six.com.crawler.work.space.WorkSpaceData;

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

	private LinkedBlockingQueue<Job> pendingExecuteQueue = new LinkedBlockingQueue<>();

	private Scheduler scheduler;

	private final static String schedulerGroup = "exCrawler";

	private ExecutorService executor;

	private Interner<String> keyLock = Interners.<String>newWeakInterner();

	protected void doInit() {
		initScheduler();
		// 初始化 读取等待执行任务线程 线程
		int threads = 1;
		executor = Executors.newFixedThreadPool(threads);
		log.info("start Thread{loop-read-pendingExecuteQueue-thread}");
		for (int i = 0; i < threads; i++) {
			executor.execute(() -> {
				loopReadWaitingJob();
			});
		}
		// 加载 当前节点 需要调度的任务
		loadScheduledJob();
	}

	private void loopReadWaitingJob() {
		Job job = null;
		while (true) {
			try {
				job = pendingExecuteQueue.take();
			} catch (InterruptedException e1) {
			}
			// 如果获取到Job的那么 那么execute
			if (null != job) {
				log.info("master node read job[" + job.getName() + "] from queue of pending execute to ready execute");
				doExecute(job);
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
		if (null != job && !pendingExecuteQueue.contains(job)) {
			pendingExecuteQueue.add(job);
		}
	}

	/**
	 * 本地执行 由手动执行和定时触发 调用
	 * 
	 * @param job
	 */
	public void execute(DispatchType dispatchType, String jobName) {
		synchronized (keyLock.intern(jobName)) {
			Job job = getJobDao().query(jobName);
			if (null != job) {
				getScheduleCache().delJob(jobName);
				getScheduleCache().delJobSnapshot(jobName);
				getScheduleCache().delWorkerSnapshots(jobName);

				List<JobParam> jobParams = getJobParamDao().queryJobParams(job.getName());
				job.setParamList(jobParams);

				String id = dispatchType.getCurrentTimeMillis();
				JobSnapshot jobSnapshot = new JobSnapshot();
				jobSnapshot.setId(id);
				jobSnapshot.setName(job.getName());
				jobSnapshot.setDispatchType(dispatchType);
				jobSnapshot.setWorkSpaceName(job.getWorkSpaceName());
				jobSnapshot.setDesignatedNodeName(job.getDesignatedNodeName());
				jobSnapshot.setStatus(JobSnapshotState.WAITING_EXECUTED.value());
				getScheduleCache().updateJobSnapshot(jobSnapshot);
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
		if (!isRunning(job.getName())) {
			synchronized (keyLock.intern(job.getName())) {
				// 判断任务是否在运行
				if (!isRunning(job.getName())) {
					log.info("master node execute job[" + job.getName() + "]");
					List<Node> freeNodes = getFreeNodes(job);
					if (null != freeNodes && freeNodes.size() > 0) {
						doExecute(job, freeNodes);
					} else {
						log.error("there is no node to execute job[" + job.getName() + "]");
					}
				} else {
					log.error("the job[" + job.getName() + "] is running");
				}
			}
		} else {
			log.error("the job[" + job.getName() + "] is running");
		}
	}

	/**
	 * 通知freeNodes 执行job
	 * 
	 * @param job
	 * @param freeNodes
	 */
	private void doExecute(Job job, List<Node> freeNodes) {
		JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(job.getName());
		jobSnapshot.setSaveRawData(job.getParamBoolean(CrawlerJobParamKeys.IS_SAVE_RAW_DATA, false));
		// 任务开始时候 开始时间和结束时间默认是一样的
		jobSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		jobSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		jobSnapshot.setStatus(JobSnapshotState.EXECUTING.value());
		// 缓存将被执行的job,提供给workerSchedule那边使用。
		getScheduleCache().setJob(job);
		// 更新将被执行的job's jobSnapshot
		getScheduleCache().updateJobSnapshot(jobSnapshot);
		// 保存将被执行的job's jobSnapshot
		getJobSnapshotDao().save(jobSnapshot);
		AbstractWorkerSchedulerManager workerSchedulerManager = null;
		for (Node freeNode : freeNodes) {
			try {
				workerSchedulerManager = getNodeManager().loolup(freeNode, AbstractWorkerSchedulerManager.class,
						result -> {
							List<WorkerSnapshot> workers = getScheduleCache().getWorkerSnapshots(job.getName());
							if (null != workers && workers.size() > 0) {
								doJobRelationship(jobSnapshot, JobRelationship.TRIGGER_TYPE_PARALLEL);
							}
						});
				workerSchedulerManager.execute(DispatchType.newDispatchTypeByMaster(), job.getName());
				log.info("already request worker node[" + freeNode.getName() + "] to execut the job[" + job.getName()
						+ "]");
			} catch (Exception e) {
				log.error("this master node calls worker node[" + freeNode.getName() + "] to execut the job["
						+ job.getName() + "]", e);
			}
		}
	}

	/**
	 * 获取可执行job的空闲节点
	 * 
	 * @param job
	 * @return
	 */
	private List<Node> getFreeNodes(Job job) {
		List<Node> freeNodes = null;
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
		return freeNodes;
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

	@Override
	public void suspend(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {
									if (isSuspend(jobName)) {
										JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
										jobSnapshot.setStatus(JobSnapshotState.SUSPEND.value());
										getScheduleCache().updateJobSnapshot(jobSnapshot);
									}
								});
						workerSchedulerManager.suspend(DispatchType.newDispatchTypeByMaster(), jobName);
						log.info("already request worker node[" + node.getName() + "] to suspend the job[" + jobName
								+ "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
	}

	@Override
	public void goOn(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && (DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())
				|| DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName()))) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {
									if (isRunning(jobName)) {
										JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
										jobSnapshot.setStatus(JobSnapshotState.EXECUTING.value());
										getScheduleCache().updateJobSnapshot(jobSnapshot);
									}
								});
						workerSchedulerManager.goOn(DispatchType.newDispatchTypeByMaster(), jobName);
						log.info(
								"already request worker node[" + node.getName() + "] to goOn the job[" + jobName + "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
	}

	@Override
	public void stop(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && (DispatchType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())
				|| DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName()))) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.stop(DispatchType.newDispatchTypeByMaster(), jobName);
						log.info(
								"already request worker node[" + node.getName() + "] to stop the job[" + jobName + "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
	}

	@Override
	public void rest(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.rest(DispatchType.newDispatchTypeByMaster(), jobName);
						log.info(
								"already request worker node[" + node.getName() + "] to wait the job[" + jobName + "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
	}

	@Override
	public void finish(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.finish(DispatchType.newDispatchTypeByMaster(), jobName);
						log.info("already request worker node[" + node.getName() + "] to finish the job[" + jobName
								+ "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
	}

	@Override
	public void askEnd(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_WORKER.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				if (isWait(jobName)) {
					log.info("check job[" + jobName + "]'s worker is all wait");
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
					WorkSpace<WorkSpaceData> workSpace = getWorkSpaceManager()
							.newWorkSpace(jobSnapshot.getWorkSpaceName(), WorkSpaceData.class);
					log.info("start to repair workSpace[" + jobSnapshot.getWorkSpaceName() + "]");
					workSpace.repair();
					log.info("end to repair workSpace[" + jobSnapshot.getWorkSpaceName() + "]");
					if (!workSpace.doingIsEmpty()) {
						log.info("check workSpace is not empty after repaired workSpace["
								+ jobSnapshot.getWorkSpaceName() + "]");
						goOn(DispatchType.newDispatchTypeByManual(), jobName);
					} else {
						log.info("check workSpace is empty after repaired workSpace[" + jobSnapshot.getWorkSpaceName()
								+ "]");
						// 判断当前worker's job是被什么类型调度的 1.MANUAL手动触发
						// 2.SCHEDULER调度器触发
						if (DispatchType.DISPATCH_TYPE_MANUAL.equals(jobSnapshot.getDispatchType().getName())
								|| DispatchType.DISPATCH_TYPE_SCHEDULER
										.equals(jobSnapshot.getDispatchType().getName())) {
							finish(DispatchType.newDispatchTypeByMaster(), jobName);
						} else {
							// 通过当job的触发获取它触发的它的job快照
							JobSnapshot lastJobSnapshot = getScheduleCache()
									.getJobSnapshot(jobSnapshot.getDispatchType().getName());
							// 如果触发的它的job快照==null,那么触发的它的job已经停止运行
							if (null == lastJobSnapshot) {
								// 从历史记录中获取触发它的 JobSnapshot
								lastJobSnapshot = getJobSnapshotDao().query(
										jobSnapshot.getDispatchType().getCurrentTimeMillis(),
										jobSnapshot.getDispatchType().getName());
								// 如果没获取到历史记录那么，我们将stop.然后打印日志非法被执行
								if (null == lastJobSnapshot) {
									stop(DispatchType.newDispatchTypeByManual(), jobName);
									log.error("the job[" + jobName + "]'s jobSnapshot[" + jobSnapshot.getId()
											+ "] is illegal execution");
								} else {
									// 如果触发它的jobSnapshot状态等于finishedstop
									// 时， 当前状态保持一致
									if (JobSnapshotState.FINISHED == lastJobSnapshot.getEnumStatus()) {
										finish(DispatchType.newDispatchTypeByMaster(), jobName);
									} else if (JobSnapshotState.STOP == lastJobSnapshot.getEnumStatus()) {
										stop(DispatchType.newDispatchTypeByManual(), jobName);
									}
									// 如果触发它的jobSnapshot状态等于EXECUTING时，那么触发它的job没有被正常stop,但是当前状态应该设置为stop
									else if (JobSnapshotState.EXECUTING == lastJobSnapshot.getEnumStatus()) {
										stop(DispatchType.newDispatchTypeByManual(), jobName);
									}
								}
							} else {
								// 如果触发它的jobSnapshot状态等于EXECUTING或者SUSPEND
								// 时，那么应该休眠1000毫秒，否则保持跟触发它的jobSnapshot状态一样
								if (JobSnapshotState.EXECUTING == lastJobSnapshot.getEnumStatus()
										|| JobSnapshotState.SUSPEND == lastJobSnapshot.getEnumStatus()) {
									rest(DispatchType.newDispatchTypeByMaster(), jobName);
								} else if (JobSnapshotState.FINISHED == lastJobSnapshot.getEnumStatus()) {
									finish(DispatchType.newDispatchTypeByMaster(), jobName);
								} else if (JobSnapshotState.STOP == lastJobSnapshot.getEnumStatus()) {
									stop(DispatchType.newDispatchTypeByMaster(), jobName);
								}
							}
						}

					}
				} else {
					log.info("check job[" + jobName + "]'s worker is not all wait");
				}
			}
		}
	}

	@Override
	public void endWorker(DispatchType dispatchType, String jobName) {
		if (null != dispatchType && DispatchType.DISPATCH_TYPE_WORKER.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				boolean isFinish = isFinish(jobName);
				boolean isStop = isStop(jobName);
				if (isFinish || isStop) {
					JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
					if (null != jobSnapshot) {
						JobSnapshotState state = null;
						if (isFinish) {
							state = JobSnapshotState.FINISHED;
						} else {
							state = JobSnapshotState.STOP;
						}
						jobSnapshot.setStatus(state.value());
						jobSnapshot.setEndTime(DateFormatUtils.format(new Date(), DateFormats.DATE_FORMAT_1));

						List<WorkerSnapshot> workerSnapshots = getScheduleCache().getWorkerSnapshots(jobName);
						totalWorkerSnapshot(jobSnapshot, workerSnapshots);

						reportJobSnapshot(jobSnapshot);

						getScheduleCache().delJob(jobName);
						getScheduleCache().delWorkerSnapshots(jobName);
						getScheduleCache().delJobSnapshot(jobName);

						// 当任务正常完成时 判断是否有当前任务是否有下个执行任务，如果有的话那么直接执行
						if (JobSnapshotState.FINISHED == state) {
							// 更新downloadState。
							if (jobSnapshot.isSaveRawData()) {
								getJobSnapshotDao().updateDownloadStatus(jobSnapshot.getVersion(),
										jobSnapshot.getVersion() + 1, jobSnapshot.getId(),
										DownloadContants.DOWN_LOAD_FINISHED);
							}
							doJobRelationship(jobSnapshot, JobRelationship.TRIGGER_TYPE_SERIAL);
						}
					}
				}
			}
		}
	}

	@Override
	public synchronized void stopAll(DispatchType dispatchType) {
		List<JobSnapshot> allJobs = getScheduleCache().getJobSnapshots();
		Node currentNode = getNodeManager().getCurrentNode();
		for (JobSnapshot jobSnapshot : allJobs) {
			Job job = getJobDao().query(jobSnapshot.getName());
			Set<Node> nodes = getWorkerNode(job.getName());
			AbstractWorkerSchedulerManager workerSchedulerManager = null;
			for (Node node : nodes) {
				if (!currentNode.equals(node)) {
					try {
						workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.stop(DispatchType.newDispatchTypeByMaster(), job.getName());
						log.info("Already request worker node[" + node.getName() + "] to stop the job[" + job.getName()
								+ "]");
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
					workerSchedulerManager = getNodeManager().loolup(node, AbstractWorkerSchedulerManager.class,
							result -> {

							});
					workerSchedulerManager.stopAll(DispatchType.newDispatchTypeByMaster());
					log.info("Already request worker node[" + node.getName() + "] to stop all");
				} catch (Exception e) {
					log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
				}
			}
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
			scheduleManager.execute(DispatchType.newDispatchTypeByScheduler(), jobName);
		}
	}

	/**
	 * 向调度器注册job
	 * 
	 * @param job
	 */
	@Override
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

	@Override
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
		stopAll(null);
		getScheduleCache().clear();
	}

	@Override
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
