package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

public class NbCnnbfdcProjectListWorker extends AbstractCrawlWorker {

	RedisWorkQueue projectInfoQueue;
	String pageIndexTemplate = "<<pageIndex>>";
	String pageCountCss = "div[class=PagerCss]>a:contains(>|)";
	int pageIndex = 1;
	int pageCount;
	String projectListUrlTemplate = "http://newhouse.cnnbfdc.com/lpxx.aspx?p=" + pageIndexTemplate;


	@Override
	protected void insideInit() {
		String firstUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		projectInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "nb_cnnbfdc_project_info");
		Page firstPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
		firstPage.setMethod(HttpMethod.GET);
		getDowner().down(firstPage);
		Element pageCountElement = firstPage.getDoc().select(pageCountCss).first();
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

		getWorkQueue().clear();
		getWorkQueue().push(firstPage);
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
		List<String> projectUrls = resultContext.getExtractResult("projectUrl");
		List<String> districts = resultContext.getExtractResult("district");
		if (null != projectUrls) {
			for (int i = 0; i < projectUrls.size(); i++) {
				String projectUrl = projectUrls.get(i);
				String district = districts.get(i);
				Page projectInfoPage = new Page(doingPage.getSiteCode(), 1, projectUrl, projectUrl);
				projectInfoPage.setReferer(doingPage.getFinalUrl());
				projectInfoPage.getMetaMap().put("district", Arrays.asList(district));
				projectInfoQueue.push(projectInfoPage);
			}
		}
		pageIndex++;
		if (pageIndex <= pageCount) {
			String firstUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
			projectInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "nb_cnnbfdc_project_info");
			Page nextgPage = new Page(getSite().getCode(), 1, firstUrl, firstUrl);
			nextgPage.setReferer(doingPage.getFinalUrl());
			nextgPage.setMethod(HttpMethod.GET);
			getWorkQueue().push(nextgPage);
		}
	}

	@Override
	protected void insideOnError(Exception e, Page doingPage) {

	}

}
