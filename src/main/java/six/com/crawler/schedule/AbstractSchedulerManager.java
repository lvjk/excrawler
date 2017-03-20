package six.com.crawler.schedule;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.common.http.HttpClient;
import six.com.crawler.common.ocr.ImageDistinguish;
import six.com.crawler.common.service.ClusterService;
import six.com.crawler.common.service.HttpPorxyService;
import six.com.crawler.common.service.JobService;
import six.com.crawler.common.service.PageService;
import six.com.crawler.common.service.SiteService;
import six.com.crawler.common.service.impl.ExtracterServiceImpl;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.cluster.ClusterManager;
import six.com.crawler.common.RedisManager;
import six.com.crawler.common.email.QQEmailClient;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.JobSnapshotState;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.common.service.WorkerErrMsgService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:35
 */
public abstract class AbstractSchedulerManager implements SchedulerManager, InitializingBean {

	private final static String WORKER_NAME_PREFIX = "job_worker";
	
	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private ClusterManager clusterManager;

	@Autowired
	private SiteService siteService;

	@Autowired
	private ExtracterServiceImpl paserPathService;

	@Autowired
	private JobService jobService;
	
	@Autowired
	private RedisManager redisManager;

	@Autowired
	private PageService pageService;

	@Autowired
	private HttpPorxyService httpPorxyService;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ImageDistinguish imageDistinguish;

	@Autowired
	private ClusterService clusterService;

	@Autowired
	private QQEmailClient emailClient;

	@Autowired
	private WorkerErrMsgService WorkerErrMsgService;
	
	protected abstract void init();

	public void afterPropertiesSet() {
		init();
	}
	
	/**
	 * 判断注册中心是否有此job的worker
	 * 
	 * @param job
	 * @return
	 */
	public boolean isRunning(Job job) {
		String jobName = job.getName();
		JobSnapshot jobSnapshot = getJobSnapshot(jobName);
		return null != jobSnapshot && (jobSnapshot.getEnumState() == JobSnapshotState.EXECUTING
				|| jobSnapshot.getEnumState() == JobSnapshotState.SUSPEND);
	}


	public List<Node> getWorkerNode(String jobName) {
		List<Node> nodes = new ArrayList<>();
		List<WorkerSnapshot> workerSnapshots = getWorkerSnapshots(jobName);
		if (null != workerSnapshots) {
			String nodeName = null;
			Node findNode = null;
			for (WorkerSnapshot workerSnapshot : workerSnapshots) {
				nodeName = workerSnapshot.getLocalNode();
				findNode = getClusterManager().getWorkerNode(nodeName);
				if (null != findNode) {
					nodes.add(findNode);
				}
			}
		}
		return nodes;
	}
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

	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		getRedisManager().hset(jobSnapshotskey, jobSnapshot.getName(), jobSnapshot);
	}


	public JobSnapshot getJobSnapshot(String jobName) {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		JobSnapshot jobSnapshot = getRedisManager().hget(jobSnapshotskeyskey, jobName, JobSnapshot.class);
		return jobSnapshot;
	}
	
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


	public List<JobSnapshot> getJobSnapshots() {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		Map<String, JobSnapshot> findMap = getRedisManager().hgetAll(jobSnapshotskeyskey, JobSnapshot.class);
		return new ArrayList<>(findMap.values());
	}

	public void delJobSnapshot(String jobName) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		getRedisManager().hdel(jobSnapshotskey, jobName);
	}

	public void delWorkerSnapshots(String jobName) {
		String WorkerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		getRedisManager().del(WorkerSnapshotkey);
	}

	public List<WorkerSnapshot> getWorkerSnapshots(String jobName) {
		String workerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		List<WorkerSnapshot> workerSnapshots = new ArrayList<>();
		Map<String, WorkerSnapshot> workerInfosMap = getRedisManager().hgetAll(workerSnapshotkey, WorkerSnapshot.class);
		workerSnapshots.addAll(workerInfosMap.values());
		return workerSnapshots;
	}
	
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
		sbd.append(WORKER_NAME_PREFIX).append("_");
		sbd.append(job.getName()).append("_");
		sbd.append(serialNumber);
		return sbd.toString();
	}
	
	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		String workerSnapshotKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobName());
		getRedisManager().hset(workerSnapshotKey, workerSnapshot.getName(), workerSnapshot);
	}

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public ExtracterServiceImpl getPaserPathService() {
		return paserPathService;
	}

	public void setPaserPathService(ExtracterServiceImpl paserPathService) {
		this.paserPathService = paserPathService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}
	

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public PageService getPageService() {
		return pageService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public HttpPorxyService getHttpPorxyService() {
		return httpPorxyService;
	}

	public void setHttpPorxyService(HttpPorxyService httpPorxyService) {
		this.httpPorxyService = httpPorxyService;
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

	public ClusterService getClusterService() {
		return clusterService;
	}

	public void setClusterService(ClusterService clusterService) {
		this.clusterService = clusterService;
	}
	
	public QQEmailClient getEmailClient() {
		return emailClient;
	}

	public void setEmailClient(QQEmailClient emailClient) {
		this.emailClient = emailClient;
	}
	
	
	public WorkerErrMsgService getWorkerErrMsgService() {
		return WorkerErrMsgService;
	}

	public void setWorkerErrMsgService(WorkerErrMsgService workerErrMsgService) {
		WorkerErrMsgService = workerErrMsgService;
	}

}
