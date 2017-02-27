package six.com.crawler.schedule;

import java.util.ArrayList;
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
import six.com.crawler.common.email.QQEmailClient;
import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.JobSnapshotState;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.common.utils.MD5Utils;
import six.com.crawler.common.utils.ObjectCheckUtils;
import six.com.crawler.common.utils.ThreadUtils;

import six.com.crawler.work.Worker;

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
	private Queue<Job> waitingJob = new ConcurrentLinkedQueue<>();

	private StampedLock stampedLock = new StampedLock();

	// runningWroker使用计数器
	private AtomicInteger runningWroker = new AtomicInteger(0);

	// @Autowired
	// @Qualifier("HtmlJobWorkerBuilder")
	// private HtmlJobWorkerBuilder htmlJobWorkerBuilder;

	int workerRunningMaxSize;

	private ExecutorService executor;

	private Thread executeWaitQueueThread;
	// 当前节点
	private Node currentNode;
	// 节点心跳线程
	private Thread heartbeatThread;

	Thread checkValidHttpProxyThread;

	private static final int CheckInvalidCountMax = 3;// 检查无效性最大次数

	private Scheduler scheduler;

	private final static String schedulerGroup = "spider";

	private static Lock waitQueueLock = new ReentrantLock();

	private static Condition waitQueueCondition = waitQueueLock.newCondition();

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

		// checkValidHttpProxyThread = new Thread(() -> {
		// checkedHttpProxyValid();
		// }, "check-httpProxy-valid-thread");
		// checkValidHttpProxyThread.setDaemon(true);
		// checkValidHttpProxyThread.start();

		// 加载 当前节点 需要调度的任务
		loadScheduledJob();
	}

	private void loopReadWaitingJob() {
		LOG.info("start Thread{loop-read-waitingJob-thread}");
		Job job = null;
		while (true) {
			job = waitingJob.poll();
			// 如果获取到Job的那么 那么execute
			if (null != job) {
				scheduledExecute(job);
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

	protected void checkedHttpProxyValid() {
		LOG.info("start Thread{check-httpProxy-valid-thread}");
		while (true) {
			HttpProxy httpProxy = getHttpPorxyService().getHttpProxy("check-httpProxy-valid");
			if (null != httpProxy) {
				int checkInvalidCount = 0;
				while (checkInvalidCount < CheckInvalidCountMax) {
					if (!getHttpClient().isValidHttpProxy(httpProxy)) {
						checkInvalidCount++;
						ThreadUtils.sleep(1000 * 60);// 休息1分钟
					} else {
						break;
					}
				}
				if (checkInvalidCount >= CheckInvalidCountMax) {
					String msg = "this node[" + getCurrentNode().getName() + "] check httpProxy[" + httpProxy.toString()
							+ "] is invalid";
					LOG.error(msg);
					noticeAdminByEmail(msg, msg);
				}
			}
			ThreadUtils.sleep(1000 * 60 * 30);// 休息30分钟
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
		LOG.info("start load scheduled job");
		Node currentNode = getCurrentNode();
		String nodeName = currentNode.getName();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("hostNode", nodeName);
		parameters.put("isScheduled", 1);
		List<Job> jobList = getJobService().query(parameters);
		int size = null != jobList ? jobList.size() : 0;
		LOG.info("load Scheduled Job size:" + size);
		for (Job job : jobList) {
			scheduled(job);
		}
	}

	/**
	 * 异步执行job
	 * 
	 * @param job
	 */
	private List<Node> getOtherFreeExecuteNodes(Job job) {
		List<Node> otherFreeExecuteNodes = new ArrayList<>();
		if (job.getNeedNodes() > 1) {
			// 获取 空闲节点(已经根据节点资源进行排序过)
			List<Node> freeNodes = getFreeNodes();
			int count = 0;
			// 从freeNodes里取job.getNeedNodes()-1 个节点 排除当前节点
			for (Node freeNode : freeNodes) {
				if (!freeNode.getName().equalsIgnoreCase(getCurrentNode().getName())) {
					otherFreeExecuteNodes.add(freeNode);
					count++;
				}
				if (count >= job.getNeedNodes() - 1) {
					break;
				}
			}
		}
		return otherFreeExecuteNodes;
	}

	/**
	 * 执行 worker
	 * 
	 * @param crawlerWorker
	 */
	private void executeJobWorker(Worker jobWorker) {
		getRegisterCenter().registerWorker(jobWorker);
		Job job = jobWorker.getJob();
		String nodeName = job.getHostNode();
		String jobName = job.getName();
		// 运行worker计数+1
		runningWroker.incrementAndGet();
		try {
			// 执行 job worker
			jobWorker.start();
		} catch (Exception e) {
			LOG.error("execute jobWorker {" + jobWorker.getName() + "} err", e);
		} finally {
			// 运行worker计数-1
			runningWroker.decrementAndGet();
			String currentNodeName = getCurrentNode().getName();
			String jobNodeName = job.getHostNode();
			if (currentNodeName.equals(jobNodeName)) {
				// 导出Job 运行报告
				getJobService().reportJobSnapshot(nodeName, jobName);
				getJobService().delJobSnapshotFromRegisterCenter(nodeName, jobName);
			}
			// wokrer 销毁
			jobWorker.destroy();
			// 移除注册中心数据
			getRegisterCenter().delWorker(job.getHostNode(), job.getName(), jobWorker.getName());
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
	public boolean isRunning(String jobHostNode, String jobName) {
		JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobHostNode, jobName);
		return null != jobSnapshot && (jobSnapshot.getEnumState() == JobSnapshotState.EXECUTING
				|| jobSnapshot.getEnumState() == JobSnapshotState.SUSPEND);
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
	public void submitWaitQueue(Job job) {
		if (null == job) {
			throw new NullPointerException();
		}
		waitQueueLock.lock();
		try {
			JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(job.getHostNode(),
					job.getName());
			if (null == jobSnapshot) {
				jobSnapshot = JobSnapshot.buildJobSnapshot(job);
				// id=jobname +时间
				jobSnapshot.setId(job.getName() + "_"
						+ DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2));
				jobSnapshot.setState(JobSnapshotState.WAITING_EXECUTED.value());
				getJobService().registerJobSnapshotToRegisterCenter(jobSnapshot);
			}
			if (jobSnapshot.getEnumState() != JobSnapshotState.WAITING_EXECUTED
					&& jobSnapshot.getEnumState() != JobSnapshotState.EXECUTING
					&& jobSnapshot.getEnumState() != JobSnapshotState.SUSPEND) {
				jobSnapshot.setState(JobSnapshotState.WAITING_EXECUTED.value());
				getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
			}
			waitingJob.add(job);
			waitQueueCondition.signal();
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
	private void scheduledExecute(Job job) {
		String jobHostNode = job.getHostNode();
		String jobName = job.getName();
		if (!isRunning(jobHostNode, jobName)) {
			List<Node> otherFreeExecuteNodes = getOtherFreeExecuteNodes(job);
			Worker worker = buildJobWorker(job);
			worker.init();
			JobSnapshot jobSnapshot = getJobService().getJobSnapshotFromRegisterCenter(jobHostNode, jobName);
			jobSnapshot.setState(JobSnapshotState.EXECUTING.value());
			getJobService().updateJobSnapshotToRegisterCenter(jobSnapshot);
			executor.execute(() -> {
				executeJobWorker(worker);
			});
			otherFreeExecuteNodes.forEach(node -> {
				callNodeExecuteJob(node, jobName);
			});
		}else{
			submitWaitQueue(job);
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
		return new ArrayList<Node>();
	}

	@Override
	public String callNodeExecuteJob(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/scheduled/assistExecute/"
				+ jobName;
		Request Request = getHttpClient().buildRequest(callUrl, null, HttpMethod.GET, null, null, null);
		HttpResult result = getHttpClient().executeRequest(Request);
		String responeMsg = getHttpClient().getHtml(result, ContentType.OTHER);
		return responeMsg;
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
