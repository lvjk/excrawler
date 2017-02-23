package six.com.crawler.work.plugs;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月24日 上午11:56:51
 */
public class CzfdcNewhouseInfoWorker extends HtmlCommonWorker {

	public CzfdcNewhouseInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}


	@Override
	protected void insideInit() {
	
	}

	@Override
	public void onComplete(Page p) {
		
	}

	@Override
	public void insideOnError(Exception t, Page p) {
	
	}


	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		
	}


	@Override
	protected void afterPaser(Page doingPage) throws Exception {
		
	}

}
