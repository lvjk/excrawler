package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.space.WorkSpace;

public class QDFDProjectInfoWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(QDFDProjectInfoWorker.class);

	private String pageCountCss = "ul[class=list]>table:eq(2)>tbody>tr>td>a";

	String pageIndexTemplate = "<<pageIndex>>";
	private String urlTemplate = "http://www.qdfd.com.cn/qdweb/realweb/fh/FhProjectQuery.jsp?regionFirstID=&houseTypeFirst=&houseArea=&selState=&averagePriceLow=&"
			+ "projectName=+&projectAddr=%CF%EE%C4%BF%B5%D8%D6%B7&imageField2.x=21&imageField2.y=11&page="
			+ pageIndexTemplate + "&rows=20&okey=&order=";

	private static final String BASE_URL = "http://www.qdfd.com.cn/qdweb/realweb/fh/FhProjectInfo.jsp";

	WorkSpace<Page> projectInfoQueue;
	// 第一页从1开始
	int pageIndex = 1;
	int pageCount = -1;
	String refererUrl;

	private Page buildPage(int pageIndex, String refererUrl) {
		String pageUrl = StringUtils.replace(urlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		Page page = new Page(getSite().getCode(), 1, pageUrl, pageUrl);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.GET);
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("qdfd_presell_info", Page.class);
		Page firstPage = buildPage(pageIndex, refererUrl);// 初始化第一页
		getWorkSpace().clearDoing();
		getWorkSpace().push(firstPage);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		if (pageCount == -1) {
			Element pageCountElement = doingPage.getDoc().select(pageCountCss).first();
			if (null == pageCountElement) {
				getAndSetState(WorkerLifecycleState.STOPED);
				log.error("did not find pageCount element:" + pageCountCss);
			} else {
				String onclick = pageCountElement.attr("onclick");
				String pageCountStr = StringUtils.substringBetween(onclick, "GoToPage(", ")");
				try {
					pageCount = Integer.valueOf(pageCountStr);
				} catch (Exception e) {
					getAndSetState(WorkerLifecycleState.STOPED);
					log.error("get pageCount string:" + pageCountStr, e);
				}
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

		Elements urls = doingPage.getDoc().select("ul[class=list]>table:eq(1)>tbody>tr");

		for (int i = 1; i < urls.size(); i++) {
			Element url = urls.get(i);
			String state = url.select("td").first().ownText();
			if (state.equals("在售")) {
				String href = url.select("td").get(1).getElementsByTag("a").attr("href");
				String proid = StringUtils.substringBetween(href, "javascript:detailProjectInfo(\"", "\")");
				Page unitListPage = new Page(getSite().getCode(), 1, BASE_URL, BASE_URL);
				unitListPage.setReferer(doingPage.getFinalUrl());
				unitListPage.setMethod(HttpMethod.POST);
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("projectID", proid);
				unitListPage.setParameters(params);
				unitListPage.getMetaMap().put("projectId", ArrayListUtils.asList(proid));
				unitListPage.getMetaMap().put("projectName",
						ArrayListUtils.asList(url.select("td").get(1).getElementsByTag("a").text()));
				projectInfoQueue.push(unitListPage);
			}
		}
		// 判断是否还有下一页 有下一页生成下一页丢进当前队列 即可
		pageIndex++;
		if (pageIndex <= pageCount) {
			Page page = buildPage(pageIndex, doingPage.getFinalUrl());// 初始化第一页
			getWorkSpace().push(page);
		}
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {

	}
}
