package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

public class QDFDRoomStateWorker extends AbstractCrawlWorker{
	
	final static Logger log = LoggerFactory.getLogger(QDFDRoomStateWorker.class);

	private String roomCss="table[class=px12]>tbody>tr[id=submenu1]>td[class=tableBorder3]";
	
	private Map<String,String> roomStates = new HashMap<String,String>();
	
	RedisWorkSpace<Page> roomInfoQueue;
	
	private static final String BASE_URL="http://www.qdfd.com.cn/qdweb/realweb/fh/FhHouseDetail.jsp";
	
	@Override
	protected void insideInit() {
		roomInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(),"qdfd_room_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String roomStateCss="div[class=housestatus_bg] td[style=padding:15] table:eq(0) font";
		if(roomStates.isEmpty()){
			Elements stateElements=doingPage.getDoc().select(roomStateCss);
			if(null==stateElements){
				throw new RuntimeException("don't find state node:" + roomStateCss);
			}
			
			for (Element state:stateElements) {
				String key=state.attr("color");
				String value=state.ownText();
				value=StringUtils.remove(value,":");
				value=StringUtils.remove(value," ");
				roomStates.put(key, value);
			}
		}
		Elements roomStatus=doingPage.getDoc().select(roomCss);
		for (Element state:roomStatus) {
			String key=state.attr("bgcolor");
			if(roomStates.containsKey(key)){
				doingPage.getMetaMap().put("projectId", doingPage.getMetaMap().get("projectId"));
				doingPage.getMetaMap().put("projectName", doingPage.getMetaMap().get("projectName"));
				doingPage.getMetaMap().put("preId", doingPage.getMetaMap().get("preId"));
				doingPage.getMetaMap().put("preDesc", doingPage.getMetaMap().get("preDesc"));
				doingPage.getMetaMap().put("buildId", doingPage.getMetaMap().get("buildId"));
				doingPage.getMetaMap().put("roomNum", ArrayListUtils.asList(state.ownText().trim()));
				doingPage.getMetaMap().put("startId", doingPage.getMetaMap().get("startId"));
				
				Element url=state.select("a").first();
				if(url!=null){
					String houseId=StringUtils.substringBetween(url.attr("href"), "javascript:houseDetail('", "')");
					String pageUrl=BASE_URL+"?houseID="+houseId;
					Page page=new Page(doingPage.getSiteCode(), 1, pageUrl, pageUrl);
					page.setMethod(HttpMethod.GET);
					page.setReferer(doingPage.getFinalUrl());
					page.getMetaMap().put("projectId", doingPage.getMetaMap().get("projectId"));
					page.getMetaMap().put("projectName", doingPage.getMetaMap().get("projectName"));
					page.getMetaMap().put("preId", doingPage.getMetaMap().get("preId"));
					page.getMetaMap().put("preDesc", doingPage.getMetaMap().get("preDesc"));
					page.getMetaMap().put("buildId", doingPage.getMetaMap().get("buildId"));
					page.getMetaMap().put("startId", doingPage.getMetaMap().get("startId"));
					page.getMetaMap().put("houseId",ArrayListUtils.asList(houseId));
					doingPage.getMetaMap().put("houseId", ArrayListUtils.asList(houseId));
					roomInfoQueue.push(page);
				}else{
					doingPage.getMetaMap().put("houseId", ArrayListUtils.asList(""));
				}
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		
	}
}
