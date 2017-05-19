package six.com.crawler;

import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import six.com.crawler.admin.service.ExtractPathService;
import six.com.crawler.admin.service.HttpPorxyService;
import six.com.crawler.admin.service.JobService;
import six.com.crawler.admin.service.SiteService;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.dao.DataTableDao;
import six.com.crawler.dao.ExtractPathDao;
import six.com.crawler.dao.HttpProxyDao;
import six.com.crawler.dao.JobDao;
import six.com.crawler.dao.JobParamDao;
import six.com.crawler.dao.JobSnapshotDao;
import six.com.crawler.dao.PageDao;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.dao.SiteDao;
import six.com.crawler.node.ClusterManager;
import six.com.crawler.ocr.ImageDistinguish;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.worker.AbstractWorkerPlugsManager;
import six.com.crawler.work.downer.HttpClient;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月26日 下午8:54:00 类说明
 */

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(StartMain.class)
@WebIntegrationTest("spider.home:F:/six/git/exCrawler")
public  class BaseTest extends AbstractJUnit4SpringContextTests {

	protected final static Logger log = LoggerFactory.getLogger(BaseTest.class);

	@Autowired
	public AbstractSchedulerManager jobWorkerManager;
	
	@Autowired
	public ClusterManager clusterManager;

	@Autowired
	public SiteService siteService;

	@Autowired
	public ExtractPathService paserPathService;

	@Autowired
	public HttpPorxyService httpPorxyService;

	@Autowired
	public JobService jobService;

	@Autowired
	public PageDao pageDao;

	@Autowired
	public SiteDao siteDao;
	@Autowired
	public ExtractPathDao pathdao;
	
	@Autowired
	public JobDao jobDao;

	@Autowired
	public JobParamDao jobParameterDao;

	@Autowired
	public RedisManager redisManager;


	@Autowired
	public HttpProxyDao httpProxyDao;
	
	@Autowired
	public SpiderConfigure spiderConfigure;
	
	@Autowired
	public HttpClient httpClient;
	
	@Autowired
	public ImageDistinguish imageDistinguish;
	
	
	@Autowired
	public JobSnapshotDao jobActivityInfoDao;
	
	@Autowired
	public DataTableDao dataTableDao;
	
	@Autowired
	public six.com.crawler.email.QQEmailClient QQEmailClient;
	
	@Autowired
	public AbstractWorkerPlugsManager workerPlugsManager;
	

	static {
		final TimeZone zone = TimeZone.getTimeZone("GMT+8"); // 获取中国时区
		TimeZone.setDefault(zone); // 设置时区
	}

	@Test
	public void test() {

	}

}
