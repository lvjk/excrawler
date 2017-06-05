package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.exception.ProcessWorkerCrawlerException;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class ZQFGZXUnitInfoWorker extends AbstractCrawlWorker{

	WorkSpace<Page> roomStateInfoQueue;
	
	@Override
	protected void insideInit() {
		roomStateInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("zqfgzx_room_state_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String attrCss="dl[class=dl-horizontal buildinginfo]:eq(0)>dd";
		Elements attrs=doingPage.getDoc().select(attrCss);
		if(attrs.size()<2){
			throw new ProcessWorkerCrawlerException("don't find state node:" + attrCss+",pageSrc is :"+doingPage.getPageSrc());
		}else{
			Element developer=attrs.get(2);
			doingPage.getMetaMap().put("developer", ArrayListUtils.asList(developer.ownText()));
			Element buildName=attrs.get(1);
			doingPage.getMetaMap().put("unitName", ArrayListUtils.asList(buildName.ownText()));
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		Page roomStatePage=new Page(doingPage.getSiteCode(), 1, doingPage.getFinalUrl(),  doingPage.getFinalUrl());
		roomStatePage.setReferer(doingPage.getReferer());
		roomStatePage.setNoNeedDown(1);
		roomStatePage=parseRoomStateInfo(roomStatePage,doingPage);
		roomStateInfoQueue.push(roomStatePage);
	}
	
	private Page parseRoomStateInfo(Page nextPage,Page doingPage){
		List<String> unitIds=new ArrayList<String>();
		List<String> layers=new ArrayList<String>();
		List<String> roomNos=new ArrayList<String>();
		List<String> realEstates = new ArrayList<String>();
		List<String> roomTypes = new ArrayList<String>();
		List<String> realEstateNames = new ArrayList<String>();
		List<String> preBuildAreas=new ArrayList<String>();
		List<String> preShareAreas=new ArrayList<String>();
		List<String> preInnerAreas=new ArrayList<String>();
		List<String> roomProperties=new ArrayList<String>();
		List<String> actBuildAreas=new ArrayList<String>();
		List<String> actShareAreas=new ArrayList<String>();
		List<String> actInnerAreas=new ArrayList<String>();
		List<String> developers=new ArrayList<String>();
		List<String> roomStates=new ArrayList<String>();
		List<String> roomSequences=new ArrayList<String>();
		
		String unitId=null;
		if(doingPage.getMeta("unitId")!=null&&doingPage.getMeta("unitId").size()>0){
			unitId=doingPage.getMeta("unitId").get(0);
		}else{
			throw new ProcessWorkerCrawlerException("don't find unitId,meta is :"+doingPage.getMetaMap().toString());
		}
		
		Elements floors=doingPage.getDoc().select("ul[class=RoomState]");
		if(null!=floors&&floors.size()>0){
			for (Element floor:floors) {
				Elements rooms=floor.select("li:gt(0)");
				int seqiemceIndex=1;
				for (Element room:rooms) {
					Elements attrs=room.select("dl[class='loupan-info']>dd");
					String realEstate=attrs.get(0).select("a").first().ownText();
					String roomNo=attrs.get(1).ownText();
					String roomType=attrs.get(2).ownText();
					String roomProperty=attrs.get(8).ownText();
					String layer=attrs.get(7).ownText();
					
					String realEstateName=attrs.get(6).select("a").first().ownText();
					String preBuildArea=attrs.get(3).ownText();
					String preShareArea=attrs.get(4).ownText();
					String preInnerArea=attrs.get(5).ownText();
					
					String state=attrs.get(9).ownText();
					String actBuildArea=attrs.get(10).ownText();
					String actShareArea=attrs.get(11).ownText();
					String actInnerArea=attrs.get(12).ownText();
					String developer=attrs.get(13).ownText();
					
					preBuildAreas.add(preBuildArea);
					preShareAreas.add(preShareArea);
					preInnerAreas.add(preInnerArea);
					
					actBuildAreas.add(actBuildArea);
					actShareAreas.add(actShareArea);
					actInnerAreas.add(actInnerArea);
					developers.add(developer);
					roomProperties.add(roomProperty);
					roomTypes.add(roomType);
					realEstateNames.add(realEstateName);
					realEstates.add(realEstate);
					roomStates.add(state);
					roomNos.add(roomNo);
					layers.add(layer);
					unitIds.add(unitId);
					roomSequences.add(layer+"-"+seqiemceIndex);
					seqiemceIndex++;
				}
			}
			
			nextPage.getMetaMap().put("unitId", unitIds);
			nextPage.getMetaMap().put("layer", layers);
			nextPage.getMetaMap().put("preBuildArea", preBuildAreas);
			nextPage.getMetaMap().put("preShareArea", preShareAreas);
			nextPage.getMetaMap().put("preInnerArea", preInnerAreas);
			nextPage.getMetaMap().put("actBuildArea", actBuildAreas);
			nextPage.getMetaMap().put("actShareArea", actShareAreas);
			nextPage.getMetaMap().put("actInnerArea", actInnerAreas);
			
			nextPage.getMetaMap().put("roomNo", roomNos);
			nextPage.getMetaMap().put("state", roomStates);
			nextPage.getMetaMap().put("realEstateName", realEstates);
			nextPage.getMetaMap().put("unitName", realEstateNames);
			nextPage.getMetaMap().put("developer", developers);
			nextPage.getMetaMap().put("roomType", roomTypes);
			nextPage.getMetaMap().put("roomProperty", roomProperties);
			nextPage.getMetaMap().put("roomSequence", roomSequences);
		}else{
			throw new RuntimeException("don't find state node: ul[class=RoomState]; pageSrc is :" +doingPage.getPageSrc());
		}
		return nextPage;
	}
}
