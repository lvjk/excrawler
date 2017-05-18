package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class ZQFGZXRoomStateInfoWorker extends AbstractCrawlWorker{
	
	Map<String,String> roomState=new HashMap<String,String>();

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
		
		String unitId=null;
		if(doingPage.getMeta("unitId")==null||doingPage.getMeta("unitId").size()>0){
			unitId=doingPage.getMeta("unitId").get(0);
		}else{
			throw new RuntimeException("don't find unitId,meta is :"+doingPage.getMetaMap().toString());
		}
				
		
		Elements floors=doingPage.getDoc().select("ul[class=RoomState]");
		for (Element floor:floors) {
			Elements rooms=floor.select("li:gt(0)");
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
			}
		}
		
		doingPage.getMetaMap().put("unitId", unitIds);
		doingPage.getMetaMap().put("layer", layers);
		doingPage.getMetaMap().put("preBuildArea", preBuildAreas);
		doingPage.getMetaMap().put("preShareArea", preShareAreas);
		doingPage.getMetaMap().put("preInnerArea", preInnerAreas);
		doingPage.getMetaMap().put("actBuildArea", actBuildAreas);
		doingPage.getMetaMap().put("actShareArea", actShareAreas);
		doingPage.getMetaMap().put("actInnerArea", actInnerAreas);
		
		doingPage.getMetaMap().put("roomNo", roomNos);
		doingPage.getMetaMap().put("state", roomStates);
		doingPage.getMetaMap().put("realEstateName", realEstates);
		doingPage.getMetaMap().put("unitName", realEstateNames);
		doingPage.getMetaMap().put("developer", developers);
		doingPage.getMetaMap().put("roomType", roomTypes);
		doingPage.getMetaMap().put("roomProperty", roomProperties);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

}
