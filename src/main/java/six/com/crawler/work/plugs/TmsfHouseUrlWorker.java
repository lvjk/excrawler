package six.com.crawler.work.plugs;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月22日 下午5:55:15
 */
public class TmsfHouseUrlWorker extends AbstractCrawlWorker {

	private String buildingIdCss = "input[id=buildingid]";
	private String sidCss = "input[id=sid]";
	private String areaCss = "input[id=area]";
	private String allpriceCss = "input[id=allprice]";
	private String housestateCss = "input[id=housestate]";
	private String housetypeCss = "input[id=housetype]";

	public TmsfHouseUrlWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {

	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {
		
	}

	@Override
	protected void onComplete(Page doingPage) {
		
	}

	@Override
	protected void insideOnError(Exception e, Page doingPage) {
		
	}

}
