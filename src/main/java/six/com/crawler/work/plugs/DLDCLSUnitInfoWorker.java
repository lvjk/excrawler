package six.com.crawler.work.plugs;

import java.util.List;

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
public class DLDCLSUnitInfoWorker extends AbstractCrawlWorker{

	WorkSpace<Page> roomStateQueue;
	
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		roomStateQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_ls_room_state_info", Page.class);
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
		List<String> unitIds=resultContext.getExtractResult("unitId");
		for (int i = 0; i < unitIds.size(); i++) {
			String unitId=unitIds.get(i);
			String URL="http://218.25.171.244/InfoLayOut_LS/Config/GetUrlData.aspx?url=http://192.168.10.11:81/RealtyOutside/ArtyWebPage/XmlData/XmlRoom.aspx?roomsPerRow=4&lpid="+unitId;
			Page roomStateInfo = new Page(getSite().getCode(), 1, URL, URL);
			roomStateInfo.setReferer(doingPage.getFinalUrl());
			roomStateInfo.setMethod(HttpMethod.GET);
			roomStateInfo.setType(PageType.XML.value());
			
			roomStateInfo.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
			roomStateInfo.getMetaMap().put("unitId", ArrayListUtils.asList(unitId));
			
			roomStateQueue.push(roomStateInfo);
		}
	}
}
