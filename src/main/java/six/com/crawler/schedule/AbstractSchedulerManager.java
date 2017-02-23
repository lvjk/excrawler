package six.com.crawler.schedule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpClient;
import six.com.crawler.common.ocr.ImageDistinguish;
import six.com.crawler.common.service.HttpPorxyService;
import six.com.crawler.common.service.JobService;
import six.com.crawler.common.service.PageService;
import six.com.crawler.common.service.SiteService;
import six.com.crawler.common.service.impl.PaserPathServiceImpl;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.Worker;
import six.com.crawler.common.RedisManager;
import six.com.crawler.common.configure.SpiderConfigure;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:35
 */
public abstract class AbstractSchedulerManager implements SchedulerManager {

	final static Logger LOG = LoggerFactory.getLogger(AbstractSchedulerManager.class);

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private SiteService siteService;

	@Autowired
	private PaserPathServiceImpl paserPathService;

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


	private final static String WORKER_NAME_PREFIX = "job_worker";

	/**
	 * 通过job 获取一个worker name 名字统一有 前缀 : 节点+job 名字
	 * +Job类型+WORKER_NAME_PREFIX+当前Job worker的序列号组成
	 * 
	 * @param job
	 * @return
	 */
	protected String getWorkerNameByJob(Job job) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(WORKER_NAME_PREFIX).append("_");
		sbd.append(job.getHostNode()).append("_");
		sbd.append(job.getName()).append("_");
		int serialNumber = getRegisterCenter().getSerNumOfWorkerByJob(job.getHostNode(), job.getName());
		sbd.append(serialNumber);
		return sbd.toString();
	}

	// 构建 job worker
	protected Worker buildJobWorker(Job job, WorkQueue stored, Site site) {
		Worker newJobWorker = null;
		String workerClass = job.getWorkerClass();
		// 判断是否是htmljob 如果是那么调用 htmlJobWorkerBuilder 构建worker
		Class<?> clz = null;
		Constructor<?> constructor = null;
		String workerName = null;

		try {
			clz = Class.forName(workerClass);
		} catch (ClassNotFoundException e) {
			LOG.error("ClassNotFoundException  err:" + workerClass, e);
		}
		if (null != clz) {
			try {
				constructor = clz.getConstructor(String.class, AbstractSchedulerManager.class, Job.class, Site.class,
						WorkQueue.class);
			} catch (NoSuchMethodException e) {
				LOG.error("NoSuchMethodException getConstructor err:" + clz, e);
			} catch (SecurityException e) {
				LOG.error("SecurityException err" + clz, e);
			}
			if (null != constructor) {
				try {
					workerName = getWorkerNameByJob(job);
					newJobWorker = (Worker) constructor.newInstance(workerName, this, job, site, stored);
				} catch (InstantiationException e) {
					LOG.error("InstantiationException  err:" + workerClass, e);
				} catch (IllegalAccessException e) {
					LOG.error("IllegalAccessException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				} catch (IllegalArgumentException e) {
					LOG.error("IllegalArgumentException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				} catch (InvocationTargetException e) {
					LOG.error("InvocationTargetException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				}
			}
		}
		return newJobWorker;
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

	public PaserPathServiceImpl getPaserPathService() {
		return paserPathService;
	}

	public void setPaserPathService(PaserPathServiceImpl paserPathService) {
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


	public abstract Node getCurrentNode();
}
