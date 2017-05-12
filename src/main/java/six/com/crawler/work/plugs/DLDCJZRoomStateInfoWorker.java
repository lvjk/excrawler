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
public class DLDCJZRoomStateInfoWorker extends AbstractCrawlWorker{
	
	
	private Map<String,String> roomStates = new HashMap<String,String>();

	@Override
	protected void insideInit() {
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Elements status=doingPage.getDoc().select("span[class=XkbTl]");
		
		for (Element state:status) {
			String color=state.attr("style");
			roomStates.put(color, state.ownText());
		}
		
		Elements trElements = doingPage.getDoc().select("table[class=XkbTable]>tbody>tr:gt(0)");
		
		List<String> colors=new ArrayList<String>();
		List<String> roomNames=new ArrayList<String>();
		List<String> floorNumbers=new ArrayList<String>();
		List<String> nominalLevels=new ArrayList<String>();
		List<String> addrs=new ArrayList<String>();
		List<String> areas=new ArrayList<String>();
		
		List<String> presellIds=new ArrayList<String>();
		List<String> projectIds=new ArrayList<String>();
		List<String> unitIds=new ArrayList<String>();
		String presellId=doingPage.getMeta("presellId").get(0);
		String projectId=doingPage.getMeta("projectId").get(0);
		String unitId=doingPage.getMeta("unitId").get(0);
		
		for (Element trElement:trElements) {
			String floorNumber=trElement.select("span[class=XkbWlc]").first().ownText();
			String nominalLevel=trElement.select("span[class=XkbCeng]").first().ownText();
			
			Elements roomStatus=trElement.select("span[class=XkbRoom]");
			for (Element roomState:roomStatus) {
				String color=roomState.attr("style");
				
				if(roomStates.containsKey(color)){
					colors.add(roomStates.get(color));
				}
				
				String roomNo=roomState.ownText();
				String detail=roomState.attr("title");
				String area=StringUtils.substringBetween(detail, "面积：", "坐落：");
				String address=StringUtils.substringAfter(detail, "坐落：");
				addrs.add(address);
				areas.add(area);
				roomNames.add(roomNo);
				floorNumbers.add(floorNumber);
				nominalLevels.add(nominalLevel);
				presellIds.add(presellId);
				projectIds.add(projectId);
				unitIds.add(unitId);
			}
		}
		doingPage.getMetaMap().put("presellId",presellIds);
		doingPage.getMetaMap().put("projectId", projectIds);
		doingPage.getMetaMap().put("unitId", unitIds);
		doingPage.getMetaMap().put("roomState", colors);
		doingPage.getMetaMap().put("roomName", roomNames);
		doingPage.getMetaMap().put("floorNumber",floorNumbers);
		doingPage.getMetaMap().put("nominalLevel",nominalLevels);
		doingPage.getMetaMap().put("address", addrs);
		doingPage.getMetaMap().put("area", areas);
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
