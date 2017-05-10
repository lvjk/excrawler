package six.com.crawler.work.plugs;

import java.util.List;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCGXRoomStateInfoWorker extends AbstractCrawlWorker{
	
	WorkSpace<Page> roomInfoQueue;
	
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		roomInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_gx_room_info", Page.class);
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
		//http://218.25.171.244/InfoLayOut_GX/Config/LoadProcToXML.aspx?pid=Arty_ROOMINFO&csnum=1&cn1=fwid&cv1=c57260f21928478490ad12aef66bce88
		List<String> roomIds=resultContext.getExtractResult("roomId");
		for (String roomId:roomIds) {
			String url="http://218.25.171.244/InfoLayOut_GX/Config/LoadProcToXML.aspx?pid=Arty_ROOMINFO&csnum=1&cn1=fwid&cv1="+roomId;
			
			Page roomInfo = new Page(getSite().getCode(), 1, url, url);
			roomInfo.setReferer(doingPage.getFinalUrl());
			roomInfo.setMethod(HttpMethod.GET);
			roomInfo.setType(PageType.XML.value());
			
			roomInfo.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
			roomInfo.getMetaMap().put("unitId", doingPage.getMeta("unitId"));
			roomInfo.getMetaMap().put("roomId", ArrayListUtils.asList(roomId));
			
			roomInfoQueue.push(roomInfo);
		}
	}
}
