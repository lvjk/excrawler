package six.com.crawler.work;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午5:02:00 通用爬虫实现
 */
public class CommonCrawlWorker extends AbstractCrawlWorker {

	public CommonCrawlWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue workQueue) {
		super(name, manager, job, site, workQueue);
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
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage,ResultContext resultContext) {

	}

	@Override
	protected void insideOnError(Exception e, Page doingPage) {

	}

}
