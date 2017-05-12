package six.com.crawler.work.plugs;

import java.util.List;

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
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCSQProjectInfoWorker extends AbstractCrawlWorker{
	
	final static Logger log = LoggerFactory.getLogger(DLDCSQProjectInfoWorker.class);
	
	String pageCountCss = "font[color='#0033FF']";

	WorkSpace<Page> unitListQueue;
	
	int pageCount=-1;
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		unitListQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_sq_unit_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		if(pageCount==-1){
			Element pageCountElement = doingPage.getDoc().select(pageCountCss).first();
			if (null == pageCountElement) {
				getAndSetState(WorkerLifecycleState.STOPED);
				log.error("did not find pageCount element:" + pageCountCss);
			} else {
				String onclick = pageCountElement.ownText();
				String pageCountStr = StringUtils.substringBetween(onclick, "共", "页");
				try {
					pageCount = Integer.valueOf(pageCountStr);
				} catch (Exception e) {
					getAndSetState(WorkerLifecycleState.STOPED);
					log.error("get pageCount string:" + pageCountStr, e);
				}
			}
		}
		
		Element projectInfo=doingPage.getDoc().select("td[bgcolor='#a6d0e7']>table").get(0);
		Elements proItems=projectInfo.select("td[class='bluedeeppa12']");
		
		String projectName=proItems.get(1).ownText();
		String preSalePermitCertificate=proItems.get(3).ownText();
		String developer=proItems.get(5).ownText();
		String landUseCertificate=proItems.get(7).ownText();
		String address=proItems.get(9).ownText();
		String planPermitCertificate=proItems.get(11).ownText();

		doingPage.getMetaMap().put("projectName", ArrayListUtils.asList(projectName));
		doingPage.getMetaMap().put("preSalePermitCertificate", ArrayListUtils.asList(preSalePermitCertificate));
		doingPage.getMetaMap().put("developer", ArrayListUtils.asList(developer));
		doingPage.getMetaMap().put("landUseCertificate", ArrayListUtils.asList(landUseCertificate));
		doingPage.getMetaMap().put("address", ArrayListUtils.asList(address));
		doingPage.getMetaMap().put("planPermitCertificate", ArrayListUtils.asList(planPermitCertificate));
		
		Elements details=projectInfo.select("td[bgcolor='#a6d0e7']>table").get(0).select("tr");
		if(details.size()>=2){
			Elements items=details.select("span[class='bluedeeppa12']");
			String totalPloidy=items.get(0).ownText();
			String totoalArea=items.get(1).ownText();
			String canSoldTotalPloidy=items.get(2).ownText();
			String canSoleTotalArea=items.get(3).ownText();
			String houseCount=items.get(4).ownText();
			String houseArea=items.get(5).ownText();
			String canSoldHouseCount=items.get(6).ownText();
			String canSoldHouseArea=items.get(7).ownText();
			
			doingPage.getMetaMap().put("totalPloidy", ArrayListUtils.asList(totalPloidy));
			doingPage.getMetaMap().put("totoalArea", ArrayListUtils.asList(totoalArea));
			doingPage.getMetaMap().put("canSoldTotalPloidy", ArrayListUtils.asList(canSoldTotalPloidy));
			doingPage.getMetaMap().put("canSoleTotalArea", ArrayListUtils.asList(canSoleTotalArea));
			doingPage.getMetaMap().put("houseCount", ArrayListUtils.asList(houseCount));
			doingPage.getMetaMap().put("houseArea", ArrayListUtils.asList(houseArea));
			doingPage.getMetaMap().put("canSoldHouseCount", ArrayListUtils.asList(canSoldHouseCount));
			doingPage.getMetaMap().put("canSoldHouseArea", ArrayListUtils.asList(canSoldHouseArea));
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		//http://www.dlfd.gov.cn/fdc/D01XmxxAction.do?Control=detail&id=d0bf85fd635e4deebe402ff9baca5709&xmbh=48541
		List<String> projectIds=resultContext.getExtractResult("projectId");
		for (int i = 0; i < pageCount; i++) {
			String url="http://www.dlfd.gov.cn/fdc/D01XmxxAction.do?Control=detail&id="+projectIds.get(i)+"&pageNo="+(i+1);
			Page unitListPage=new Page(doingPage.getSiteCode(), 1, url, url);
			unitListPage.setMethod(HttpMethod.GET);
			unitListPage.setReferer(doingPage.getFinalUrl());
			unitListPage.getMetaMap().put("projectId", ArrayListUtils.asList(projectIds.get(i)));
			
			unitListQueue.push(unitListPage);
		}
	}

}
