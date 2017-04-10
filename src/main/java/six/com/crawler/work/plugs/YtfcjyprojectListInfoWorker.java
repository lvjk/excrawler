package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

public class YtfcjyprojectListInfoWorker extends AbstractCrawlWorker{
	final static Logger log = LoggerFactory.getLogger(YtfcjyprojectListInfoWorker.class);
	
	RedisWorkSpace<Page> projectInfoQueue;
	String projectListUril = "http://www.ytfcjy.com/public/project/ProjectList.aspx";
	
	// 第一页从1开始
	int pageIndex = 1;
	int pageCount = -1;
	String refererUrl;
	
//	private Page buildPage(int pageIndex, String refererUrl, Map<String, Object> paramMap) {
//		
//		Page page = new Page(getSite().getCode(), 1, projectListUril, projectListUril);
//		page.setReferer(refererUrl);
//		
//		if(1==pageIndex){
//			page.setMethod(HttpMethod.GET);
//		}else {
//			page.setParameters(paramMap);
//			page.setMethod(HttpMethod.POST);
//		}
//
//		page.setType(PageType.LISTING.value());
//		return page;
//	}
	
	@Override
	protected void insideInit() {
		//初始化项目队列
		projectInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "ytfcjy_project_info",Page.class);
		Page projectListPage = new Page(getSite().getCode(), 1, projectListUril, projectListUril);
		getWorkQueue().push(projectListPage);
		
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		System.out.println("ddddddddddddddddddddddddddddd");
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		if (pageCount == -1) {
			Element pageCountElement = doc.getElementById("PageNavigator1_LblPageCount");
			String pageCountStr = pageCountElement.text();
			pageCount = Integer.valueOf(pageCountStr);
		}
		
		Elements  tableList = doc.select("div.main_content>table.tableStyle>tbody>tr.TR_BG_list");
		for(int index=0; index<tableList.size();index++){
			Elements  tdList = tableList.get(index).select("td");
			if(tdList.size()==7){
				System.out.println("编号："+tdList.get(0).text());
				System.out.println("项目名称："+tdList.get(1).text());
				System.out.println("开发商："+tdList.get(2).text());
			}
			
		}
		
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		WebElement nextElement = getDowner().findWebElement("//*[@id='PageNavigator1_LnkBtnNext']");
		getDowner().click(nextElement, "//*[@id='PageNavigator1_LnkBtnNext']");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

}
