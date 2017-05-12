package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCSQProjectListWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(DLDCSQProjectListWorker.class);

	WorkSpace<Page> projectInfoQueue;
	String pageCountCss = "font[color='#0033FF']";
	int pageIndex = 1;
	int pageCount = -1;
	String PROJECT_LIST_URL = "http://www.dlfd.gov.cn/fdc/D01XmxxAction.do?Control=select";
	String refererUrl;

	private Page buildPage(int pageIndex, String refererUrl) {
		Page page = new Page(getSite().getCode(), 1, refererUrl, refererUrl);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.POST);
		Map<String,Object> params=new HashMap<String,Object>();
		params.put("pageNo", pageIndex);
		page.setParameters(params);
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_sq_project_info", Page.class);
		if (!(helper.isDownloadState() && helper.isUseRawData())) {
			Page firstPage = buildPage(pageIndex, PROJECT_LIST_URL);// 初始化第一页
			getWorkSpace().clearDoing();
			getWorkSpace().push(firstPage);
		}
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
				String onclick = pageCountElement.ownText();
				String pageCountStr = StringUtils.substringBetween(onclick, "共", "页");
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		List<String> districts=new ArrayList<String>();
		Elements trElements=doingPage.getDoc().select("table[bgcolor='#78abf1']>tbody>tr:gt(0)");
		for (Element tr:trElements) {
			Element district=tr.select("td[align='left']").first();
			districts.add(district.ownText());
		}
		List<String> projectInfoUrls = resultContext.getExtractResult("projectUrl");
		List<String> projectIds = resultContext.getExtractResult("projectId");
		if (null != projectInfoUrls) {
			for (int i = 0; i < projectInfoUrls.size(); i++) {
				String projectInfoUrl="http://www.dlfd.gov.cn"+projectInfoUrls.get(i);
				Page projectInfo = new Page(getSite().getCode(), 1, projectInfoUrl, projectInfoUrl);
				projectInfo.setReferer(doingPage.getFinalUrl());
				projectInfo.getMetaMap().put("projectId",ArrayListUtils.asList(projectIds.get(i)));
				projectInfo.getMetaMap().put("district", ArrayListUtils.asList(districts.get(i)));
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

}
