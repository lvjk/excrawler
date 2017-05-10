package six.com.crawler.work.plugs;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCGXProjectInfoWorker extends AbstractCrawlWorker{
	
	WorkSpace<Page> unitInfoQueue;

	@Override
	protected void insideInit() {
		
		unitInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_gx_unit_info", Page.class);
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
		String cv1=doingPage.getMeta("projectId").get(0);
		String URL="http://218.25.171.244/InfoLayOut_GX/Config/LoadProcToXML.aspx?pid=Arty_XSLPXX&csnum=3&cn1=ysxkid&cv1="+cv1+"&cn2=PageIndex&cv2=1&cn3=PageSize&cv3=10000";
		
		Page unitInfoPage = new Page(getSite().getCode(), 1, URL, URL);
		unitInfoPage.setReferer(doingPage.getFinalUrl());
		unitInfoPage.setMethod(HttpMethod.GET);
		unitInfoPage.setType(PageType.XML.value());
		
		unitInfoPage.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
		
		unitInfoQueue.push(unitInfoPage);
	}
}
