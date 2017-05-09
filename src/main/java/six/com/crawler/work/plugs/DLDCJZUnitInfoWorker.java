package six.com.crawler.work.plugs;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCJZUnitInfoWorker  extends AbstractCrawlWorker{

	WorkSpace<Page> roomStateInfoQueue;
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		roomStateInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_jz_room_state_info", Page.class);
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
		String url=doingPage.getMeta("url").get(0);
		Page roomStateInfoPage= new Page(doingPage.getSiteCode(), 1, url, url);
		roomStateInfoPage.setReferer(doingPage.getFinalUrl());
		roomStateInfoPage.setMethod(HttpMethod.GET);
		
		roomStateInfoPage.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
		roomStateInfoPage.getMetaMap().put("projectName", doingPage.getMeta("projectName"));
		roomStateInfoPage.getMetaMap().put("lid", doingPage.getMeta("lid"));
		roomStateInfoPage.getMetaMap().put("xmid", doingPage.getMeta("xmid"));
		roomStateInfoPage.getMetaMap().put("presellCode", doingPage.getMeta("presellCode"));
		roomStateInfoQueue.push(roomStateInfoPage);
	}
}
