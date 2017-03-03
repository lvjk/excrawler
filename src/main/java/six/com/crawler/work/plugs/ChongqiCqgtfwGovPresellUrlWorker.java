package six.com.crawler.work.plugs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月28日 上午9:19:11
 */
public class ChongqiCqgtfwGovPresellUrlWorker extends AbstractCrawlWorker {

	final static Logger LOG = LoggerFactory.getLogger(ChongqiCqgtfwGovPresellUrlWorker.class);
	RedisWorkQueue presellInfoQueue;
	String pageIndexTemplate = "<<pageIndex>>";
	String firstUrl = "http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/index.htm";
	String urlTemplate = "http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/index_" + pageIndexTemplate + ".htm";
	int pageCount = -1;
	int pageIndex = 0;

	@Override
	protected void insideInit() {
		presellInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "chongqi_cqgtfw_gov_presell_info");
		Page firstPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
		getWorkQueue().clear();
		getWorkQueue().push(firstPage);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		//获取 分页总数
		if (-1 == pageCount) {
			String pageInfoCss = "div[class=page]>script";
			Element pageInfoElement = doingPage.getDoc().select(pageInfoCss).first();
			String html = pageInfoElement.html();
			String pageCountStr = StringUtils.substringBetween(html, "var countPage =", ";//共多少页");
			pageCountStr = StringUtils.trim(pageCountStr);
			if (!NumberUtils.isNumber(pageCountStr)) {
				throw new RuntimeException("don't find pageCount");
			}
			try {
				pageCount = Integer.valueOf(pageCountStr);
			} catch (Exception e) {
				LOG.error("pageCount Integer.valueOf(" + pageCountStr + ") err", e);
			}
			if (-1 == pageCount) {
				getAndSetState(WorkerLifecycleState.WAITED);
				throw new RuntimeException("don't find pageCount");
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> presellUrls = resultContext.getExtractResult("presellInfoUrl");
		if (null != presellUrls) {
			for (String presellUrl : presellUrls) {
				Page presellPage = new Page(getSite().getCode(), 1, presellUrl, presellUrl);
				presellPage.setSiteCode(doingPage.getSiteCode());
				presellPage.setReferer(doingPage.getFinalUrl());
				presellInfoQueue.push(presellPage);
			}
			pageIndex++;
			if (pageIndex < pageCount) {
				String nextPageUrl = StringUtils.replace(urlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
				Page nextPage = new Page(getSite().getCode(), 1, nextPageUrl, nextPageUrl);
				nextPage.setSiteCode(doingPage.getSiteCode());
				nextPage.setReferer(doingPage.getFinalUrl());
				getWorkQueue().push(nextPage);
			}
		}
	}

	@Override
	protected void insideOnError(Exception e, Page doingPage) {

	}

}
