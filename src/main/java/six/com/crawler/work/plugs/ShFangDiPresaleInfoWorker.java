package six.com.crawler.work.plugs;

import java.util.ArrayList;
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
 * @date 创建时间：2016年12月9日 下午2:48:34
 */
public class ShFangDiPresaleInfoWorker extends AbstractCrawlWorker {

	RedisWorkQueue saleInfoQueue;

	public ShFangDiPresaleInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		saleInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "sh_fangdi_sale_info");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		List<String> projectNamelist = resultContext.getExtractResult("projectName");
		String projectName = null;
		if (null != projectNamelist && !projectNamelist.isEmpty()) {
			projectName = projectNamelist.get(0);
		}
		List<String> presalePremitList = resultContext.getExtractResult("presalePermit");
		if (null != presalePremitList) {
			projectNamelist = new ArrayList<>(presalePremitList.size());
			for (int i = 0; i < presalePremitList.size(); i++) {
				projectNamelist.add(projectName);
			}
			resultContext.addExtractResult("projectName", projectNamelist);
		}
		List<String> saleInfoUrlList = resultContext.getExtractResult("销售信息url_3");
		if (null != saleInfoUrlList) {
			String saleInfoUrl = null;
			String presalePremit = null;
			List<String> tempPresalePremitList = null;
			for (int i = 0; i < saleInfoUrlList.size(); i++) {
				tempPresalePremitList = new ArrayList<>();
				saleInfoUrl = saleInfoUrlList.get(i);
				presalePremit = presalePremitList.get(i);
				tempPresalePremitList.add(presalePremit);
				Page preSaleInfoPage = new Page(doingPage.getSiteCode(), 1, saleInfoUrl, saleInfoUrl);
				preSaleInfoPage.setType(PageType.DATA.value());
				preSaleInfoPage.getMetaMap().put("projectName", projectNamelist);
				preSaleInfoPage.getMetaMap().put("presalePermit", tempPresalePremitList);
				preSaleInfoPage.setReferer(doingPage.getFinalUrl());
				saleInfoQueue.push(preSaleInfoPage);
			}
		}
	}

	@Override
	public void onComplete(Page p,ResultContext resultContext) {
	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

}
