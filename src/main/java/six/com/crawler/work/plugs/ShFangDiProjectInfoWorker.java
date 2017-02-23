package six.com.crawler.work.plugs;

import java.util.List;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月7日 下午3:57:22
 */
public class ShFangDiProjectInfoWorker extends AbstractCrawlWorker {

	RedisWorkQueue preSaleInfoQueue;

	public ShFangDiProjectInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		preSaleInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "sh_fangdi_presale_info");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {
		List<String> projectNamelist = result.getExtractResult("projectName");
		List<String> preSaleInfoUrlList = result.getExtractResult("预售信息url_2");
		if (null != preSaleInfoUrlList) {
			String preSaleInfoUrl = preSaleInfoUrlList.get(0);
			Page preSaleInfoPage = new Page(doingPage.getSiteCode(), 1, preSaleInfoUrl, preSaleInfoUrl);
			preSaleInfoPage.setType(PageType.DATA.value());
			preSaleInfoPage.getMetaMap().put("projectName", projectNamelist);
			preSaleInfoPage.setReferer(doingPage.getFinalUrl());
			preSaleInfoQueue.push(preSaleInfoPage);
		}
	}

	@Override
	public void onComplete(Page p,ResultContext resultContext) {
	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

}
