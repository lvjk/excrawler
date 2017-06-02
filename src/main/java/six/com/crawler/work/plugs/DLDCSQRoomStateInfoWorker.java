package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
public class DLDCSQRoomStateInfoWorker extends AbstractCrawlWorker{

	
	Map<String,String> roomState=new HashMap<String,String>();
	@Override
	protected void insideInit() {
		roomState.put("black", "可售");
		roomState.put("#00CC00", "已售");
		roomState.put("blue", "已被开发企业抵押给金融机构，暂不可售");
		roomState.put("red", "不可售（因超建、查封、物业用房、回迁安置等原因）");
		
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {

		String projectId=doingPage.getMeta("projectId").get(0);
		String unitId=doingPage.getMeta("unitId").get(0);
		List<String> floors=new ArrayList<String>();
		List<String> projectIds = new ArrayList<String>();
		List<String> unitIds= new ArrayList<String>();
		List<String> states=new ArrayList<String>();
		List<String> purposes = new ArrayList<String>();
		List<String> houseTypes= new ArrayList<String>();
		List<String> buildAreas=new ArrayList<String>();
		List<String> houseAreas=new ArrayList<String>();
		List<String> roomNos=new ArrayList<String>();
		
		
		Elements floorElements=doingPage.getDoc().select("table[class=table_lb1]>tbody>tr");
		for (Element floorElement:floorElements) {
			String floor=floorElement.select("td[bgcolor='#EFF7F7']").first().ownText();
			Elements rooms=floorElement.select("td[bgcolor='fafafa']");
			for (int i = 0; i < rooms.size(); i++) {
				Element room=rooms.get(i);
				if(!room.html().replace("&nbsp;","").isEmpty()){
					String detailStr=room.attr("title");
					String purpose=StringUtils.substringBetween(detailStr, "房屋用途:", "房屋类型:");
					String houseType=StringUtils.substringBetween(detailStr, "房屋类型:", "建筑面积:");
					String buildArea=StringUtils.substringBetween(detailStr, "建筑面积:", "套内面积:");
					String houseArea=StringUtils.substringAfter(detailStr, "套内面积:");
					
					purposes.add(purpose);
					houseTypes.add(houseType);
					buildAreas.add(buildArea);
					houseAreas.add(houseArea);
					String state=room.select("font").first().attr("color");
					String roomNo=room.select("font").first().ownText();
					
					if(null != roomState.get(state)){
						states.add(roomState.get(state));
					}else{
						states.add("其他");
					}
					
					roomNos.add(roomNo);
					
					floors.add(floor);
					projectIds.add(projectId);
					unitIds.add(unitId);
				}
			}
		}
		
		doingPage.getMetaMap().put("projectId", projectIds);
		doingPage.getMetaMap().put("state", states);
		doingPage.getMetaMap().put("unitId", unitIds);
		doingPage.getMetaMap().put("purpose", purposes);
		doingPage.getMetaMap().put("houseType", houseTypes);
		doingPage.getMetaMap().put("buildArea", buildAreas);
		doingPage.getMetaMap().put("houseArea", houseAreas);
		doingPage.getMetaMap().put("roomNo", roomNos);
		
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
