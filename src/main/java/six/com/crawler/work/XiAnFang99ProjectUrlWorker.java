package six.com.crawler.work;

import java.util.List;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;


/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年12月26日 下午3:55:06 
*/
public class XiAnFang99ProjectUrlWorker extends HtmlCommonWorker{

	
	RedisWorkQueue projectInfoQueue;
	
	
	public XiAnFang99ProjectUrlWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	public void onComplete(Page p) {
		List<String> projectInfoUrls=p.getResultContext().getResult("项目信息url_1");
		if(null!=projectInfoUrls&&projectInfoUrls.size()>0){
			String url=projectInfoUrls.get(0);
			Page newPage=new Page(p.getSiteCode(), 1, url, url);
			newPage.setReferer(p.getFinalUrl());
			newPage.setType(PageType.DATA.value());
			if (!projectInfoQueue.duplicateKey(newPage.getPageKey())) {
				projectInfoQueue.push(newPage);
			}
		}
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
		projectInfoQueue = new RedisWorkQueue(getManager().getRedisManager(),
				"xianfang99_project_info_2");
	}

}
