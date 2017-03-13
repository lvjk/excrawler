package six.com.crawler.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import okhttp3.Request;
import six.com.crawler.common.DateFormats;
import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.email.QQEmailClient;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.JobSnapshotState;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.NodeType;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.common.utils.JobTableUtils;
import six.com.crawler.common.utils.MD5Utils;
import six.com.crawler.common.utils.ObjectCheckUtils;
import six.com.crawler.common.utils.ThreadUtils;

import six.com.crawler.work.Worker;
import six.com.crawler.work.WorkerLifecycleState;

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
 * 
 */
@Component
public class CommonSchedulerManager extends AbstractSchedulerManager implements InitializingBean {

	final static Logger LOG = LoggerFactory.getLogger(CommonSchedulerManager.class);

	// 记录wait Job
	private Queue<Job> waitingRunQueue = new ConcurrentLinkedQueue<>();

	private final static StampedLock stampedLock = new StampedLock();

	private final static Lock waitQueueLock = new ReentrantLock();

	private final static Condition waitQueueCondition = waitQueueLock.newCondition();

	// runningWroker使用计数器
	private AtomicInteger runningWroker = new AtomicInteger(0);

	private int workerRunningMaxSize;

	private ExecutorService executor;

	private Thread executeWaitQueueThread;
	// 当前节点
	private Node currentNode;
	// 节点心跳线程
	private Thread heartbeatThread;

	private Scheduler scheduler;

	private final static String schedulerGroup = "exCrawler";

	QQEmailClient emailClient;

	public void afterPropertiesSet() {

		workerRunningMaxSize = getConfigure().getConfig("worker.running.max.size", 20);
		executor = Executors.newFixedThreadPool(workerRunningMaxSize, new JobWorkerThreadFactory());
		String mailHost = getConfigure().getConfig("email.host", "smtp.qq.com");
		String mailPost = getConfigure().getConfig("email.post", "465");
		String mailUser = getConfigure().getConfig("email.user", "359852326@qq.com");
		String mailPwd = getConfigure().getConfig("email.pwd", "auqoidnoizodbijf");
		emailClient = new QQEmailClient(mailHost, mailPost, mailUser, mailPwd);

		initScheduler();
		initCurrentNode();
		// 修复job(因为应用挂掉导致未能正确update job 的状态)
		String nodeName = getCurrentNode().getName();
		LOG.info("repair node:" + nodeName);
		// 复位节点在注册中心的历史数据
		getRegisterCenter().reset(nodeName);

		// 初始化 心跳线程
		heartbeatThread = new Thread(() -> {
			LOG.info("running heartbeat thread");
			heartbeat();
		}, "Heartbeat-Thread");
		heartbeatThread.setDaemon(true);
		// 启动心跳线程
		heartbeatThread.start();

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
				LOG.info("从待执行队列里获取到job[" + job.getName() + "],准备执行");
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

	private void initCurrentNode() {
		String host = getConfigure().getHost();
		int port = getConfigure().getPort();
		String nodeName = getConfigure().getConfig("node.name", MD5Utils.MD5_8(host + ":" + port));
		Node existedNode = getRegisterCenter().getNode(nodeName);
		// 检查 nodeName是否有注册过。如果注册过那么检查 ip 和端口是相同
		if (null != existedNode && !(existedNode.getHost().equals(host) && existedNode.getPort() == port)) {
			LOG.error("the " + nodeName + "[" + host + ":" + port + "]has been executed");
			System.exit(1);
		}

		currentNode = new Node();
		currentNode.setType(getConfigure().getNodeType());
		currentNode.setName(nodeName);
		currentNode.setHost(host);
		currentNode.setPort(port);

	}

	/**
	 * 因为redis注册中心节点数据的有效期是 Constants.REDIS_REGISTER_CENTER_HEARTBEAT 秒
	 * 所以每次心跳更新时间应该要小于 有效期 所以
	 * sleeptime=Constants.REDIS_REGISTER_CENTER_HEARTBEAT*1000-1000;
	 */
	private void heartbeat() {
		long sleepTime = Constants.REDIS_REGISTER_CENTER_HEARTBEAT * 1000 - 1000;
		while (true) {
			Node totalNode = getJobService().totalNodeJobInfo(currentNode.getName());
			if (null != totalNode) {
				currentNode.setRunningJobMaxSize(workerRunningMaxSize);
				currentNode.setRunningJobSize(getRunningWorkerCount());
				currentNode.setTotalJobSize(totalNode.getTotalJobSize());
				currentNode.setTotalScheduleJobSize(totalNode.getTotalScheduleJobSize());
				currentNode.setTotalNoScheduleJobSize(totalNode.getTotalNoScheduleJobSize());
			}
			// 节点注册
			try {
				getRegisterCenter().registerNode(currentNode, Constants.REDIS_REGISTER_CENTER_HEARTBEAT);
			} catch (Exception e) {
				// 如果异常那么将关闭集群模式
				String subject = "register node to redis err:[" + currentNode.getName() + "]";
				LOG.error(subject, e);
				noticeAdminByEmail(subject, e.getMessage());
			}
			ThreadUtils.sleep(sleepTime);
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
	 * 获取空闲可以运行任务的节点
	 * 
	 * @param needFreeNodeSize
	 *            需要的空闲的节点数量 =总运行节点数-1
	 * @param jobLocalNode
	 *            任务的本地节点
	 * @return
	 */
	private List<Node> getOtherFreeNodes(int needFreeNodeSize, String jobLocalNode) {
		List<Node> otherFreeNodes = null;
		if (needFreeNodeSize > 0) {
			otherFreeNodes = new ArrayList<>();
			// 获取 空闲节点(已经根据节点资源进行排序过)
			List<Node> freeNodes = getFreeNodes();
			if (null != freeNodes) {
				for (Node freeNode : freeNodes) {
					if (freeNode.getType() != NodeType.MASTER && freeNode.getType() != NodeType.MASTER_STANDBY
							&& !StringUtils.equals(freeNode.getName(), jobLocalNode)) {
						otherFreeNodes.add(freeNode);
						if (otherFreeNodes.size() == needFreeNodeSize) {
							break;
						}
					}
				}
			}
		} else {
			otherFreeNodes = Collections.emptyList();
		}
		return otherFreeNodes;
	}

	/**
	 * 执行 worker
	 * 
	 * @param crawlerWorker
	 */
	private void executeWorker(Worker worker) {
		String currentNodeName = getCurrentNode().getName();
		Job job = worker.getJob();
		String jobLocalNodeName = job.getLocalNode();
		String jobName = job.getName();
		// 运行worker计数+1
		runningWroker.incrementAndGet();
		try {
			worker.init();
			// 执行 job worker
			worker.start();
		} catch (Exception e) {
			LOG.error("execute jobWorker {" + worker.getName() + "} err", e);
		} finally {
			// 运行worker计数-1
			runningWroker.decrementAndGet();
			if (StringUtils.equals(currentNodeName, jobLocalNodeName)) {
				// 导出Job 运行报告
				getJobService().reportJobSnapshot(jobLocalNodeName, jobName);
			}
			// 移除注册中心数据
			getRegisterCenter().delWorker(jobLocalNodeName, jobName, worker.getName());
			// wokrer 销毁
			worker.destroy();
		}

	}

	public synchronized void suspendWorkerByJob(String jobHostNode, String jobName) {
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobHostNode, jobName);
		jobSnapshot.setState(JobSnapshotState.SUSPEND.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
		List<Worker> list = getRegisterCenter().getWorkers(jobName);
		for (Worker worker : list) {
			worker.suspend();
		}
	}

	public synchronized void goOnWorkerByJob(String jobHostNode, String jobName) {
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobHostNode, jobName);
		jobSnapshot.setState(JobSnapshotState.EXECUTING.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
		List<Worker> list = getRegisterCenter().getWorkers(jobName);
		for (Worker worker : list) {
			worker.goOn();
		}
	}

	public synchronized void stopWorkerByJob(String jobHostNode, String jobName) {
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobHostNode, jobName);
		jobSnapshot.setState(JobSnapshotState.STOP.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
		List<Worker> list = getRegisterCenter().getWorkers(jobName);
		for (Worker worker : list) {
			worker.stop();
		}
	}

	public void finishWorkerByJob(String jobHostNode, String jobName) {
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobHostNode, jobName);
		jobSnapshot.setState(JobSnapshotState.FINISHED.value());
		getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
		List<Worker> list = getRegisterCenter().getWorkers(jobName);
		for (Worker worker : list) {
			worker.stop();
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
			String jobLocalNodeName = job.getLocalNode();
			String jobName = job.getName();
			JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobLocalNodeName, jobName);
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
	 * 
	 * @param job
	 */
	private void doExecute(Job job) {
		// 判断任务是否在运行
		if (!isRunning(job)) {
			// 判断任务节点是否是本地节点
			List<JobParam> jobParameters = getJobService().queryJobParams(job.getName());
			job.setParamList(jobParameters);
			// 新建一个 JobSnapshot 并生成一个id
			if (getCurrentNode().getType() == NodeType.MASTER) {
				LOG.info("MASTER节点调度执行job[" + job.getName() + "]");
				JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(job.getLocalNode(),
						job.getName());
				if (null == jobSnapshot) {
					throw new RuntimeException("执行前必须在缓存中注册jobSnapshot:" + job.getName());
				}
				Date nowDate = new Date();
				jobSnapshot.setStartTime(DateFormatUtils.format(nowDate, DateFormats.DATE_FORMAT_1));
				jobSnapshot.setEndTime(DateFormatUtils.format(nowDate, DateFormats.DATE_FORMAT_1));
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
				jobSnapshot.setTableName(tempTbaleName);
				jobSnapshot.setState(JobSnapshotState.EXECUTING.value());
				// 将jobSnapshot更新缓存 这里一定要 saveJobSnapshot
				getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
				getJobService().saveJobSnapshot(jobSnapshot);
				// 需要运行节点数量减去本地运行节点1
				int needFreeNodeSize = job.getNeedNodes() - 1;
				// 设置0 不启用集群
				needFreeNodeSize = 1;
				List<Node> otherFreeExecuteNodes = getOtherFreeNodes(needFreeNodeSize, job.getLocalNode());
				otherFreeExecuteNodes.forEach(node -> {
					callAssistExecute(node, job.getName());
				});
			} else {
				LOG.info("工作节点执行job[" + job.getName() + "]");
				callLocalExecute(job);
			}
		} else {
			LOG.info("任务job[" + job.getName() + "]正在执行，将任务返回到待执行队列");
			submitWaitQueue(job);
		}
	}

	/**
	 * 本地执行 由手动执行和定时触发 调用
	 * 
	 * @param job
	 */
	public void localExecute(Job job) {
		String id = job.getName() + "_" + DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2);
		JobSnapshot jobSnapshot = new JobSnapshot();
		jobSnapshot.setId(id);
		jobSnapshot.setName(job.getName());
		jobSnapshot.setLocalNode(job.getLocalNode());
		jobSnapshot.setState(JobSnapshotState.WAITING_EXECUTED.value());
		getJobService().registerJobSnapshotToRegisterCenter(jobSnapshot);
		submitWaitQueue(job);
	}

	public boolean callLocalExecute(Job job) {
		LOG.info("构建任务job[" + job.getName() + "] worker");
		Worker worker = buildJobWorker(job);
		executor.execute(() -> {
			LOG.info("开始执行任务job[" + job.getName() + "]'s worker");
			executeWorker(worker);
		});
		// 等待worker.getState() == WorkerLifecycleState.STARTED 时返回true
		LOG.info("等待任务job[" + job.getName() + "]'s worker开始执行");
		while (worker.getState() == WorkerLifecycleState.READY) {
		}
		LOG.info("任务job[" + job.getName() + "]'s worker开始执行");
		return true;
	}

	public void assistExecute(Job job) {
		submitWaitQueue(job);
	}

	@Override
	public String callAssistExecute(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/scheduled/assistExecute/"
				+ jobName;
		Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
		HttpResult result = getHttpClient().executeRequest(Request);
		String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
		return responeMsg;
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
		// 然后获取当前节点有关的job worker 然后调用stop
		List<Worker> list = getRegisterCenter().getLocalWorkers();
		for (Worker worker : list) {
			worker.stop();
		}
		// 然后shut down worker线程池
		executor.shutdown();
	}

	public Node getCurrentNode() {
		return currentNode;
	}

	@Override
	public List<Node> getFreeNodes() {
		List<Node> result = getClusterService().getClusterInfo();
		return result;
	}

	@Override
	public void update(Observable o, Object arg) {

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
