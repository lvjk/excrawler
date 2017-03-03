package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;

public class NbCnnbfdcUnitInfoWorker extends AbstractCrawlWorker{
	
	String iframeCss = "td>iframe[id=mapbarframe]";
	
	Map<String,String> roomStates = new HashMap<String,String>();
	
	RedisWorkQueue roomStateInfoQueue;
	
	@Override
	protected void insideInit() {
		roomStateInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "nb_cnnbfdc_room_state_info");
	}

	@Override
	protected void beforeDown(Page doingPage) {
	}

	@Override
	protected void beforeExtract(Page doingPage) {
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		//获取楼栋名称的详情链接地址
		String dataCssSelect_1 = "table>tbody>tr:gt(0)>td:eq(0)>a[class='e_huangse']";
		Elements elements = doingPage.getDoc().select(dataCssSelect_1);
		List<String> unitName = resultContext.getExtractResult("unitName");
		if(unitName != null){
			for(int i=0;i<unitName.size();i++){
				String unitId = resultContext.getOutResults().get(i).get(Constants.DEFAULT_RESULT_ID);
				String onclick = elements.get(i).attr("onclick");
				String roomInfoPageUrl = "http://newhouse.cnnbfdc.com/" + onclick.substring(onclick.indexOf("'")+1, onclick.indexOf(",")-1);
				Page roomInfoPage = new Page(doingPage.getSiteCode(), 1, roomInfoPageUrl, roomInfoPageUrl);
				roomInfoPage.setMethod(HttpMethod.GET);
				getDowner().down(roomInfoPage);
				Element iframe = roomInfoPage.getDoc().select(iframeCss).first();
				if (null == iframe) {
					throw new RuntimeException("don't find iframe:" + iframeCss);
				}
				
				String src = iframe.attr("src");
				Page page = new Page(doingPage.getSiteCode(), 1, src, src);
				page.setReferer(doingPage.getFinalUrl());
				page.getMetaMap().computeIfAbsent("unitId",mapKey->new ArrayList<>()).add(unitId);
				roomStateInfoQueue.push(page);
			}
		}
	}

	@Override
	protected void insideOnError(Exception e, Page doingPage) {
	}

}
