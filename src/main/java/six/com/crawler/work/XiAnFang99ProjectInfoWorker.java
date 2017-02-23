package six.com.crawler.work;


import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年12月26日 下午4:39:02 
*/
public class XiAnFang99ProjectInfoWorker extends HtmlCommonWorker{

	public XiAnFang99ProjectInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
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

	@Override
	protected void insideInit() {
	}

}
