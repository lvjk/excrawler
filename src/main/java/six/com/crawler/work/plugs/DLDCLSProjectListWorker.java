package six.com.crawler.work.plugs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

public class DLDCLSProjectListWorker extends AbstractCrawlWorker{
	
	RedisWorkSpace<Page> projectInfoQueue;
	
	String pageIndexTemplate = "<<pageIndex>>";
	
	int pageIndex = 1;
	
	int pageCount;
	
	String projectListUrlTemplate="http://218.25.171.244/InfoLayOut_LS/Config/LoadProcToXML.aspx?pid=Arty_YSXX&csnum=2&cn1=pageindex&cv1="+pageIndexTemplate+"&cn2=DefOrderfldName&cv2=xkzh";

	String refererUrl;

	private Page buildPage(int pageIndex, String refererUrl) {
		String url=StringUtils.replace(projectListUrlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		Page page = new Page(getSite().getCode(), 1, url, url);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.POST);
		page.setType(PageType.XML.value());
		page.getParameters().put("pid", "Arty_YSXX");
		page.getParameters().put("cv1", pageIndex);
		page.getParameters().put("cv2", "xkzh");
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(),"dldc_ls_project_info", Page.class);
		Page firstPage = buildPage(pageIndex, refererUrl);// 初始化第一页
		getWorkQueue().clearDoing();
		getWorkQueue().push(firstPage);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		List<String> projectInfoUrls = resultContext.getExtractResult("dldc_ls_project_info");
		if (null != projectInfoUrls) {
			for (String projectInfoUrl : projectInfoUrls) {
				Page projectInfo = new Page(getSite().getCode(), 1, projectInfoUrl, projectInfoUrl);
				projectInfo.setReferer(doingPage.getFinalUrl());
				projectInfo.setMethod(HttpMethod.GET);
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
