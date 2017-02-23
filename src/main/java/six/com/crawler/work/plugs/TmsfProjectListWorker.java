package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月21日 下午2:20:30
 */
public class TmsfProjectListWorker extends AbstractCrawlWorker {

	String siteidFlag = "<<siteid>>";
	String propertyidFlag = "<<propertyid>>";
	String projectUrilTemplate = "/newhouse/property_" + siteidFlag + "_" + propertyidFlag + "_info.htm";
	String projectDivCss = "div[class=build_txt line26]";
	RedisWorkQueue projectInfoQueue;
	String pageCountCss = "div[class=pagenuber_info]>font:eq(1)";
	String pageIndexTemplate = "<<pageIndex>>";
	String urlTemplate = "http://www.tmsf.com/newhouse/" + "property_searchall.htm?" + "searchkeyword=&" + "keyword=&"
			+ "sid=&" + "districtid=&" + "areaid=&" + "dealprice=&" + "propertystate=&" + "propertytype=&"
			+ "ordertype=&" + "priceorder=&" + "openorder=&" + "view720data=&" + "page=" + pageIndexTemplate + "&"
			+ "bbs=&" + "avanumorder=&" + "comnumorder=";
	int pageIndex = 1;
	int pageCount = -1;
	String refererUrl;

	public TmsfProjectListWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	private Page buildPage(int pageIndex, String refererUrl) {
		String pageUrl = StringUtils.replace(urlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		Page page = new Page(getSite().getCode(), 1, pageUrl, pageUrl);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.GET);
		page.setType(PageType.LISTING.value());
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_project_info");
		Page firstPage = buildPage(pageIndex, refererUrl);// 初始化第一页
		getWorkQueue().clear();
		getWorkQueue().push(firstPage);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		if (pageCount == -1) {
			Element pageCountElement = doc.select(pageCountCss).first();
			String pageCountElementText = pageCountElement.text();
			String[] pageCountParams = StringUtils.split(pageCountElementText, "/");
			String pageCountStr = pageCountParams[1];
			pageCount = Integer.valueOf(pageCountStr);
		}
		Elements projectDivElements = doc.select(projectDivCss);
		for (Element projecrDivElement : projectDivElements) {
			String onclick = projecrDivElement.attr("onclick");
			onclick = StringUtils.substringBetween(onclick, "toPropertyInfo(", ")");
			String[] params = StringUtils.split(onclick, ",");
			String projectUrl = StringUtils.replace(projectUrilTemplate, siteidFlag, params[0]);
			projectUrl = StringUtils.replace(projectUrl, propertyidFlag, params[1]);
			projectUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), projectUrl);
			Page projectPage = new Page(doingPage.getSiteCode(), 1, projectUrl, projectUrl);
			projectPage.setReferer(doingPage.getFinalUrl());
			projectPage.setType(PageType.DATA.value());
			projectInfoQueue.push(projectPage);
		}
		pageIndex++;
		if (pageIndex > pageCount) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
		} else {
			Page page = buildPage(pageIndex, doingPage.getFinalUrl());// 初始化第一页
			getWorkQueue().push(page);
		}

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	@Override
	protected void onComplete(Page doingPage) {

	}

}
