package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCLSProjectListWorker extends AbstractCrawlWorker {

	WorkSpace<Page> projectInfoQueue;

	String pageIndexTemplate = "<<pageIndex>>";

	int pageIndex = 1;

	int pageCount = -1;

	String projectListUrlTemplate = "http://218.25.171.244/InfoLayOut_LS/Config/LoadProcToXML.aspx?pid=Arty_YSXX&csnum=2&cn1=pageindex&cv1="
			+ pageIndexTemplate + "&cn2=DefOrderfldName&cv2=xkzh";

	String refererUrl;

	private Page buildPage(int pageIndex, String refererUrl) {
		String url = StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		Page page = new Page(getSite().getCode(), 1, url, url);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.POST);
		page.setType(PageType.XML.value());
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("pid", "Arty_YSXX");
		param.put("cv1", pageIndex);
		param.put("cv2", "xkzh");
		page.setParameters(param);
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_ls_project_info", Page.class);
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
			pageCount = Integer.parseInt(doingPage.getDoc().getElementsByTag("PageCount").first().ownText());
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String BASE_URL = "http://218.25.171.244/InfoLayOut_LS/Config/LoadProcToXML.aspx?pid=Arty_YSXX&csnum=1&cn1=ysxkid&cv1=";
		List<String> projectIds = resultContext.getExtractResult("projectId");
		if (null != projectIds) {
			for (String projectId : projectIds) {
				String URL = BASE_URL + projectId;
				Page projectInfo = new Page(getSite().getCode(), 1, URL, URL);
				projectInfo.setReferer(doingPage.getFinalUrl());
				projectInfo.setMethod(HttpMethod.GET);
				projectInfo.setType(PageType.XML.value());
				projectInfo.getMetaMap().put("projectId", ArrayListUtils.asList(projectId));
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
