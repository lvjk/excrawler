package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCJZProjectInfoWorker extends AbstractCrawlWorker{

	WorkSpace<Page> unitInfoQueue;
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		unitInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_jz_unit_info", Page.class);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		Element element=doingPage.getDoc().select("form[name='ysxkzForm']").get(1);
		
		Elements trElements=element.select("table>tbody>tr");
		String projectName=resultContext.getExtractResult("projectName").get(0);
		String presellCode=resultContext.getExtractResult("presellCode").get(0);
		int rowIndex=0;
		for (Element tr:trElements) {
			Page unitPage=new Page(doingPage.getSiteCode(), 1, doingPage.getFirstUrl(), doingPage.getFinalUrl());
			unitPage.setNoNeedDown(1);
			if(rowIndex>0){
				Elements attrElements=tr.select("td");
				String unitNo=attrElements.get(1).select("span>nobr").first().ownText();
				String area=attrElements.get(2).select("span>nobr").first().ownText();
				String address=attrElements.get(3).select("span>nobr").first().ownText();
				String url=attrElements.get(4).select("a").first().attr("href");
				String lid=StringUtils.substringBetween(url, "&lid=", "&xmid");
				String xmid=StringUtils.substringAfter(url, "&xmid");
				
				unitPage.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
				unitPage.getMetaMap().put("projectName", ArrayListUtils.asList(projectName));
				unitPage.getMetaMap().put("lid", ArrayListUtils.asList(lid));
				unitPage.getMetaMap().put("xmid", ArrayListUtils.asList(xmid));
				unitPage.getMetaMap().put("presellCode", ArrayListUtils.asList(presellCode));
				unitPage.getMetaMap().put("unitNo", ArrayListUtils.asList(unitNo));
				unitPage.getMetaMap().put("area", ArrayListUtils.asList(area));
				unitPage.getMetaMap().put("address", ArrayListUtils.asList(address));
				unitPage.getMetaMap().put("url", ArrayListUtils.asList(url));
				
				unitInfoQueue.push(unitPage);
			}
			
			rowIndex++;
		}
	}
}
