package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

public class NbCnnbfdcPresellListWorker extends AbstractCrawlWorker {
	
	RedisWorkSpace<Page> presellInfoQueue;
	
	String pageIndexTemplate = "<<pageIndex>>";
	
	String pageCountCss = "div[class='PagerCss']>a:contains(>|)";
	
	int pageIndex = 1;
	
	int pageCount;
	
	String projectListUrlTemplate = "http://newhouse.cnnbfdc.com/tmgs_xkzgs.aspx?p=" + pageIndexTemplate;

	@Override
	protected void insideInit() {
		String firstUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		presellInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "nb_cnnbfdc_presell_info",Page.class);
		if(!(helper.isDownloadState() && helper.isUseRawData())){
			Page firstPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
			firstPage.setMethod(HttpMethod.GET);
			getDowner().down(firstPage);
			Element pageCountElement = firstPage.getDoc().select(pageCountCss).first();
			if (null == pageCountElement) {
				throw new RuntimeException("don't find pageCountElement:" + pageCountCss);
			} else {
				String endPageUrl = pageCountElement.attr("href");
				String pageCountStr = StringUtils.remove(endPageUrl, "http://newhouse.cnnbfdc.com/tmgs_xkzgs.aspx?p=");
				if (NumberUtils.isNumber(pageCountStr)) {
					pageCount = Integer.valueOf(pageCountStr);
				} else {
					throw new RuntimeException("pageCount isn't num:" + pageCountStr);
				}

			}
			getWorkQueue().clearDoing();
			getWorkQueue().push(firstPage);
		}
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
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> presellNames = resultContext.getExtractResult("presellCardName");
		List<String> projectNames = resultContext.getExtractResult("projectName");
		List<String> regions = resultContext.getExtractResult("region");
		List<String> developerNames = resultContext.getExtractResult("developerName");
		List<String> presellUrls = resultContext.getExtractResult("presellUrl");
		if (null != presellNames) {
			for (int i = 0; i < presellUrls.size(); i++) {
				String presellUrl = presellUrls.get(i);
				presellUrl = presellUrl.substring(presellUrl.indexOf("'")+1, presellUrl.indexOf(",")-1);
				String presellName = presellNames.get(i);
				String projectName = projectNames.get(i);
				String region = regions.get(i);
				String developerName = developerNames.get(i);
				Page projectInfoPage = new Page(doingPage.getSiteCode(), 1, presellUrl, presellUrl);
				projectInfoPage.setReferer(doingPage.getFinalUrl());
				projectInfoPage.getMetaMap().put("presellCardName", Arrays.asList(presellName));
				projectInfoPage.getMetaMap().put("projectName", Arrays.asList(projectName));
				projectInfoPage.getMetaMap().put("region", Arrays.asList(region));
				projectInfoPage.getMetaMap().put("developerName", Arrays.asList(developerName));
				presellInfoQueue.push(projectInfoPage);
			}
		}
		
		if(!(helper.isDownloadState() && helper.isUseRawData())){
			pageIndex++;
			if (pageIndex <= pageCount) {
				String firstUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
				presellInfoQueue = new RedisWorkSpace<>(getManager().getRedisManager(), "nb_cnnbfdc_presell_info",Page.class);
				Page nextgPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
				nextgPage.setReferer(doingPage.getFinalUrl());
				nextgPage.setMethod(HttpMethod.GET);
				getWorkQueue().push(nextgPage);
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
}
