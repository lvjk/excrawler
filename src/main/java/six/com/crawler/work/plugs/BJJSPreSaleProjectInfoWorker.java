package six.com.crawler.work.plugs;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class BJJSPreSaleProjectInfoWorker extends AbstractCrawlWorker {

	final static Logger LOG = LoggerFactory.getLogger(BJJSPreSaleProjectInfoWorker.class);

	WorkSpace<Page> projectUnitUrlQueue;

	private String unitUrlTemplate = "http://www.bjjs.gov.cn/eportal/ui?pageId=411612&systemId=2&srcId=1";

	private String rowcountCss = "div[style=width: 710px]>div[id=\"\"]>table:eq(1)";

	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		projectUnitUrlQueue = getManager().getWorkSpaceManager().newWorkSpace("bjjs_gov_unit_url", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void beforeExtract(Page doingPage) {

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		// 转换前获取楼盘url
		Document doc = doingPage.getDoc();
		String projectId = StringUtils.substringBetween(doingPage.getFinalUrl(), "projectID=", "&systemID");
		Elements els = doc.select(rowcountCss);
		if (null != els) {
			int rowcount = Integer.parseInt(StringUtils.substringBetween(els.text(), "共有", "个楼栋信息").trim());
			String pid = resultContext.getOutResults().get(0).get(Extracter.DEFAULT_RESULT_ID);
			String url = unitUrlTemplate + "&id=" + projectId + "&rowcount=" + rowcount;
			Page unitUrlPage = new Page(getSite().getCode(), 1, url, url);
			unitUrlPage.setReferer(doingPage.getFinalUrl());
			unitUrlPage.setMethod(HttpMethod.GET);
			unitUrlPage.setType(PageType.DATA.value());
			unitUrlPage.getMetaMap().put("projectId", Arrays.asList(pid));
			projectUnitUrlQueue.push(unitUrlPage);
		}
	}

	@Override
	protected boolean insideOnError(Exception e, Page doingPage) {
		// TODO Auto-generated method stub
		return false;
	}

}
