package six.com.crawler.work.plugs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class BJJSPreSalePorjectListWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(BJJSPreSalePorjectListWorker.class);

	private static final String PROJECT_LIST_URL = "http://www.bjjs.gov.cn/eportal/ui?pageId=307670";
	private String pageCountCss = "a[class=pagingNormal]:contains(尾页)";
	WorkSpace<Page> projectInfoQueue;

	// 第一页从1开始
	int pageIndex = 1;
	int pageCount = -1;
	int pageSize = 15;

	String refererUrl;

	private Page buildPage(int pageIndex, String refererUrl) {
		Page page = new Page(getSite().getCode(), 1, PROJECT_LIST_URL, PROJECT_LIST_URL);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.POST);
		page.getParameters().put("currentPage", pageIndex);
		page.getParameters().put("pageSize", pageSize);
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("bjjs_gov_project_info", Page.class);
		Page firstPage = buildPage(pageIndex, refererUrl);// 初始化第一页
		getWorkSpace().clearDoing();
		getWorkSpace().push(firstPage);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		if (pageCount == -1) {
			Element pageCountElement = doingPage.getDoc().select(pageCountCss).first();
			if (null == pageCountElement) {
				getAndSetState(WorkerLifecycleState.STOPED);
				log.error("did not find pageCount element:" + pageCountCss);
			} else {
				// document.getElementById('currentPage').value=14;document.getElementById('currentPage').form.submit();
				String onclick = pageCountElement.attr("onclick");
				String pageCountStr = StringUtils.substringBetween(onclick, ".value=", ";document");
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
		List<String> projectInfoUrls = resultContext.getExtractResult("bjjs_gov_project_url");
		if (null != projectInfoUrls) {
			for (String projectInfoUrl : projectInfoUrls) {
				Page projectInfo = new Page(getSite().getCode(), 1, projectInfoUrl, projectInfoUrl);
				projectInfo.setReferer(doingPage.getFinalUrl());
				projectInfoQueue.push(projectInfo);
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

	@Override
	protected boolean insideOnError(Exception e, Page doingPage) {
		return false;
	}
}
