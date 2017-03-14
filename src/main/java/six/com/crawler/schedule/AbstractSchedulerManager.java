package six.com.crawler.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.http.HttpClient;
import six.com.crawler.common.ocr.ImageDistinguish;
import six.com.crawler.common.service.ClusterService;
import six.com.crawler.common.service.HttpPorxyService;
import six.com.crawler.common.service.JobService;
import six.com.crawler.common.service.PageService;
import six.com.crawler.common.service.SiteService;
import six.com.crawler.common.service.impl.ExtracterServiceImpl;
import six.com.crawler.cluster.ClusterManager;
import six.com.crawler.common.RedisManager;
import six.com.crawler.common.configure.SpiderConfigure;
import six.com.crawler.common.email.QQEmailClient;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:35
 */
public abstract class AbstractSchedulerManager implements SchedulerManager, InitializingBean {

	final static Logger LOG = LoggerFactory.getLogger(AbstractSchedulerManager.class);

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private ClusterManager clusterManager;

	@Autowired
	private SiteService siteService;

	@Autowired
	private ExtracterServiceImpl paserPathService;

	@Autowired
	private RegisterCenter registerCenter;

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

	private final static String WORKER_NAME_PREFIX = "job_worker";
	
	@Autowired
	private QQEmailClient emailClient;

	public void afterPropertiesSet() {
		init();
	}

	/**
	 * 通过job 获取一个worker name 名字统一有 前缀 : 节点+job 名字
	 * +Job类型+WORKER_NAME_PREFIX+当前Job worker的序列号组成
	 * 
	 * @param job
	 * @return
	 */
	public String getWorkerNameByJob(Job job) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(WORKER_NAME_PREFIX).append("_");
		sbd.append(job.getLocalNode()).append("_");
		sbd.append(job.getName()).append("_");
		int serialNumber = getRegisterCenter().getSerNumOfWorkerByJob(job.getName());
		sbd.append(serialNumber);
		return sbd.toString();
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

	public RegisterCenter getRegisterCenter() {
		return registerCenter;
	}

	public void setRegisterCenter(RegisterCenter registerCenter) {
		this.registerCenter = registerCenter;
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

	protected abstract void init();
}
