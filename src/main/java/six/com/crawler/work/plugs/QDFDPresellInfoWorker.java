package six.com.crawler.work.plugs;

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

public class QDFDPresellInfoWorker extends AbstractCrawlWorker{

	final static Logger log = LoggerFactory.getLogger(QDFDPresellInfoWorker.class);
	
	RedisWorkSpace<Page> projectUnitUrlQueue;
	
	private String unitInfoUrlCss="ul[class=kpdy_bg]>table:eq(1)>tbody>tr>td>a";
	
	private static final String BASE_URL="http://www.qdfd.com.cn/qdweb/realweb/fh/FhHouseStatus.jsp";
	
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		projectUnitUrlQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(),"qdfd_unit_info", Page.class);
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
		Elements urlElement = doingPage.getDoc().select(unitInfoUrlCss);
		if(null==urlElement){
			return;
		}
		
		Element birdElement=doingPage.getDoc().select("a[id=Bird_btn]").first();
		//FhProjectBird.jsp?projectID=2096&projectName=银盛泰 德郡四期&presell_id=3321&predesc=青房注字(城13)第37
		String href=birdElement.attr("href");
		String presell_id=StringUtils.substringBetween(href, "&presell_id=", "&predesc=");
		String predesc=StringUtils.substringBetween(href, "&predesc=");
		for (Element url:urlElement) {
			String unitId=StringUtils.substringBetween(url.attr("href"), "javascript:getBuilingList(", "\",\"");
			String pageUrl=BASE_URL+"?preid="+unitId;
			Page page=new Page(doingPage.getSiteCode(),1,pageUrl,pageUrl);
			page.setMethod(HttpMethod.GET);
			page.setReferer(doingPage.getFinalUrl());
			page.getMetaMap().putAll(doingPage.getMetaMap());
			page.getMetaMap().put("preid", ArrayListUtils.asList(presell_id));
			page.getMetaMap().put("preDesc", ArrayListUtils.asList(predesc));
			projectUnitUrlQueue.push(page);
		}
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

}
