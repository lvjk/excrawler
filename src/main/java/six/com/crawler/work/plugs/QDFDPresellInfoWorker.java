package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

public class QDFDPresellInfoWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(QDFDPresellInfoWorker.class);

	WorkSpace<Page> projectUnitUrlQueue;

	private String unitInfoUrlCss = "ul[class=kpdy_bg]>table:eq(1)>tbody>tr>td>a";

	private static final String BASE_URL = "http://www.qdfd.com.cn/qdweb/realweb/fh/FhBuildingList.jsp";

	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		projectUnitUrlQueue = getManager().getWorkSpaceManager().newWorkSpace("qdfd_unit_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		Elements urlElement = doingPage.getDoc().select(unitInfoUrlCss);
		if (null == urlElement) {
			return;
		}

		// FhProjectBird.jsp?projectID=2096&projectName=银盛泰
		// 德郡四期&presell_id=3321&predesc=青房注字(城13)第37
		for (Element url : urlElement) {
			String presell_id = StringUtils.substringBetween(url.attr("href"), "javascript:getBuilingList(\"", "\",\"");
			String predesc = StringUtils.substringBetween(url.attr("href"), "\",\"", "\")");
			String pageUrl = BASE_URL + "?preid=" + presell_id;
			Page page = new Page(doingPage.getSiteCode(), 1, pageUrl, pageUrl);
			page.setMethod(HttpMethod.GET);
			page.setReferer(doingPage.getFinalUrl());
			page.getMetaMap().put("projectId", doingPage.getMetaMap().get("projectId"));
			page.getMetaMap().put("projectName", doingPage.getMetaMap().get("projectName"));
			page.getMetaMap().put("preId", ArrayListUtils.asList(presell_id));
			page.getMetaMap().put("preDesc", ArrayListUtils.asList(predesc));
			projectUnitUrlQueue.push(page);
		}
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub

	}

}
