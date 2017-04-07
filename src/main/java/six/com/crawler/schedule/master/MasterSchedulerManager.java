package six.com.crawler.schedule.master;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
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

import six.com.crawler.common.DateFormats;
import six.com.crawler.constants.JobConTextConstants;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotState;
import six.com.crawler.entity.Node;
import six.com.crawler.entity.NodeType;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.RedisRegisterKeyUtils;
import six.com.crawler.schedule.ScheduledJob;
import six.com.crawler.schedule.worker.WorkerAbstractSchedulerManager;
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
@Component
public class MasterSchedulerManager extends MasterAbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	private ConcurrentLinkedQueue<Job> pendingExecuteQueue = new ConcurrentLinkedQueue<>();

	private final static Lock waitQueueLock = new ReentrantLock();

	private final static Condition waitQueueCondition = waitQueueLock.newCondition();

	private Map<String, Set<String>> jobRunningWorkerNames = new ConcurrentHashMap<>();

	private Thread executeWaitQueueThread;

	private Scheduler scheduler;

	private final static String schedulerGroup = "exCrawler";

	protected void doInit() {
		initScheduler();
		// 初始化 读取等待执行任务线程 线程
		executeWaitQueueThread = new Thread(() -> {
			loopReadWaitingJob();
		}, "loop-read-waitingJob-thread");
		executeWaitQueueThread.setDaemon(true);
		// 启动读取等待执行任务线程 线程
		executeWaitQueueThread.start();
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
	public synchronized void execute(String jobName) {
		Job job = getJobDao().query(jobName);
		if (null != job) {
			String id = DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2);
			JobSnapshot jobSnapshot = new JobSnapshot();
			jobSnapshot.setId(id);
			jobSnapshot.setName(job.getName());
			jobSnapshot.setNextJobName(job.getNextJobName());
			jobSnapshot.setWorkSpaceName(job.getWorkSpaceName());
			jobSnapshot.setDesignatedNodeName(job.getDesignatedNodeName());
			jobSnapshot.setStatus(JobSnapshotState.WAITING_EXECUTED.value());
			registerJobSnapshot(jobSnapshot);
			submitWaitQueue(job);
			log.info("already submit job[" + jobName + "] to queue and it[" + id + "] will to be executed");
		} else {
			log.info("ready to execute job[" + jobName + "] is null");
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
		// 判断任务是否在运行
		if (!isRunning(job.getName())) {
			log.info("master node execute job[" + job.getName() + "]");
			String lockKey = getOperationJobLock(job.getName());
			List<Node> freeNodes = null;
			try {
				getRedisManager().lock(lockKey);
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
					JobSnapshot jobSnapshot = getJobSnapshot(job.getName());
					String fixedTableName = job.getParam(JobConTextConstants.FIXED_TABLE_NAME);
					String isSnapshotTable = job.getParam(JobConTextConstants.IS_SNAPSHOT_TABLE);
					String tempTbaleName = null;
					if ("1".equals(isSnapshotTable)) {
						JobSnapshot lastJobSnapshot = getJobSnapshotDao().queryLast(job.getName());
						if (null != lastJobSnapshot && StringUtils.isNotBlank(lastJobSnapshot.getTableName())
								&& lastJobSnapshot.getEnumStatus() != JobSnapshotState.FINISHED) {
							tempTbaleName = lastJobSnapshot.getTableName();
						} else {
							String jobStart = StringUtils.remove(jobSnapshot.getId(), job.getName() + "_");
							// 判断是否启用镜像表
							tempTbaleName = JobTableUtils.buildJobTableName(fixedTableName, jobStart);
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
					updateJobSnapshot(jobSnapshot);
					getJobSnapshotDao().save(jobSnapshot);
					int callSucceedCount = 0;
					WorkerAbstractSchedulerManager workerSchedulerManager=null;
					for (Node freeNode : freeNodes) {
						try {
							workerSchedulerManager=getNodeManager().loolup(freeNode, WorkerAbstractSchedulerManager.class);
							workerSchedulerManager.execute(job.getName());
							callSucceedCount++;
							log.info("already request worker node[" + freeNode.getName() + "] to execut the job["
									+ job.getName() + "]");
						} catch (Exception e) {
							log.error("call worker node[" + freeNode.getName() + "] to execut the job[" + job.getName()
									+ "] err", e);
						}
					}
					if (callSucceedCount > 0) {
						return;
					}
				} else {
					log.error("there is no node to execute job[" + job.getName() + "]");
				}
			} catch (Exception e) {
				log.error("master node execute job[" + job.getName() + "] err", e);
				delJobSnapshot(job.getName());
			} finally {
				getRedisManager().unlock(lockKey);
			}
		} else {
			log.error("the job[" + job.getName() + "] is running");
		}
		delJobSnapshot(job.getName());
	}

	/**
	 * call工作节点暂停任务
	 * 只有当请求工作节点执行暂停成功数==实际执行任务的工作节点数，才会是完全暂停
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public void suspend(String jobName) {
		String lockKey = getOperationJobLock(jobName);
		try {
			getRedisManager().lock(lockKey);
			List<Node> nodes = getWorkerNode(jobName);
			int callSuccessedCount = 0;
			WorkerAbstractSchedulerManager workerSchedulerManager=null;
			for (Node node : nodes) {
				try {
					workerSchedulerManager=getNodeManager().loolup(node, WorkerAbstractSchedulerManager.class);
					workerSchedulerManager.suspend(jobName);
					callSuccessedCount++;
					log.info("Already request worker node[" + node.getName() + "] to suspend the job[" + jobName + "]");
				} catch (Exception e) {
					log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
				}
			}
			
			if (callSuccessedCount == nodes.size()) {
				JobSnapshot jobSnapshot = getJobSnapshot(jobName);
				jobSnapshot.setStatus(JobSnapshotState.SUSPEND.value());
				updateJobSnapshot(jobSnapshot);
			}
		} finally {
			getRedisManager().unlock(lockKey);
		}
	}

	/**
	 * call工作节点继续任务
	 * 只要当请求工作节点继续执行任务的成功数>0，那么就认为它成功
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public void goOn(String jobName) {
		String lockKey = getOperationJobLock(jobName);
		try {
			getRedisManager().lock(lockKey);

			List<Node> nodes = getWorkerNode(jobName);
			int callSuccessedCount = 0;
			WorkerAbstractSchedulerManager workerSchedulerManager=null;
			for (Node node : nodes) {
				try {
					workerSchedulerManager=getNodeManager().loolup(node, WorkerAbstractSchedulerManager.class);
					workerSchedulerManager.goOn(jobName);
					callSuccessedCount++;
					log.info("Already request worker node[" + node.getName() + "] to goOn the job[" + jobName + "]");
				} catch (Exception e) {
					log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
				}
			}
			if (callSuccessedCount > 0) {
				JobSnapshot jobSnapshot = getJobSnapshot(jobName);
				jobSnapshot.setStatus(JobSnapshotState.EXECUTING.value());
				updateJobSnapshot(jobSnapshot);
			}
		} finally {
			getRedisManager().unlock(lockKey);
		}
	}

	/**
	 * call工作节点继续任务
	 * 只有当请求工作节点执行停止成功数==实际执行任务的工作节点数，才会是完全停止
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public void stop(String jobName) {
		String lockKey = getOperationJobLock(jobName);
		try {
			getRedisManager().lock(lockKey);

			List<Node> nodes = getWorkerNode(jobName);
			int callSuccessedCount = 0;
			WorkerAbstractSchedulerManager workerSchedulerManager=null;
			for (Node node : nodes) {
				try {
					workerSchedulerManager=getNodeManager().loolup(node, WorkerAbstractSchedulerManager.class);
					workerSchedulerManager.stop(jobName);
					callSuccessedCount++;
					log.info("Already request worker node[" + node.getName() + "] to stop the job[" + jobName + "]");
				} catch (Exception e) {
					log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
				}
			}
		
			if (callSuccessedCount == nodes.size()) {
				JobSnapshot jobSnapshot = getJobSnapshot(jobName);
				jobSnapshot.setStatus(JobSnapshotState.STOP.value());
				updateJobSnapshot(jobSnapshot);
			}
		} finally {
			getRedisManager().unlock(lockKey);
		}
	}

	public synchronized void stopAll() {
		List<JobSnapshot> allJobs = getJobSnapshots();
		Node currentNode = getNodeManager().getCurrentNode();
		for (JobSnapshot jobSnapshot : allJobs) {
			Job job = getJobDao().query(jobSnapshot.getName());
			List<Node> nodes = getWorkerNode(job.getName());
			WorkerAbstractSchedulerManager workerSchedulerManager=null;
			for (Node node : nodes) {
				if (!currentNode.equals(node)) {
					try {
						workerSchedulerManager=getNodeManager().loolup(node, WorkerAbstractSchedulerManager.class);
						workerSchedulerManager.stop(job.getName());
						log.info("Already request worker node[" + node.getName() + "] to stop the job[" + job.getName()
								+ "]");
					} catch (Exception e) {
						log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
					}
				}
			}
		}
		List<Node> nodes = getNodeManager().getWorkerNodes();
		WorkerAbstractSchedulerManager workerSchedulerManager=null;
		for (Node node : nodes) {
			if (!currentNode.equals(node)) {
				try {
					workerSchedulerManager=getNodeManager().loolup(node, WorkerAbstractSchedulerManager.class);
					workerSchedulerManager.stopAll();
					log.info("Already request worker node[" + node.getName() + "] to stop all");
				} catch (Exception e) {
					log.error("get node[" + node.getName() + "]'s workerSchedulerManager err", e);
				}
			}
		}
	}

	public void startWorker(String jobName, String workerName) {
		String lockKey = getOperationJobLock(jobName);
		try {
			getRedisManager().lock(lockKey);
			jobRunningWorkerNames.computeIfAbsent(jobName, mapKey -> new ConcurrentHashSet<>()).add(workerName);
		} finally {
			getRedisManager().unlock(lockKey);
		}
	}

	/**
	 * worker结束时调用此方法计数减1，如果计数==0的话那么导出job运行报告
	 */
	public void endWorker(String jobName, String workerName) {
		String lockKey = getOperationJobLock(jobName);
		try {
			getRedisManager().lock(lockKey);
			Set<String> jobWorkerNames = jobRunningWorkerNames.get(jobName);
			jobWorkerNames.remove(workerName);
			if (0 == jobWorkerNames.size()) {
				jobRunningWorkerNames.remove(jobName);
				List<WorkerSnapshot> workerSnapshots = getWorkerSnapshots(jobName);
				int finishedCount = 0;
				for (WorkerSnapshot workerSnapshot : workerSnapshots) {
					if (workerSnapshot.getState() == WorkerLifecycleState.FINISHED) {
						finishedCount++;
					}
				}
				JobSnapshot jobSnapshot = getJobSnapshot(jobName);
				JobSnapshotState state = null;
				if (workerSnapshots.size() == finishedCount) {
					state = JobSnapshotState.FINISHED;
				} else {
					state = JobSnapshotState.STOP;
				}
				jobSnapshot.setStatus(state.value());
				jobSnapshot.setEndTime(DateFormatUtils.format(new Date(), DateFormats.DATE_FORMAT_1));
				totalWorkerSnapshot(jobSnapshot, getWorkerSnapshots(jobName));
				reportJobSnapshot(jobSnapshot);
				delWorkerSnapshots(jobName);
				delJobSnapshot(jobName);
				// 当任务正常完成时 判断是否有当前任务是否有下个执行任务，如果有的话那么直接执行
				if (JobSnapshotState.FINISHED == state) {
					String nextJobName = jobSnapshot.getNextJobName();
					if (StringUtils.isNotBlank(nextJobName)) {
						log.info("execute job[" + "jobName" + "]'s nextJob[" + nextJobName + "]");
						execute(nextJobName);
					}
				}
			}
		} finally {
			getRedisManager().unlock(lockKey);
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
					newJobDataMap.put(ScheduledJob.JOB_NAME_KEY, job.getName());
					newJobDataMap.put(ScheduledJob.SCHEDULER_MANAGER_KEY, this);
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
		String patternKey = RedisRegisterKeyUtils.getResetPreKey() + "*";
		Set<String> keys = getRedisManager().keys(patternKey);
		for (String key : keys) {
			getRedisManager().del(key);
		}
	}

	private String getOperationJobLock(String jobName) {
		String lockKey = "masterSchedulerManager_operationJob_" + jobName;
		return lockKey;
	}

	/**
	 * 容器结束时调用此销毁方法
	 */
	@PreDestroy
	public void destroy() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.error("scheduler shutdown err");
		}
	}
}
