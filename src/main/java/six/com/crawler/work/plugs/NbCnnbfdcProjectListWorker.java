package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.exception.ProcessWorkerCrawlerException;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class NbCnnbfdcProjectListWorker extends AbstractCrawlWorker {

	WorkSpace<Page> projectInfoQueue;
	String pageIndexTemplate = "<<pageIndex>>";
	String pageCountCss = "div[class=PagerCss]>a:contains(>|)";
	int pageIndex = 1;
	int pageCount;
	String projectListUrlTemplate = "http://newhouse.cnnbfdc.com/lpxx.aspx?p=" + pageIndexTemplate;

	private Page buildPage(int pageIndex, String refererUrl) {
		String pageUrl = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		Page page = new Page(getSite().getCode(), 1, pageUrl, pageUrl);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.GET);
		page.setType(PageType.LISTING.value());
		return page;
	}
	
	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("nb_cnnbfdc_project_info", Page.class);
		int pageIndex = 1;
		Page firstPage = buildPage(pageIndex,null);// 初始化第一页
		
		getDowner().down(firstPage);
		
		Element pageCountElement = firstPage.getDoc().select(pageCountCss).first();
		String endPageUrl = pageCountElement.attr("href");
		String pageCountStr = StringUtils.remove(endPageUrl, "http://newhouse.cnnbfdc.com/lpxx.aspx?p=");
		if (NumberUtils.isNumber(pageCountStr)) {
			pageCount = Integer.valueOf(pageCountStr);
		} else {
			throw new ProcessWorkerCrawlerException("pageCount isn't num:" + pageCountStr);
		}
		
		getWorkSpace().clearDoing();
		
		getWorkSpace().push(firstPage);
		
		Page lastPage = firstPage;
		while (pageIndex <= pageCount) {
			Page nextPage = buildPage(pageIndex, lastPage.getFinalUrl());// 初始化第一页
			getWorkSpace().push(nextPage);
			lastPage = nextPage;
			pageIndex++;
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
		List<String> projectUrls = resultContext.getExtractResult("projectUrl");
		List<String> districts = resultContext.getExtractResult("district");
		List<String> projectIds = resultContext.getExtractResult("projectId");
		if (null != projectUrls) {
			for (int i = 0; i < projectUrls.size(); i++) {
				String projectUrl = projectUrls.get(i);
				String district = districts.get(i);
				String projectId = projectIds.get(i);
				Page projectInfoPage = new Page(doingPage.getSiteCode(), 1, projectUrl, projectUrl);
				projectInfoPage.setReferer(doingPage.getFinalUrl());
				projectInfoPage.getMetaMap().put("district", Arrays.asList(district));
				projectInfoPage.getMetaMap().put("projectId", Arrays.asList(projectId));
				projectInfoQueue.push(projectInfoPage);
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
