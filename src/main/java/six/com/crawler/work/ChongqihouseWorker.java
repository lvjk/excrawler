package six.com.crawler.work;

import java.util.List;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年11月22日 上午11:01:31 
*/
public class ChongqihouseWorker extends HtmlCommonWorker{

	public ChongqihouseWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
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
	protected void afterPaser(Page page) throws Exception {
		ResultContext resultContext = page.getResultContext();
		// 将page中的新data urls保存到处理队列
		List<String> newDataUrlsResult = resultContext.takeResult("dataUrl");
		if (null != newDataUrlsResult) {
			int duplicateDoneSize=0;
			for (String newUrl : newDataUrlsResult) {
				Page newPage = new Page(page.getSiteCode(), 1, page.getFirstUrl(), newUrl);
				// 将当前page的url 赋值给新page 的Referer
				newPage.setReferer(page.getFinalUrl());
				newPage.setType(PageType.DATA.value());
				newPage.setDepth(page.getDepth() + 1);
				newPage.setMetaMap(page.getMetaMap());
				if (!getWorkQueue().duplicateKey(newPage.getPageKey())) {
					getWorkQueue().push(newPage);
				}else{
					duplicateDoneSize++;
				}
			}
			if(duplicateDoneSize==0){
				List<String> nextUrlsResult = resultContext.takeResult("nextUrl");
				if (null != nextUrlsResult && nextUrlsResult.size() > 0) {
					String nextUrl = nextUrlsResult.get(0);
					Page nextPage = new Page(page.getSiteCode(), page.getPageNum() + 1, page.getFirstUrl(), nextUrl);
					// 将当前page的url 赋值给新page 的Referer
					nextPage.setReferer(page.getFinalUrl());
					nextPage.setType(page.getType().value());
					nextPage.setDepth(page.getDepth());
					nextPage.setMetaMap(page.getMetaMap());
					getWorkQueue().push(nextPage);
				}
			}
		}
	}

}
