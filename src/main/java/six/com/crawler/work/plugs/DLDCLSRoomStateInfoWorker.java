package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.select.Elements;

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
public class DLDCLSRoomStateInfoWorker extends AbstractCrawlWorker{
	
	WorkSpace<Page> roomInfoQueue;
	
	Map<String,String> roomStatesMap=null;
	
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		roomInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_ls_room_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		if(roomStatesMap==null){//初始化状态信息
			roomStatesMap=new HashMap<String,String>();
			Elements stateElements=doingPage.getDoc().getElementsByTag("fjztmc");
			Elements colorElements=doingPage.getDoc().getElementsByTag("color");
			for (int i = 0; i < stateElements.size(); i++) {
				String value=stateElements.get(i).ownText();
				String key=colorElements.get(i).ownText();
				roomStatesMap.put(key, value);
			}
		}
		
		Elements roomIds=doingPage.getDoc().select("ID");
		Elements roomStates=doingPage.getDoc().select("BackColor");
		Elements wlcElements=doingPage.getDoc().select("Wlc");
		List<String> colors=new ArrayList<String>();
		List<String> actualLayers=new ArrayList<String>();
		List<String> houseIds=new ArrayList<String>();
		List<String> projectIds=new ArrayList<String>();
		List<String> unitIds=new ArrayList<String>();
		String projectId=doingPage.getMeta("projectId").get(0);
		String unitId=doingPage.getMeta("unitId").get(0);
		
		
		for (int i = 0; i < roomStates.size(); i++) {
			String roomid=roomIds.get(i).ownText();
			if(null!=roomid && !roomid.isEmpty()){
				String color=roomStates.get(i).ownText();
				if(roomStatesMap.containsKey(color)){
					colors.add(roomStatesMap.get(color));
				}
				String actualLayer=wlcElements.get(i).ownText();
				actualLayers.add(actualLayer);
				houseIds.add(roomid);
			}
			projectIds.add(projectId);
			unitIds.add(unitId);
		}
		doingPage.getMetaMap().put("projectId", projectIds);
		doingPage.getMetaMap().put("unitId", unitIds);
		doingPage.getMetaMap().put("state", colors);
		doingPage.getMetaMap().put("actualLayer",actualLayers);
		doingPage.getMetaMap().put("houseId", houseIds);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> roomIds=resultContext.getExtractResult("houseId");
		for (String roomId:roomIds) {
			String url="http://218.25.171.244/InfoLayOut_LS/Config/LoadProcToXML.aspx?pid=Arty_ROOMINFO&csnum=1&cn1=fwid&cv1="+roomId;
			
			Page roomInfo = new Page(getSite().getCode(), 1, url, url);
			roomInfo.setReferer(doingPage.getFinalUrl());
			roomInfo.setMethod(HttpMethod.GET);
			roomInfo.setType(PageType.XML.value());
			
			roomInfo.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
			roomInfo.getMetaMap().put("unitId", doingPage.getMeta("unitId"));
			roomInfo.getMetaMap().put("roomId", ArrayListUtils.asList(roomId));
			roomInfo.getMetaMap().put("actualLayer", doingPage.getMeta("actualLayer"));
			
			roomInfoQueue.push(roomInfo);
		}
	}
}
