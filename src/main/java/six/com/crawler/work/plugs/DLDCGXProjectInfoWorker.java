package six.com.crawler.work.plugs;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

public class DLDCGXProjectInfoWorker extends AbstractCrawlWorker{

	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		
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
		String BASE_URL="http://218.25.171.244/InfoLayOut_GX/Config/LoadProcToXML.aspx?pid=Arty_XSLPXX&csnum=3&cn1=ysxkid&cv1=ea51e7784ff64494b227cadca087958b&cn2=PageIndex&cv2=1&cn3=PageSize&cv3=10000";
		
	}
}
