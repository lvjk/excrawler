package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

public class NbCnnbfdcProjectListWorker extends AbstractCrawlWorker {

	RedisWorkSpace<Page> projectInfoQueue;
	String pageIndexTemplate = "<<pageIndex>>";
	String pageCountCss = "div[class=PagerCss]>a:contains(>|)";
	int pageIndex = 1;
	int pageCount;
	String projectListUrlTemplate = "http://newhouse.cnnbfdc.com/lpxx.aspx?p=" + pageIndexTemplate;


	@Override
	protected void insideInit() {
		String firstUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		projectInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "nb_cnnbfdc_project_info",Page.class);
		if(!(helper.isDownloadState() && helper.isUseRawData())){
			Page firstPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
			firstPage.setMethod(HttpMethod.GET);
			getWorkSpace().clearDoing();
			getWorkSpace().push(firstPage);
		}
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		if(pageCount!=-1){
			Element pageCountElement = doingPage.getDoc().select(pageCountCss).first();
			if(null == pageCountElement){
				Elements pageElements=doingPage.getDoc().select("div[class=PagerCss]>a");
				pageCountElement=pageElements.get(pageElements.size()-2);
			}
			
			if (null == pageCountElement) {
				throw new RuntimeException("don't find pageCountElement:" + pageCountCss);
			} else {
				String endPageUrl = pageCountElement.attr("href");
				String pageCountStr = StringUtils.remove(endPageUrl, "http://newhouse.cnnbfdc.com/lpxx.aspx?p=");
				if (NumberUtils.isNumber(pageCountStr)) {
					pageCount = Integer.valueOf(pageCountStr);
				} else {
					throw new RuntimeException("pageCount isn't num:" + pageCountStr);
				}
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}
	
	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> projectUrls = resultContext.getExtractResult("projectUrl");
		List<String> districts = resultContext.getExtractResult("district");
		List<String> projectIds = resultContext.getExtractResult("projectId");
		if (null != projectUrls) {
			for (int i = 0; i < projectUrls.size(); i++) {
				String projectUrl = projectUrls.get(i);
				String district = districts.get(i);
				String projectId=projectIds.get(i);
				Page projectInfoPage = new Page(doingPage.getSiteCode(), 1, projectUrl, projectUrl);
				projectInfoPage.setReferer(doingPage.getFinalUrl());
				projectInfoPage.getMetaMap().put("district", Arrays.asList(district));
				projectInfoPage.getMetaMap().put("projectId",Arrays.asList(projectId));
				projectInfoQueue.push(projectInfoPage);
			}
		}
		if(!(helper.isUseRawData() && helper.isDownloadState())){
			pageIndex++;
			if (pageIndex <= pageCount) {
				String firstUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
				Page nextgPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
				nextgPage.setReferer(doingPage.getFinalUrl());
				nextgPage.setMethod(HttpMethod.GET);
				getWorkSpace().push(nextgPage);
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
