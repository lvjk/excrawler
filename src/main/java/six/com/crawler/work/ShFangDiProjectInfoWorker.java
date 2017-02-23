package six.com.crawler.work;

import java.util.List;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月7日 下午3:57:22
 */
public class ShFangDiProjectInfoWorker extends HtmlCommonWorker {

	RedisWorkQueue preSaleInfoQueue;

	public ShFangDiProjectInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	public void onComplete(Page p) {
		List<String> projectNamelist = p.getResultContext().takeResult("projectName");
		List<String> preSaleInfoUrlList = p.getResultContext().takeResult("预售信息url_2");
		if(null!=preSaleInfoUrlList){
			String preSaleInfoUrl = preSaleInfoUrlList.get(0);
			Page preSaleInfoPage = new Page(p.getSiteCode(), 1, preSaleInfoUrl, preSaleInfoUrl);
			preSaleInfoPage.setType(PageType.DATA.value());
			preSaleInfoPage.getMetaMap().put("projectName", projectNamelist);
			preSaleInfoPage.setReferer(p.getFinalUrl());
			preSaleInfoQueue.push(preSaleInfoPage);
		}
	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}



	@Override
	protected void insideInit() {
		preSaleInfoQueue = new RedisWorkQueue(getManager().getRedisManager(),
				"sh_fangdi_presale_info");
	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {
		
	}

}
