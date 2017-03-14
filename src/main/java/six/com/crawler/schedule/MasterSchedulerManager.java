package six.com.crawler.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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

import okhttp3.Request;
import six.com.crawler.common.DateFormats;
import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.JobSnapshotState;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.NodeType;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.common.utils.JobTableUtils;
import six.com.crawler.common.utils.ObjectCheckUtils;
import six.com.crawler.common.utils.ScheduleUrlUtils;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月18日 下午10:32:28 类说明
 * 
 *          执行一个爬虫工作流程 :
 * 
 *          1.提交爬虫job至待执行队列,并将job快照注册至注册中心 2.读取待执行任务队列线程读取队列中的job,
 *          判断job是否处于运行中，如果运行中重新返回队列，否则做初始化，呼叫执行爬虫任务线程池执行任务 3.执行爬虫任务线程池执行爬虫任务
 *          4.执行完任务
 *          
 *          注意:集群 命令调用还需完善
 * 
 * 
 */
@Component
public class MasterSchedulerManager extends MasterAbstractSchedulerManager {

	final static Logger LOG = LoggerFactory.getLogger(MasterSchedulerManager.class);
	// 记录wait Job
	private Queue<Job> waitingRunQueue = new ConcurrentLinkedQueue<>();

	private final static StampedLock stampedLock = new StampedLock();

	private final static Lock waitQueueLock = new ReentrantLock();

	private final static Condition waitQueueCondition = waitQueueLock.newCondition();

	private Map<String, AtomicInteger> jobRunningWorkerSize = new ConcurrentHashMap<>();
	// runningWroker使用计数器
	private AtomicInteger runningWroker = new AtomicInteger(0);

	private Thread executeWaitQueueThread;

	private Scheduler scheduler;

	private final static String schedulerGroup = "exCrawler";

	protected void init() {
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
		LOG.info("start Thread{loop-read-waitingJob-thread}");
		Job job = null;
		while (true) {
			job = waitingRunQueue.poll();
			// 如果获取到Job的那么 那么execute
			if (null != job) {
				LOG.info("MASTER node read job[" + job.getName() + "] from waitingQueue to ready execute");
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
			LOG.error("start scheduler err");
			System.exit(1);
		}
	}

	/**
	 * 加载需要调度的job
	 */
	private void loadScheduledJob() {
		if (NodeType.MASTER == getConfigure().getNodeType()) {
			LOG.info("start load scheduled job");
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("isScheduled", 1);
			List<Job> jobList = getJobService().query(parameters);
			int size = null != jobList ? jobList.size() : 0;
			LOG.info("load Scheduled Job size:" + size);
			for (Job job : jobList) {
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
	 * 本地执行 由手动执行和定时触发 调用
	 * 
	 * @param job
	 */
	public void execute(Job job) {
		String id = job.getName() + "_" + DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2);
		JobSnapshot jobSnapshot = new JobSnapshot();
		jobSnapshot.setId(id);
		jobSnapshot.setName(job.getName());
		jobSnapshot.setLocalNode(job.getLocalNode());
		jobSnapshot.setState(JobSnapshotState.WAITING_EXECUTED.value());
		getJobService().registerJobSnapshotToRegisterCenter(jobSnapshot);
		submitWaitQueue(job);
	}

	/**
	 * call工作节点执行任务
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public synchronized void doExecute(Job job) {
		// 判断任务是否在运行
		if (!isRunning(job)) {
			int needFreeNodeSize = job.getNeedNodes();
			List<Node> freeNodes = getClusterManager().getFreeNodes(needFreeNodeSize);
			if (null != freeNodes && freeNodes.size() > 0) {
				LOG.info("MASTER node execute job[" + job.getName() + "]");
				List<JobParam> jobParams = getJobService().queryJobParams(job.getName());
				job.setParamList(jobParams);
				JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(job.getName());
				String fixedTableName = job.getParam(JobConTextConstants.FIXED_TABLE_NAME);
				String isSnapshotTable = job.getParam(JobConTextConstants.IS_SNAPSHOT_TABLE);
				String tempTbaleName = null;
				if ("1".equals(isSnapshotTable)) {
					JobSnapshot lastJobSnapshot = getJobService().queryLastJobSnapshotFromHistory(jobSnapshot.getId(),
							job.getName());
					if (null != lastJobSnapshot && StringUtils.isNotBlank(lastJobSnapshot.getTableName())
							&& lastJobSnapshot.getEnumState() != JobSnapshotState.FINISHED) {
						tempTbaleName = lastJobSnapshot.getTableName();
					} else {
						String jobStart = StringUtils.remove(jobSnapshot.getId(), job.getName() + "_");
						// 判断是否启用镜像表
						tempTbaleName = JobTableUtils.buildJobTableName(fixedTableName, jobStart);
					}
				} else {
					tempTbaleName = fixedTableName;
				}
				Date nowDate = new Date();
				// 任务开始时候 开始时间和结束时间默认是一样的
				jobSnapshot.setStartTime(DateFormatUtils.format(nowDate, DateFormats.DATE_FORMAT_1));
				jobSnapshot.setEndTime(DateFormatUtils.format(nowDate, DateFormats.DATE_FORMAT_1));
				jobSnapshot.setTableName(tempTbaleName);
				jobSnapshot.setState(JobSnapshotState.EXECUTING.value());
				// 将jobSnapshot更新缓存 这里一定要 saveJobSnapshot
				getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
				getJobService().saveJobSnapshot(jobSnapshot);
				// 需要运行节点数量减去本地运行节点1
				LOG.info("get nodes[" + freeNodes.size() + "] to execute job[" + job.getName() + "]");
				for (Node freeNode : freeNodes) {
					String jobName = job.getName();
					String callUrl = ScheduleUrlUtils.getExecuteJob(freeNode, jobName);
					Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
					getHttpClient().executeRequest(Request);
					jobRunningWorkerSize.computeIfAbsent(jobName, mapKey -> new AtomicInteger()).incrementAndGet();
					LOG.info("the job[" + job.getName() + "] is executed by node[" + freeNode.getName() + "]");
				}
				return;
			} else {
				LOG.error("there is no node to execute job[" + job.getName() + "]");
			}
		} else {
			LOG.error("the job[" + job.getName() + "] is running");
		}
		getJobService().delJobSnapshotFromRegisterCenter(job.getName());

	}

	/**
	 * call工作节点暂停任务
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public synchronized void suspend(Job job) {
		String jobName = job.getName();
		List<Node> nodes = getWorkerNode(jobName);
		for (Node node : nodes) {
			String callUrl = ScheduleUrlUtils.getSuspendJob(node, jobName);
			Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
			HttpResult result = getHttpClient().executeRequest(Request);
			String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
			LOG.info(responeMsg);
		}
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobName);
		jobSnapshot.setState(JobSnapshotState.SUSPEND.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
	}

	/**
	 * call工作节点继续任务
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public synchronized void goOn(Job job) {
		String jobName = job.getName();
		List<Node> nodes = getWorkerNode(jobName);
		for (Node node : nodes) {
			String callUrl = ScheduleUrlUtils.getGoonJob(node, jobName);
			Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
			HttpResult result = getHttpClient().executeRequest(Request);
			String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
			LOG.info(responeMsg);
		}
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobName);
		jobSnapshot.setState(JobSnapshotState.EXECUTING.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
	}

	/**
	 * call工作节点继续任务
	 * 
	 * @param node
	 *            工作节点
	 * @param jobName
	 *            任务name
	 */
	public synchronized void stop(Job job) {
		String jobName = job.getName();
		List<Node> nodes = getWorkerNode(jobName);
		for (Node node : nodes) {
			String callUrl = ScheduleUrlUtils.getStopJob(node, jobName);
			Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
			HttpResult result = getHttpClient().executeRequest(Request);
			String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
			LOG.info(responeMsg);
		}
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobName);
		jobSnapshot.setState(JobSnapshotState.STOP.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
	}

	public synchronized void stopAll() {
		List<JobSnapshot> allJobs = getRegisterCenter().getJobSnapshots();
		for (JobSnapshot jobSnapshot : allJobs) {
			String jobName = jobSnapshot.getName();
			List<Node> nodes = getWorkerNode(jobName);
			for (Node node : nodes) {
				String callUrl = ScheduleUrlUtils.getStopJob(node, jobName);
				Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
				HttpResult result = getHttpClient().executeRequest(Request);
				String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
				LOG.info(responeMsg);
			}
			jobSnapshot.setState(JobSnapshotState.STOP.value());
			getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
		}
		List<Node> nodes = getClusterManager().getAllNodes();
		for (Node node : nodes) {
			String callUrl = ScheduleUrlUtils.getStopAll(node);
			Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
			HttpResult result = getHttpClient().executeRequest(Request);
			String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
			LOG.info(responeMsg);
		}
	}

	/**
	 * worker结束时调用此方法计数减1，如果计数==0的话那么导出job运行报告
	 */
	public synchronized void end(Job job) {
		String jobName = job.getName();
		if (0 == jobRunningWorkerSize.get(jobName).decrementAndGet()) {
			List<WorkerSnapshot> workerSnapshots = getJobService().getWorkSnapshotsFromRegisterCenter(jobName);
			if (null != workerSnapshots) {
				int finishedCount = 0;
				for (WorkerSnapshot workerSnapshot : workerSnapshots) {
					if (workerSnapshot.getState() == WorkerLifecycleState.FINISHED) {
						finishedCount++;
					}
				}
				JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobName);
				if (workerSnapshots.size() == finishedCount) {
					jobSnapshot.setState(JobSnapshotState.FINISHED.value());
				} else {
					jobSnapshot.setState(JobSnapshotState.STOP.value());
				}
				jobSnapshot.setEndTime(DateFormatUtils.format(new Date(), DateFormats.DATE_FORMAT_1));
				getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
				getJobService().reportJobSnapshot(jobName);
			}
		}
	}

	/**
	 * 判断注册中心是否有此job的worker
	 * 
	 * @param job
	 * @return
	 */
	public boolean isRunning(Job job) {
		if (waitingRunQueue.contains(job)) {
			return true;
		} else {
			String jobName = job.getName();
			JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobName);
			return null != jobSnapshot && (jobSnapshot.getEnumState() == JobSnapshotState.EXECUTING
					|| jobSnapshot.getEnumState() == JobSnapshotState.SUSPEND);
		}
	}

	public int getRunningWorkerCount() {
		return runningWroker.get();
	}

	/**
	 * 向调度器注册job
	 * 
	 * @param job
	 */
	public void scheduled(Job job) {
		ObjectCheckUtils.checkNotNull(job);
		JobKey jobKey = new JobKey(job.getName(), schedulerGroup);
		try {
			boolean existed = scheduler.checkExists(jobKey);
			if (existed) {
				return;
			}
		} catch (SchedulerException e1) {
			LOG.error("scheduler checkExists{" + job.getName() + "} err", e1);
			return;
		}

		long stamp = stampedLock.writeLock();
		try {
			boolean existed = scheduler.checkExists(jobKey);
			if (existed) {
				return;
			}
		} catch (SchedulerException e1) {
			LOG.error("scheduler checkExists{" + job.getName() + "} err", e1);
			return;
		}
		try {
			if (StringUtils.isNotBlank(job.getCronTrigger())) {
				Trigger trigger = TriggerBuilder.newTrigger().withIdentity(job.getName(), schedulerGroup)
						.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronTrigger())).startNow().build();
				JobBuilder jobBuilder = JobBuilder.newJob(ScheduledJob.class);
				jobBuilder.withIdentity(jobKey);
				JobDataMap newJobDataMap = new JobDataMap();
				newJobDataMap.put(ScheduledJob.JOB_KEY, job);
				newJobDataMap.put(ScheduledJob.SCHEDULER_MANAGER_KEY, this);
				jobBuilder.setJobData(newJobDataMap);
				JobDetail jobDetail = jobBuilder.build();
				try {
					scheduler.scheduleJob(jobDetail, trigger);
				} catch (SchedulerException e) {
					LOG.error("scheduleJob err:" + job.getName());
				}

			}

		} finally {
			stampedLock.unlock(stamp);
		}
	}

	/**
	 * 从调度任务中删除 指定job
	 * 
	 * @param job
	 */
	public void cancelScheduled(String jobName) {
		if (StringUtils.isNotBlank(jobName)) {
			try {
				JobKey key = new JobKey(jobName, schedulerGroup);
				scheduler.deleteJob(key);
			} catch (SchedulerException e) {
				LOG.error("deleteJobFromScheduled err", e);
			}
		}
	}

	/**
	 * 容器结束时调用此销毁方法
	 */
	@PreDestroy
	public void destroy() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			LOG.error("scheduler shutdown err");
		}
	}

	public List<Node> getWorkerNode(String jobName) {
		List<Node> nodes = new ArrayList<>();
		List<WorkerSnapshot> workerSnapshots = getRegisterCenter().getWorkerSnapshots(jobName);
		if (null != workerSnapshots) {
			String nodeName = null;
			Node findNode = null;
			for (WorkerSnapshot workerSnapshot : workerSnapshots) {
				nodeName = workerSnapshot.getLocalNode();
				findNode = getClusterManager().getNode(nodeName);
				nodes.add(findNode);
			}
		}
		return nodes;
	}

	@Override
	public void noticeAdminByEmail(String topic, String msg) {
		// for (String to : getConfigure().getAdminEmails()) {
		// // 注册异常 邮件通知管理员
		// try {
		// emailClient.sendMail(to, topic, msg);
		// } catch (MessagingException e) {
		// LOG.error("notice admin email err:" + to, e);
		// }
		// }
	}

	@Override
	public void noticeAdminByPhone(String topic, String msg) {

	}

}
