package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
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
		
		for (Element trElement:trElements) {
			String floorNumber=trElement.select("span[class=XkbWlc]").first().ownText();
			String nominalLevel=trElement.select("span[class=XkbCeng]").first().ownText();
			
			Elements roomStatus=trElement.select("span[class=XkbRoom]");
			for (Element roomState:roomStatus) {
				String color=roomState.attr("style");
				
				if(roomStates.containsKey(color)){
					doingPage.getMetaMap().put("roomState", ArrayListUtils.asList(roomStates.get(color)));
				}
				
				String roomNo=roomState.ownText();
				String detail=roomState.attr("title");
				
				doingPage.getMetaMap().put("roomNo", ArrayListUtils.asList(roomNo));
				doingPage.getMetaMap().put("floorNumber", ArrayListUtils.asList(floorNumber));
				doingPage.getMetaMap().put("nominalLevel", ArrayListUtils.asList(nominalLevel));
				doingPage.getMetaMap().put("detail", ArrayListUtils.asList(detail));
				
			}
		}
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
