package six.com.crawler.work.plugs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCJZProjectListWorker extends AbstractCrawlWorker{
	
	final static Logger log = LoggerFactory.getLogger(DLDCJZProjectListWorker.class);
	
	RedisWorkSpace<Page> projectInfoQueue;
	String pageCountCss = "input[name='pageNo']";
	int pageIndex = 1;
	int pageCount=-1;
	String PROJECT_LIST_URL = "http://www.fczw.cn/ysxkzList.xhtml?method=doQuery";
	String refererUrl;
	
	private Page buildPage(int pageIndex, String refererUrl) {
		Page page = new Page(getSite().getCode(), 1, PROJECT_LIST_URL, PROJECT_LIST_URL);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.POST);
		page.getParameters().put("currentPage", pageIndex);
		page.getParameters().put("pageSize", 10);
		return page;
	}
	
	@Override
	protected void insideInit() {
		projectInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(),"dldc_jz_project_info", Page.class);
		if(!(helper.isDownloadState() && helper.isUseRawData())){
			Page firstPage = buildPage(pageIndex, refererUrl);// 初始化第一页
			getWorkQueue().clearDoing();
			getWorkQueue().push(firstPage);
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
				String pageCountStr = StringUtils.substringBetween(onclick, "第1/", "页");
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
		List<String> projectInfoUrls = resultContext.getExtractResult("projectUrl");
		if (null != projectInfoUrls) {
			for (String projectInfoUrl : projectInfoUrls) {
				Page projectInfo = new Page(getSite().getCode(), 1, projectInfoUrl, projectInfoUrl);
				projectInfo.setReferer(doingPage.getFinalUrl());
				
				String projId=projectInfoUrl.substring(projectInfoUrl.indexOf("ysxkid="), projectInfoUrl.length());
				projectInfo.getMetaMap().put("projectId", ArrayListUtils.asList(projId));
				projectInfoQueue.push(projectInfo);
			}
		}
		// 判断是否还有下一页 有下一页生成下一页丢进当前队列 即可
		pageIndex++;
		if (pageIndex <= pageCount) {
			Page page = buildPage(pageIndex, doingPage.getFinalUrl());// 初始化第一页
			getWorkQueue().push(page);
		}
	}

}
