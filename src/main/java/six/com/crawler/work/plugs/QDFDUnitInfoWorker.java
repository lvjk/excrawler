package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

public class QDFDUnitInfoWorker extends AbstractCrawlWorker{

	
	private String unitInfoUrlCss="ul[class=xsb_bg]>table:eq(1)>tbody>tr>td>a";
	
	RedisWorkSpace<Page> projectUnitInfoQueue;
	
	private static final String BASE_URL="http://www.qdfd.com.cn/qdweb/realweb/fh/FhHouseStatus.jsp";
	
	@Override
	protected void insideInit() {
		projectUnitInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(),"qdfd_room_state_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		
		Elements urlElements=doingPage.getDoc().select(unitInfoUrlCss);
		for (Element url:urlElements) {
			String paramStr=StringUtils.substringBetween(url.attr("href"), "javascript:showHouseStatus(", ")");
			String []params=paramStr.replaceAll("\"", "").split(",");
			String buildid=params[0];
			String startid=params[1];
			String proid=params[2];
			
			String pageUrl=BASE_URL+"?buildingID="+buildid+"&startID="+startid+"&projectID="+proid;
			Page page=new Page(doingPage.getSiteCode(),1,pageUrl,pageUrl);
			page.setMethod(HttpMethod.GET);
			page.setReferer(doingPage.getFinalUrl());
			page.getMetaMap().put("projectId", doingPage.getMetaMap().get("projectId"));
			page.getMetaMap().put("projectName", doingPage.getMetaMap().get("projectName"));
			page.getMetaMap().put("buildId", ArrayListUtils.asList(buildid));//楼栋ID
			page.getMetaMap().put("startId", ArrayListUtils.asList(startid));//start ID
			projectUnitInfoQueue.push(page);
		}
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}
}
