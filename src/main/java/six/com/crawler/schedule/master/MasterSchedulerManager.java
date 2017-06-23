package six.com.crawler.schedule.master;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import six.com.crawler.common.DateFormats;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobRelationship;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotStatus;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.Node;
import six.com.crawler.node.NodeChangeWatcher;
import six.com.crawler.node.NodeType;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.SchedulerCommand;
import six.com.crawler.schedule.SchedulerCommandGroup;
import six.com.crawler.schedule.TriggerType;
import six.com.crawler.schedule.worker.AbstractWorkerSchedulerManager;
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
@Component
public class MasterSchedulerManager extends AbstractMasterSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	public static final String JOB_NAME_KEY = "jobName";

	public static final String SCHEDULER_MANAGER_KEY = "scheduleManager";

	public static final String CLEAR_BEFORE_DAYS_KEY = "clearBeforeDays";

	private LinkedBlockingQueue<Job> pendingExecuteQueue = new LinkedBlockingQueue<>();

	private final static String systemGroup = "exCrawler";

	private final static String schedulerGroup = "exCrawler";

	private Scheduler scheduler;

	private Thread doJobThread;

	private Interner<String> keyLock = Interners.<String>newWeakInterner();

	private LinkedBlockingQueue<SchedulerCommandGroup> schedulerCommandQueue = new LinkedBlockingQueue<>();

	private Thread doSchedulerCommand;

	protected final void doInit() {
		// 如果当前节点是否master
		if (NodeType.SINGLE == getClusterManager().getCurrentNode().getType()
				|| NodeType.MASTER == getClusterManager().getCurrentNode().getType()) {
			try {
				repair();
			} catch (Exception e) {
				log.error("master node repair err", e);
			}
			initMasterNodeScheduler();
		} else {
			// 注册变为主节点wather
			getClusterManager().registerToMasterNodeWatcher(new NodeChangeWatcher() {
				@Override
				public void onChange(String masterNodeName) {
					if (StringUtils.equals(masterNodeName, getClusterManager().getNodeName())) {
						// 当检测到变更为主节点时那么，应该暂停当前节点上运行的任务，然后加载计划执行任务，
						getWorkerSchedulerManager().stopAll(TriggerType.newDispatchTypeByMaster());
						// 初始化主节点调度中心
						initMasterNodeScheduler();
					}
				}
			});
		}
	}

	private void initMasterNodeScheduler() {
		initDoJobThread();// 初始化 读取等待执行任务线程 线程
		initDoSchedulerCommand();
		initScheduler();// 初始化 时间调度器
		loadSystemJob();// 初始化 加载系统job
		loadScheduledJob();// 初始化加载需要时间调度的job
		// 注册丢失worker节点wather
		getClusterManager().registerMissWorkerNodeWatcher(new NodeChangeWatcher() {
			@Override
			public void onChange(String workerNodeName) {
				// 找出丢失的workerNode
				// 再次检查丢失的workerNode是否存活
				// 清理丢失的workerNode在缓存中的worker运行记录
			}
		});
	}

	private void initDoJobThread() {
		doJobThread = new Thread(() -> {
			log.info("start Thread{loop-read-pendingExecuteQueue-thread}");
			Job job = null;
			while (true) {
				try {
					job = pendingExecuteQueue.take();
					if (null != job) {
						doExecute(job);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}, "loop-read-pendingExecuteQueue-thread");
		doJobThread.setDaemon(true);
		doJobThread.start();
	}

	private void initDoSchedulerCommand() {
		doSchedulerCommand = new Thread(() -> {
			log.info("start Thread{do-scheduler-command-thread}");
			SchedulerCommandGroup commandGroup = null;
			while (true) {
				try {
					commandGroup = schedulerCommandQueue.take();
					if (null != commandGroup) {
						doCommandGroup(commandGroup);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}, "loop-read-pendingExecuteQueue-thread");
		doSchedulerCommand.setDaemon(true);
		doSchedulerCommand.start();
	}

	@Override
	public void submitCommand(SchedulerCommandGroup commandGroup) {
		schedulerCommandQueue.add(commandGroup);
	}

	private void doCommandGroup(SchedulerCommandGroup commandGroup) {
		SchedulerCommand[] commands = commandGroup.getSchedulerCommands();
		if (null != commands) {
			for (int i = 0; i < commands.length;) {
				SchedulerCommand command = commands[i];
				if (doCommand(command, 10000, 3000)) {
					i++;
				} else {
					log.error("execute schedulerCommand failed:" + command.toString());
					break;
				}
			}
		}

	}

	private boolean doCommand(SchedulerCommand command, long timeOut, long interval) {
		boolean reulst = false;
		if (SchedulerCommand.EXECUTE.equals(command.getCommand())) {
			String jobSnapshotId = execute(TriggerType.newDispatchTypeByMaster(), command.getJobName());
			if(StringUtils.isNotBlank(jobSnapshotId)){
				reulst = TimeoutHelper.checkTimeout(() -> {
					List<WorkerSnapshot> workers = getScheduleCache().getWorkerSnapshots(command.getCommand());
					if (null != workers && workers.size() > 0) {
						return true;
					} else {
						JobSnapshot tempJobSnapshot = getJobSnapshotDao().query(jobSnapshotId, command.getJobName());
						if (null != tempJobSnapshot && (tempJobSnapshot.getStatus() == JobSnapshotStatus.STOP.value()
								|| tempJobSnapshot.getStatus() == JobSnapshotStatus.FINISHED.value())) {
							return true;
						} else {
							return false;
						}
					}
				}, timeOut, interval, "check to execute job[" + command.getJobName() + "]");
			}
		} else if (SchedulerCommand.SUSPEND.equals(command.getCommand())) {
			suspend(TriggerType.newDispatchTypeByManual(), command.getJobName());
		} else if (SchedulerCommand.GOON.equals(command.getCommand())) {
			goOn(TriggerType.newDispatchTypeByManual(), command.getJobName());
		} else if (SchedulerCommand.STOP.equals(command.getCommand())) {
			JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(command.getCommand());
			stop(TriggerType.newDispatchTypeByMaster(), command.getJobName());
			reulst = TimeoutHelper.checkTimeout(() -> {
				if (getScheduleCache().getJobSnapshot(command.getJobName()) == null ? true : false) {
					JobSnapshot tempJobSnapshot = getJobSnapshotDao().query(jobSnapshot.getId(), command.getJobName());
					if (null != tempJobSnapshot && tempJobSnapshot.getStatus() == JobSnapshotStatus.STOP.value()) {
						return true;
					}
				}
				return false;
			}, timeOut, interval, "check to stop job[" + command.getJobName() + "]");
		} else if (SchedulerCommand.FINISH.equals(command.getCommand())) {
			JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(command.getCommand());
			finish(TriggerType.newDispatchTypeByMaster(), command.getJobName());
			reulst = TimeoutHelper.checkTimeout(() -> {
				if (getScheduleCache().getJobSnapshot(command.getJobName()) == null ? true : false) {
					JobSnapshot tempJobSnapshot = getJobSnapshotDao().query(jobSnapshot.getId(), command.getJobName());
					if (null != tempJobSnapshot && tempJobSnapshot.getStatus() == JobSnapshotStatus.FINISHED.value()) {
						return true;
					}
				}
				return false;
			}, timeOut, interval, "check to finish job[" + command.getJobName() + "]");
		}
		return reulst;
	}

	private void initScheduler() {
		Properties props = new Properties();
		props.put("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler");
		props.put("org.quartz.scheduler.rmi.export", false);
		props.put("org.quartz.scheduler.rmi.proxy", false);
		props.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", false);
		props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		props.put("org.quartz.threadPool.threadCount", "2");
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
	 * 加载需要时间调度的job
	 */
	private void loadScheduledJob() {
		Node currentNode = getClusterManager().getCurrentNode();
		if (NodeType.SINGLE == currentNode.getType() || NodeType.MASTER == currentNode.getType()) {
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
	 * 加载系统job
	 */
	private void loadSystemJob() {
		String systemJobName = "systemJob";
		String cronTrigger = getConfigure().getConfig("master.systemjob.cronTrigger", "0 0 1 * * ? *");
		int clearBeforeDays = getConfigure().getConfig("master.systemjob.clearBeforeDays", 7);
		JobKey jobKey = new JobKey(systemJobName, systemGroup);
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(systemJobName, systemGroup)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronTrigger)).startNow().build();
		JobBuilder jobBuilder = JobBuilder.newJob(SystemClearJob.class);
		jobBuilder.withIdentity(jobKey);
		JobDataMap newJobDataMap = new JobDataMap();
		newJobDataMap.put(SCHEDULER_MANAGER_KEY, this);
		newJobDataMap.put(CLEAR_BEFORE_DAYS_KEY, clearBeforeDays);
		jobBuilder.setJobData(newJobDataMap);
		JobDetail jobDetail = jobBuilder.build();
		try {
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("load system's job");
		} catch (SchedulerException e) {
			log.error("scheduleJob err:" + systemJobName);
		}
	}

	/**
	 * 本地执行 由手动执行和定时触发 调用
	 * 
	 * @param job
	 */
	public String execute(TriggerType dispatchType, String jobName) {
		Node currentNode = getClusterManager().getCurrentNode();
		String jobSnapshotId = null;
		if (NodeType.SINGLE == currentNode.getType() || NodeType.MASTER == currentNode.getType()) {
			synchronized (keyLock.intern(jobName)) {
				Job job = getJobDao().query(jobName);
				if (null != job && !pendingExecuteQueue.contains(job) && !isRunning(jobName)) {

					getScheduleCache().delJob(jobName);
					getScheduleCache().delJobSnapshot(jobName);
					getScheduleCache().delWorkerSnapshots(jobName);

					List<JobParam> jobParams = getJobParamDao().queryByJob(job.getName());
					job.setParamList(jobParams);

					jobSnapshotId = dispatchType.getCurrentTimeMillis();
					JobSnapshot jobSnapshot = new JobSnapshot();
					jobSnapshot.setId(jobSnapshotId);
					jobSnapshot.setName(job.getName());
					jobSnapshot.setTriggerType(dispatchType);
					jobSnapshot.setWorkSpaceName(job.getWorkSpaceName());
					jobSnapshot.setStatus(JobSnapshotStatus.WAITING_EXECUTED.value());
					getScheduleCache().updateJobSnapshot(jobSnapshot);
					pendingExecuteQueue.add(job);
					log.info("already submit job[" + jobName + "] to queue and it[" + jobSnapshotId
							+ "] will to be executed");
				} else {
					log.info("ready to execute job[" + jobName + "] is null");
				}
			}
		}
		return jobSnapshotId;
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
		// 判断任务是否在运行
		if (!isRunning(job.getName())) {
			log.info("master node execute job[" + job.getName() + "]");
			// TODO 这里计算可执行资源时，需要进行资源隔离，避免并发导致同时分配
			String designatedNodeName = job.getDesignatedNodeName();
			int needNodes = job.getNeedNodes();
			int needThreads = job.getThreads();
			List<Node> freeNodes = getFreeNodes(designatedNodeName, needNodes, needThreads);
			if (null != freeNodes && freeNodes.size() > 0) {
				doExecute(job, freeNodes);
				return;
			} else {
				log.error("there is no node to execute job[" + job.getName() + "]");
			}
		} else {
			log.error("the job[" + job.getName() + "] is running");
		}
		getScheduleCache().delJob(job.getName());
		getScheduleCache().delJobSnapshot(job.getName());
		getScheduleCache().delWorkerSnapshots(job.getName());
	}

	/**
	 * 通知freeNodes 执行job
	 * 
	 * @param job
	 * @param freeNodes
	 */
	private void doExecute(Job job, List<Node> freeNodes) {
		JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(job.getName());
		// 任务开始时候 开始时间和结束时间默认是一样的
		jobSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		jobSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		jobSnapshot.setStatus(JobSnapshotStatus.EXECUTING.value());
		// 缓存将被执行的job,提供给workerSchedule那边使用。
		getScheduleCache().setJob(job);
		// 更新将被执行的job's jobSnapshot
		getScheduleCache().updateJobSnapshot(jobSnapshot);
		// 保存将被执行的job's jobSnapshot
		getJobSnapshotDao().save(jobSnapshot);
		AbstractWorkerSchedulerManager workerSchedulerManager = null;
		for (Node freeNode : freeNodes) {
			try {
				workerSchedulerManager = getClusterManager().loolup(freeNode, AbstractWorkerSchedulerManager.class,
						response -> {
							if (response.isOk()) {
								doJobRelationship(jobSnapshot, JobRelationship.EXECUTE_TYPE_PARALLEL);
							}
						});
				workerSchedulerManager.execute(TriggerType.newDispatchTypeByMaster(), job.getName());
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
	private List<Node> getFreeNodes(String designatedNodeName, int needNodes, int needThreads) {
		List<Node> freeNodes = null;
		if (StringUtils.isNotBlank(designatedNodeName)) {
			Node designatedNode = getClusterManager().getWorkerNode(designatedNodeName);
			freeNodes = Arrays.asList(designatedNode);
		} else {
			freeNodes = getClusterManager().getFreeWorkerNodes(needNodes);
		}
		return freeNodes;
	}

	private void doJobRelationship(JobSnapshot jobSnapshot, int executeType) {
		List<JobRelationship> jobRelationships = getJobRelationshipDao().query(jobSnapshot.getName());
		// TODO 这里并发触发的话，需要考虑 是否成功并发执行
		for (JobRelationship jobRelationship : jobRelationships) {
			if ((executeType == jobRelationship.getExecuteType()) && (1 == jobRelationship.getStatus())) {
				execute(TriggerType.newDispatchTypeByJob(jobSnapshot.getName(), jobSnapshot.getId()),
						jobRelationship.getNextJobName());
			}
		}
	}

	@Override
	public void suspend(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && TriggerType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {
									if (result.isOk() && isSuspend(jobName)) {
										JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
										jobSnapshot.setStatus(JobSnapshotStatus.SUSPEND.value());
										getScheduleCache().updateJobSnapshot(jobSnapshot);
									}
								});
						workerSchedulerManager.suspend(TriggerType.newDispatchTypeByMaster(), jobName);
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
	public void goOn(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && (TriggerType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())
				|| TriggerType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName()))) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {
									if (result.isOk() && isRunning(jobName)) {
										JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
										jobSnapshot.setStatus(JobSnapshotStatus.EXECUTING.value());
										getScheduleCache().updateJobSnapshot(jobSnapshot);
									}
								});
						workerSchedulerManager.goOn(TriggerType.newDispatchTypeByMaster(), jobName);
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
	public void stop(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && (TriggerType.DISPATCH_TYPE_MANUAL.equals(dispatchType.getName())
				|| TriggerType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName()))) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.stop(TriggerType.newDispatchTypeByMaster(), jobName);
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
	public void rest(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && TriggerType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.rest(TriggerType.newDispatchTypeByMaster(), jobName);
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
	public void finish(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && TriggerType.DISPATCH_TYPE_MASTER.equals(dispatchType.getName())) {
			synchronized (keyLock.intern(jobName)) {
				Set<Node> nodes = getWorkerNode(jobName);
				AbstractWorkerSchedulerManager workerSchedulerManager = null;
				for (Node node : nodes) {
					try {
						workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.finish(TriggerType.newDispatchTypeByMaster(), jobName);
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
	public void askEnd(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && TriggerType.DISPATCH_TYPE_WORKER.equals(dispatchType.getName())) {
			if (isWait(jobName)) {
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
							goOn(TriggerType.newDispatchTypeByManual(), jobName);
						} else {
							log.info("check workSpace is empty after repaired workSpace["
									+ jobSnapshot.getWorkSpaceName() + "]");
							// 判断当前worker's job是被什么类型调度的 1.MANUAL手动触发
							// 2.SCHEDULER调度器触发
							if (TriggerType.DISPATCH_TYPE_MANUAL.equals(jobSnapshot.getTriggerType().getName())
									|| TriggerType.DISPATCH_TYPE_SCHEDULER
											.equals(jobSnapshot.getTriggerType().getName())) {
								log.info("master check the job[" + jobName + "] is not triggered by job");
								finish(TriggerType.newDispatchTypeByMaster(), jobName);
							} else {
								// 通过当job的触发获取它触发的它的job快照
								JobSnapshot lastJobSnapshot = getScheduleCache()
										.getJobSnapshot(jobSnapshot.getTriggerType().getName());
								// 如果触发的它的job快照==null,那么触发的它的job已经停止运行
								if (null == lastJobSnapshot) {
									// 从历史记录中获取触发它的 JobSnapshot
									lastJobSnapshot = getJobSnapshotDao().query(
											jobSnapshot.getTriggerType().getCurrentTimeMillis(),
											jobSnapshot.getTriggerType().getName());
									// 如果没获取到历史记录那么，我们将stop.然后打印日志非法被执行
									if (null == lastJobSnapshot) {
										stop(TriggerType.newDispatchTypeByManual(), jobName);
										log.error("the job[" + jobName + "]'s jobSnapshot[" + jobSnapshot.getId()
												+ "] is illegal execution");
									} else {
										// 如果触发它的jobSnapshot状态等于finishedstop
										// 时， 当前状态保持一致
										if (JobSnapshotStatus.FINISHED == lastJobSnapshot.getEnumStatus()) {
											log.info("master check the job[" + jobName
													+ "]'s triggered job is finished");
											finish(TriggerType.newDispatchTypeByMaster(), jobName);
										} else if (JobSnapshotStatus.STOP == lastJobSnapshot.getEnumStatus()) {
											log.info("master check the job[" + jobName + "]'s triggered job is stoped");
											stop(TriggerType.newDispatchTypeByManual(), jobName);
										} else {// 如果触发它的jobSnapshot状态等于其他时，那么触发它的job没有被正常stop,但是当前状态应该设置为stop
											log.info("master check the job[" + jobName + "]'s triggered job is stoped");
											stop(TriggerType.newDispatchTypeByManual(), jobName);
										}
									}
								} else {
									// 如果触发它的jobSnapshot状态等于EXECUTING或者SUSPEND
									// 时，那么应该休眠1000毫秒，否则保持跟触发它的jobSnapshot状态一样
									if (JobSnapshotStatus.EXECUTING == lastJobSnapshot.getEnumStatus()
											|| JobSnapshotStatus.SUSPEND == lastJobSnapshot.getEnumStatus()) {
										log.info("master check the job[" + jobName + "]'s triggered job is running");
										rest(TriggerType.newDispatchTypeByMaster(), jobName);
									} else if (JobSnapshotStatus.FINISHED == lastJobSnapshot.getEnumStatus()) {
										log.info("master check the job[" + jobName + "]'s triggered job is finished");
										finish(TriggerType.newDispatchTypeByMaster(), jobName);
									} else if (JobSnapshotStatus.STOP == lastJobSnapshot.getEnumStatus()) {
										log.info("master check the job[" + jobName + "]'s triggered job is stoped");
										stop(TriggerType.newDispatchTypeByMaster(), jobName);
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
	}

	@Override
	public void endWorker(TriggerType dispatchType, String jobName) {
		if (null != dispatchType && TriggerType.DISPATCH_TYPE_WORKER.equals(dispatchType.getName())) {
			boolean isFinish = isFinish(jobName);
			boolean isStop = isStop(jobName);
			if (isFinish || isStop) {
				synchronized (keyLock.intern(jobName)) {
					isFinish = isFinish(jobName);
					isStop = isStop(jobName);
					if (isFinish || isStop) {
						JobSnapshot jobSnapshot = getScheduleCache().getJobSnapshot(jobName);
						if (null != jobSnapshot) {
							JobSnapshotStatus state = null;
							if (isFinish) {
								state = JobSnapshotStatus.FINISHED;
							} else {
								state = JobSnapshotStatus.STOP;
							}
							jobSnapshot.setStatus(state.value());
							jobSnapshot.setEndTime(DateFormatUtils.format(new Date(), DateFormats.DATE_FORMAT_1));

							List<WorkerSnapshot> workerSnapshots = getScheduleCache().getWorkerSnapshots(jobName);
							totalWorkerSnapshot(jobSnapshot, workerSnapshots);
							reportJobSnapshot(jobSnapshot, workerSnapshots);

							getScheduleCache().delJob(jobName);
							getScheduleCache().delWorkerSnapshots(jobName);
							getScheduleCache().delJobSnapshot(jobName);

							// 当任务正常完成时 判断是否有当前任务是否有下个执行任务，如果有的话那么直接执行
							if (JobSnapshotStatus.FINISHED == state) {
								doJobRelationship(jobSnapshot, JobRelationship.EXECUTE_TYPE_SERIAL);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public synchronized void stopAll(TriggerType dispatchType) {
		List<JobSnapshot> allJobs = getScheduleCache().getJobSnapshots();
		Node currentNode = getClusterManager().getCurrentNode();
		for (JobSnapshot jobSnapshot : allJobs) {
			Job job = getJobDao().query(jobSnapshot.getName());
			Set<Node> nodes = getWorkerNode(job.getName());
			AbstractWorkerSchedulerManager workerSchedulerManager = null;
			for (Node node : nodes) {
				if (!currentNode.equals(node)) {
					try {
						workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
								result -> {

								});
						workerSchedulerManager.stop(TriggerType.newDispatchTypeByMaster(), job.getName());
						log.info("Already request worker node[" + node.getName() + "] to stop the job[" + job.getName()
								+ "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
		List<Node> nodes = getClusterManager().getWorkerNodes();
		AbstractWorkerSchedulerManager workerSchedulerManager = null;
		for (Node node : nodes) {
			if (!currentNode.equals(node)) {
				try {
					workerSchedulerManager = getClusterManager().loolup(node, AbstractWorkerSchedulerManager.class,
							result -> {

							});
					workerSchedulerManager.stopAll(TriggerType.newDispatchTypeByMaster());
					log.info("Already request worker node[" + node.getName() + "] to stop all");
				} catch (Exception e) {
					log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
				}
			}
		}
	}

	@Transactional
	private void reportJobSnapshot(JobSnapshot jobSnapshot, List<WorkerSnapshot> workerSnapshots) {
		if (null != jobSnapshot) {
			getJobSnapshotDao().update(jobSnapshot);
			if (null != workerSnapshots && workerSnapshots.size() > 0) {
				getWorkerSnapshotDao().batchSave(workerSnapshots);
			}
		}

	}

	public static class ScheduledJob implements org.quartz.Job {
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			AbstractSchedulerManager scheduleManager = (AbstractSchedulerManager) context.getJobDetail().getJobDataMap()
					.get(SCHEDULER_MANAGER_KEY);
			String jobName = (String) context.getJobDetail().getJobDataMap().get(JOB_NAME_KEY);
			scheduleManager.execute(TriggerType.newDispatchTypeByScheduler(), jobName);
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
	}
}
