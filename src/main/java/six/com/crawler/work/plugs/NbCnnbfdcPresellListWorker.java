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
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class NbCnnbfdcPresellListWorker extends AbstractCrawlWorker {

	WorkSpace<Page> presellInfoQueue;

	String pageIndexTemplate = "<<pageIndex>>";

	String pageCountCss = "div[class='PagerCss']>a:contains(>|)";

	int pageIndex = 1;

	int pageCount = -1;

	String projectListUrlTemplate = "http://newhouse.cnnbfdc.com/tmgs_xkzgs.aspx?p=" + pageIndexTemplate;

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
		presellInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("nb_cnnbfdc_presell_info", Page.class);
		
		int pageIndex = 1;
		Page firstPage = buildPage(pageIndex,null);// 初始化第一页
		
		getDowner().down(firstPage);
		
		Element pageCountElement = firstPage.getDoc().select(pageCountCss).first();
		
		String endPageUrl = pageCountElement.attr("href");
		String pageCountStr = StringUtils.remove(endPageUrl, "http://newhouse.cnnbfdc.com/tmgs_xkzgs.aspx?p=");
		if (NumberUtils.isNumber(pageCountStr)) {
			pageCount = Integer.valueOf(pageCountStr);
		} else {
			throw new RuntimeException("pageCount isn't num:" + pageCountStr);
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
		List<String> presellIds = resultContext.getExtractResult("presellId");
		List<String> presellNames = resultContext.getExtractResult("presellCardName");
		List<String> projectNames = resultContext.getExtractResult("projectName");
		List<String> regions = resultContext.getExtractResult("region");
		List<String> developerNames = resultContext.getExtractResult("developerName");
		List<String> presellUrls = resultContext.getExtractResult("presellUrl");
		if (null != presellNames) {
			for (int i = 0; i < presellUrls.size(); i++) {
				String presellId = presellIds.get(i);
				String presellUrl = presellUrls.get(i);
				presellUrl = presellUrl.substring(presellUrl.indexOf("'") + 1, presellUrl.indexOf(",") - 1);
				String presellName = presellNames.get(i);
				String projectName = projectNames.get(i);
				String region = regions.get(i);
				String developerName = developerNames.get(i);
				Page projectInfoPage = new Page(doingPage.getSiteCode(), 1, presellUrl, presellUrl);
				projectInfoPage.setReferer(doingPage.getFinalUrl());
				projectInfoPage.getMetaMap().put("presellId", Arrays.asList(presellId));
				projectInfoPage.getMetaMap().put("presellCardName", Arrays.asList(presellName));
				projectInfoPage.getMetaMap().put("projectName", Arrays.asList(projectName));
				projectInfoPage.getMetaMap().put("region", Arrays.asList(region));
				projectInfoPage.getMetaMap().put("developerName", Arrays.asList(developerName));
				presellInfoQueue.push(projectInfoPage);
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
}
