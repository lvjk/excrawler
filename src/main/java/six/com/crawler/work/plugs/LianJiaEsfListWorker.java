package six.com.crawler.work.plugs;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月24日 下午3:30:20
 */
public class LianJiaEsfListWorker extends AbstractCrawlWorker {

	WorkSpace<Page> nextWorkQueue;
	String houseUrlCss = "div[class=list-wrap]>ul>li>div[class=info-panel]>h2>a";
	String nextPageUrlCss = "div[class=page-box house-lst-page-box]>a:contains(下一页)";

	@Override
	protected void insideInit() {
		nextWorkQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "lianjia_erf_info",Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Elements houseUrlElements = doingPage.getDoc().select(houseUrlCss);
		for (Element houseUrlElement : houseUrlElements) {
			String houseUrl = houseUrlElement.attr("href");
			houseUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseUrl);
			Page houseInfoPage = new Page(getSite().getCode(), 1, houseUrl, houseUrl);
			houseInfoPage.setReferer(doingPage.getFinalUrl());
			houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
			if(!nextWorkQueue.isDone(houseInfoPage.getPageKey())){
				nextWorkQueue.push(houseInfoPage);
			}
		}
		Element nextPageUrlElement = doingPage.getDoc().select(nextPageUrlCss).first();
		if (null != nextPageUrlElement) {
			String nextPageUrl = nextPageUrlElement.attr("href");
			nextPageUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), nextPageUrl);
			Page nextPage = new Page(getSite().getCode(), 1, nextPageUrl, nextPageUrl);
			nextPage.setReferer(doingPage.getFinalUrl());
			nextPage.getMetaMap().putAll(doingPage.getMetaMap());
			getWorkQueue().push(nextPage);
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected boolean insideOnError(Exception e, Page doingPage) {
		return false;
	}

}
