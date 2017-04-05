package six.com.crawler.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.dao.ExtractItemDao;
import six.com.crawler.dao.ExtractPathDao;
import six.com.crawler.dao.HttpProxyDao;
import six.com.crawler.dao.JobDao;
import six.com.crawler.dao.JobParamDao;
import six.com.crawler.dao.JobSnapshotDao;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.dao.SiteDao;
import six.com.crawler.dao.WorkerErrMsgDao;
import six.com.crawler.dao.WorkerSnapshotDao;
import six.com.crawler.email.QQEmailClient;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotState;
import six.com.crawler.entity.Node;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.http.HttpClient;
import six.com.crawler.node.NodeManager;
import six.com.crawler.ocr.ImageDistinguish;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.space.WorkSpaceManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:35
 */
public abstract class AbstractSchedulerManager implements SchedulerManager, InitializingBean {

	private final static String WORKER_NAME_PREFIX = "worker";

	private final static int SAVE_ERR_MSG_MAX = 20;

	private final static long WORKER_SNAPSHOT_REPORT_FREQUENCY = 1000 * 60;

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private SiteDao siteDao;

	@Autowired
	private ExtractPathDao extractPathDao;

	@Autowired
	private JobDao jobDao;

	@Autowired
	private JobSnapshotDao jobSnapshotDao;

	@Autowired
	private JobParamDao jobParamDao;

	@Autowired
	private ExtractItemDao extractItemDao;

	@Autowired
	private WorkerSnapshotDao workerSnapshotDao;

	@Autowired
	private WorkerErrMsgDao workerErrMsgDao;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private HttpProxyDao httpProxyDao;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ImageDistinguish imageDistinguish;

	@Autowired
	private QQEmailClient emailClient;

	@Autowired
	private WorkSpaceManager WorkSpaceManager;

	/**
	 * 内部初始化
	 */
	protected abstract void init();

	public void afterPropertiesSet() {
		init();
	}

	/**
	 * 判断job是否运行。 通过jobName 获取job运行快照JobSnapshot 如果存在并且状态是执行或者暂停那么 返回true否则false
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean isRunning(String jobName) {
		JobSnapshot jobSnapshot = getJobSnapshot(jobName);
		return null != jobSnapshot && (jobSnapshot.getEnumStatus() == JobSnapshotState.EXECUTING
				|| jobSnapshot.getEnumStatus() == JobSnapshotState.SUSPEND);
	}

	/**
	 * 通过jobname获取 job运行的节点
	 * 
	 * @param jobName
	 * @return
	 */
	public List<Node> getWorkerNode(String jobName) {
		List<Node> nodes = new ArrayList<>();
		List<WorkerSnapshot> workerSnapshots = getWorkerSnapshots(jobName);
		if (null != workerSnapshots) {
			String nodeName = null;
			Node findNode = null;
			for (WorkerSnapshot workerSnapshot : workerSnapshots) {
				nodeName = workerSnapshot.getLocalNode();
				findNode = getNodeManager().getWorkerNode(nodeName);
				if (null != findNode) {
					nodes.add(findNode);
				}
			}
		}
		return nodes;
	}

	/**
	 * 注解Job运行快照 JobSnapshot至缓存
	 * 
	 * @param jobSnapshot
	 */
	public void registerJobSnapshot(JobSnapshot jobSnapshot) {
		String jobName = jobSnapshot.getName();
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		String workerkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		String jobWorkerSerialNumberkey = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(jobName);
		getRedisManager().lock(jobSnapshotskey);
		try {
			// 先删除 过期job信息
			getRedisManager().hdel(jobSnapshotskey, jobName);
			// 先删除 过期jobWorkerSerialNumberkey信息
			getRedisManager().del(jobWorkerSerialNumberkey);
			// 先删除 过期workerkey 信息
			getRedisManager().del(workerkey);
			// 注册JobSnapshot
			updateJobSnapshot(jobSnapshot);
		} finally {
			getRedisManager().unlock(jobSnapshotskey);
		}
	}

	/**
	 * 更新job运行快照
	 * 
	 * @param jobSnapshot
	 */
	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		getRedisManager().hset(jobSnapshotskey, jobSnapshot.getName(), jobSnapshot);
	}

	/**
	 * 通过jobname获取 job运行快照
	 * 
	 * @param jobName
	 * @return
	 */
	public JobSnapshot getJobSnapshot(String jobName) {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		JobSnapshot jobSnapshot = getRedisManager().hget(jobSnapshotskeyskey, jobName, JobSnapshot.class);
		return jobSnapshot;
	}

	/**
	 * 统计job运行快照下所有worker的运行信息
	 * 
	 * @param jobSnapshot
	 * @param workerSnapshots
	 */
	public void totalWorkerSnapshot(JobSnapshot jobSnapshot, List<WorkerSnapshot> workerSnapshots) {
		if (null != jobSnapshot && null != workerSnapshots && !workerSnapshots.isEmpty()) {
			int totalProcessCount = 0;
			int totalResultCount = 0;
			int totalProcessTime = 0;
			int maxProcessTime = 0;
			int minProcessTime = -1;
			int avgProcessTime = 0;
			int errCount = 0;
			for (WorkerSnapshot workerSnapshot : workerSnapshots) {
				totalProcessCount += workerSnapshot.getTotalProcessCount();
				totalResultCount += workerSnapshot.getTotalResultCount();
				totalProcessTime += workerSnapshot.getTotalProcessTime();
				if (workerSnapshot.getMaxProcessTime() > maxProcessTime) {
					maxProcessTime = workerSnapshot.getMaxProcessTime();
				}
				if (-1 == minProcessTime || workerSnapshot.getMinProcessTime() < minProcessTime) {
					minProcessTime = workerSnapshot.getMinProcessTime();
				}
				avgProcessTime += workerSnapshot.getAvgProcessTime();
				errCount += workerSnapshot.getErrCount();
			}
			jobSnapshot.setTotalProcessCount(totalProcessCount);
			jobSnapshot.setTotalResultCount(totalResultCount);
			jobSnapshot.setTotalProcessTime(totalProcessTime / workerSnapshots.size());
			jobSnapshot.setMaxProcessTime(maxProcessTime);
			jobSnapshot.setMinProcessTime(minProcessTime);
			jobSnapshot.setAvgProcessTime(avgProcessTime / workerSnapshots.size());
			jobSnapshot.setErrCount(errCount);
		}
	}

	/**
	 * 获取所有运行任务快照
	 * 
	 * @return
	 */
	public List<JobSnapshot> getJobSnapshots() {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		Map<String, JobSnapshot> findMap = getRedisManager().hgetAll(jobSnapshotskeyskey, JobSnapshot.class);
		return new ArrayList<>(findMap.values());
	}

	/**
	 * 通过jobName删除指定的任务运行快照
	 * 
	 * @param jobName
	 */
	public void delJobSnapshot(String jobName) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		getRedisManager().hdel(jobSnapshotskey, jobName);
	}

	/**
	 * 通过任务名称删除运行任务所有worker的快照
	 * 
	 * @param jobName
	 */
	public void delWorkerSnapshots(String jobName) {
		String WorkerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		getRedisManager().del(WorkerSnapshotkey);
	}

	/**
	 * 通过任务名获取运行任务的所有 worker快照信息
	 * 
	 * @param jobName
	 * @return
	 */
	public List<WorkerSnapshot> getWorkerSnapshots(String jobName) {
		String workerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		List<WorkerSnapshot> workerSnapshots = new ArrayList<>();
		Map<String, WorkerSnapshot> workerInfosMap = getRedisManager().hgetAll(workerSnapshotkey, WorkerSnapshot.class);
		workerSnapshots.addAll(workerInfosMap.values());
		return workerSnapshots;
	}

	/**
	 * <p>
	 * 更新缓存中WorkSnapshot并且 Report WorkSnapshot
	 * <p>
	 * 前提 null != errMsgs&&errMsgs.size() > 0：
	 * </p>
	 * <p>
	 * 当isSaveErrMsg==true时会将异常消息保存
	 * </p>
	 * <p>
	 * 或者当errMsgs.size() >= SAVE_ERR_MSG_MAX时会将异常消息保存
	 * ,(从异常消息数量去写入，并免内存中大量消息没有被处理)
	 * </p>
	 * <p>
	 * 或者当LastReport >= WORKER_SNAPSHOT_REPORT_FREQUENCY时会将异常消息保存
	 * </p>
	 * 
	 * @param workerSnapshot
	 * @param isSaveErrMsg
	 */
	public void updateWorkSnapshotAndReport(WorkerSnapshot workerSnapshot, boolean isSaveErrMsg) {
		List<WorkerErrMsg> errMsgs = workerSnapshot.getWorkerErrMsgs();
		long nowTime = 0;
		if ((null != errMsgs && errMsgs.size() > 0)
				&& (isSaveErrMsg || errMsgs.size() >= SAVE_ERR_MSG_MAX || (nowTime = System.currentTimeMillis())
						- workerSnapshot.getLastReport() >= WORKER_SNAPSHOT_REPORT_FREQUENCY)) {
			getWorkerErrMsgDao().batchSave(errMsgs);
			errMsgs.clear();
			workerSnapshot.setLastReport(nowTime);
		}
		updateWorkerSnapshot(workerSnapshot);
	}

	/**
	 * 更新缓存中workerSnapshot
	 * 
	 * @param workerSnapshot
	 */
	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		String workerSnapshotKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobName());
		getRedisManager().hset(workerSnapshotKey, workerSnapshot.getName(), workerSnapshot);
	}

	/**
	 * 判断任务的所有worker是否全部wait
	 */
	public boolean workerIsAllWaited(String jobName) {
		List<WorkerSnapshot> workerSnapshots = getWorkerSnapshots(jobName);
		boolean result = true;
		for (WorkerSnapshot workerSnapshot : workerSnapshots) {
			// 判断其他工人是否还在运行
			if (workerSnapshot.getState() != WorkerLifecycleState.WAITED) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * 通过job 获取一个worker name 名字统一有 前缀 : 节点+job 名字
	 * +Job类型+WORKER_NAME_PREFIX+当前Job worker的序列号组成
	 * 
	 * @param job
	 * @return
	 */
	public String getWorkerNameByJob(Job job) {
		String key = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(job.getName());
		Long sernum = getRedisManager().incr(key);
		int serialNumber = sernum.intValue();
		StringBuilder sbd = new StringBuilder();
		sbd.append(job.getName()).append("_");
		sbd.append(WORKER_NAME_PREFIX).append("_");
		sbd.append(getNodeManager().getCurrentNode().getName()).append("_");
		sbd.append(serialNumber);
		return sbd.toString();
	}

	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public SiteDao getSiteDao() {
		return siteDao;
	}

	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

	public ExtractPathDao getExtractPathDao() {
		return extractPathDao;
	}

	public void setExtractPathDao(ExtractPathDao extractPathDao) {
		this.extractPathDao = extractPathDao;
	}

	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public JobSnapshotDao getJobSnapshotDao() {
		return jobSnapshotDao;
	}

	public void setJobSnapshotDao(JobSnapshotDao jobSnapshotDao) {
		this.jobSnapshotDao = jobSnapshotDao;
	}

	public JobParamDao getJobParamDao() {
		return jobParamDao;
	}

	public void setJobParamDao(JobParamDao jobParamDao) {
		this.jobParamDao = jobParamDao;
	}

	public ExtractItemDao getExtractItemDao() {
		return extractItemDao;
	}

	public void setExtractItemDao(ExtractItemDao extractItemDao) {
		this.extractItemDao = extractItemDao;
	}

	public WorkerSnapshotDao getWorkerSnapshotDao() {
		return workerSnapshotDao;
	}

	public void setWorkerSnapshotDao(WorkerSnapshotDao workerSnapshotDao) {
		this.workerSnapshotDao = workerSnapshotDao;
	}

	public WorkerErrMsgDao getWorkerErrMsgDao() {
		return workerErrMsgDao;
	}

	public void setWorkerErrMsgDao(WorkerErrMsgDao workerErrMsgDao) {
		this.workerErrMsgDao = workerErrMsgDao;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public HttpProxyDao getHttpProxyDao() {
		return httpProxyDao;
	}

	public void setHttpProxyDao(HttpProxyDao httpProxyDao) {
		this.httpProxyDao = httpProxyDao;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public ImageDistinguish getImageDistinguish() {
		return imageDistinguish;
	}

	public void setImageDistinguish(ImageDistinguish imageDistinguish) {
		this.imageDistinguish = imageDistinguish;
	}

	public QQEmailClient getEmailClient() {
		return emailClient;
	}

	public void setEmailClient(QQEmailClient emailClient) {
		this.emailClient = emailClient;
	}

	public WorkSpaceManager getWorkSpaceManager() {
		return WorkSpaceManager;
	}

	public void setWorkSpaceManager(WorkSpaceManager workSpaceManager) {
		WorkSpaceManager = workSpaceManager;
	}

}
