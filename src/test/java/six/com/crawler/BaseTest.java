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

import six.com.crawler.schedule.CommonSchedulerManager;
import six.com.crawler.schedule.RegisterCenter;
import six.com.crawler.common.RedisManager;
import six.com.crawler.common.configure.SpiderConfigure;
import six.com.crawler.common.dao.DataTableDao;
import six.com.crawler.common.dao.JobSnapshotDao;
import six.com.crawler.common.dao.JobDao;
import six.com.crawler.common.dao.JobParamDao;
import six.com.crawler.common.dao.PageDao;
import six.com.crawler.common.dao.ExtractPathDao;
import six.com.crawler.common.dao.SiteDao;
import six.com.crawler.common.http.HttpClient;
import six.com.crawler.common.ocr.ImageDistinguish;
import six.com.crawler.common.service.HttpPorxyService;
import six.com.crawler.common.service.JobService;
import six.com.crawler.common.service.ExtracterService;
import six.com.crawler.common.service.SiteService;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月26日 下午8:54:00 类说明
 */

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(StartMain.class)
@WebIntegrationTest("spider.home:F:/six/git/exCrawler")
public  class BaseTest extends AbstractJUnit4SpringContextTests {

	protected final static Logger LOG = LoggerFactory.getLogger(BaseTest.class);

	@Autowired
	public CommonSchedulerManager jobWorkerManager;

	@Autowired
	public SiteService siteService;

	@Autowired
	public ExtracterService paserPathService;

	@Autowired
	public RegisterCenter registerCenter;

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
	public SpiderConfigure spiderConfigure;
	
	@Autowired
	public HttpClient httpClient;
	
	@Autowired
	public ImageDistinguish imageDistinguish;
	
	
	@Autowired
	public JobSnapshotDao jobActivityInfoDao;
	
	@Autowired
	public DataTableDao dataTableDao;
	

	static {
		final TimeZone zone = TimeZone.getTimeZone("GMT+8"); // 获取中国时区
		TimeZone.setDefault(zone); // 设置时区
	}

	@Test
	public void test() {

	}

}
