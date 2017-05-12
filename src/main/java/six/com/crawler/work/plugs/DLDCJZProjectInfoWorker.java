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
	
	String projectDetailCss="div[class=subdiv_1]>form[name=ysxkzForm]>table>tbody>tr>td[width='300']";
	
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
		Elements details=doingPage.getDoc().select(projectDetailCss);
		doingPage.getMetaMap().put("preSalePermitNo", ArrayListUtils.asList(details.get(0).ownText()));
		doingPage.getMetaMap().put("projectName", ArrayListUtils.asList(details.get(1).ownText()));
		doingPage.getMetaMap().put("developer", ArrayListUtils.asList(details.get(2).ownText()));
		doingPage.getMetaMap().put("address", ArrayListUtils.asList(details.get(3).ownText()));
		doingPage.getMetaMap().put("salesArea", ArrayListUtils.asList(details.get(4).ownText()));
		doingPage.getMetaMap().put("issuingAuthority", ArrayListUtils.asList(details.get(5).ownText()));
		doingPage.getMetaMap().put("issueDate", ArrayListUtils.asList(details.get(6).ownText()));
		doingPage.getMetaMap().put("qualificationCertificateNo", ArrayListUtils.asList(details.get(7).ownText()));
		doingPage.getMetaMap().put("businessLicenseNo", ArrayListUtils.asList(details.get(8).ownText()));
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		Element element=doingPage.getDoc().select("form[name='ysxkzForm']").get(1);
		
		Elements trElements=element.select("table>tbody>tr");
		for (int i = 0; i < trElements.size(); i++) {
			Page unitPage=new Page(doingPage.getSiteCode(), 1, doingPage.getFirstUrl(), doingPage.getFinalUrl());
			if(i>0){
				Elements attrElements=trElements.get(i).select("td");
				String unitNo=attrElements.get(1).select("span>nobr").first().ownText();
				String area=attrElements.get(2).select("span>nobr").first().ownText();
				String address=attrElements.get(3).select("span>nobr").first().ownText();
				String url="http://www.fczw.cn/"+attrElements.get(4).select("a").first().attr("href");
				String lid=StringUtils.substringBetween(url, "&lid=", "&xmid");
				String xmid=StringUtils.substringAfter(url, "&xmid=");
				
				unitPage.getMetaMap().put("presellId", doingPage.getMeta("presellId"));
				unitPage.getMetaMap().put("unitId", ArrayListUtils.asList(lid));
				unitPage.getMetaMap().put("projectId", ArrayListUtils.asList(xmid));
				unitPage.getMetaMap().put("unitNo", ArrayListUtils.asList(unitNo));
				unitPage.getMetaMap().put("buildArea", ArrayListUtils.asList(area));
				unitPage.getMetaMap().put("address", ArrayListUtils.asList(address));
				unitPage.getMetaMap().put("url", ArrayListUtils.asList(url));
				
				unitInfoQueue.push(unitPage);
			}
		}
	}
}
