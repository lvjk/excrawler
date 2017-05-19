package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class DLDCSQUnitInfoWorker extends AbstractCrawlWorker{

	WorkSpace<Page> roomStateInfoQueue;
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		roomStateInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_sq_room_state_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Element table=doingPage.getDoc().select("td[bgcolor='#a6d0e7']>table").get(2);
		Elements infos=table.select("tr:gt(0)");
		
		List<String> projectIds=new ArrayList<String>();
		List<String> unitIds=new ArrayList<String>();
		List<String> unitNos=new ArrayList<String>();
		List<String> preSaleCardNums=new ArrayList<String>();
		List<String> addrs=new ArrayList<String>();
		List<String> totalPloidys=new ArrayList<String>();
		List<String> totalAreas=new ArrayList<String>();
		
		String projectId=doingPage.getMeta("projectId").get(0);
		for (int i = 0; i < infos.size()-1; i++) {
			Elements tds=infos.get(i).select("td[class='bluedeep12']");
			String roomStateUrl=tds.get(0).select("a").first().attr("href");
			String unitId=StringUtils.substringAfter(roomStateUrl, "&lid=");
			String unitNo=tds.get(0).select("a").first().ownText();
			String preSaleCardNum=tds.get(1).ownText();
			String addr=tds.get(2).attr("title");
			String totalPloidy=tds.get(3).ownText();
			String totalArea=tds.get(4).ownText();
			
			projectIds.add(projectId);
			unitIds.add(unitId);
			unitNos.add(unitNo);
			preSaleCardNums.add(preSaleCardNum);
			addrs.add(addr);
			totalPloidys.add(totalPloidy);
			totalAreas.add(totalArea);
			String url="http://www.dlfd.gov.cn/fdc/D01XmxxAction.do?Control=lxxdetail_1&xmid="+projectId+"&lid="+unitId;
			Page roomStateInfo=new Page(doingPage.getSiteCode(), 1, url, url);
			roomStateInfo.setMethod(HttpMethod.GET);
			roomStateInfo.setReferer(doingPage.getFinalUrl());
			roomStateInfo.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
			roomStateInfo.getMetaMap().put("unitId", ArrayListUtils.asList(unitId));
			roomStateInfoQueue.push(roomStateInfo);
		}
		
		doingPage.getMetaMap().put("projectId", projectIds);
		doingPage.getMetaMap().put("unitId", unitIds);
		doingPage.getMetaMap().put("unitNo", unitNos);
		doingPage.getMetaMap().put("preSaleCardNum", preSaleCardNums);
		doingPage.getMetaMap().put("address", addrs);
		doingPage.getMetaMap().put("totalPloidy", totalPloidys);
		doingPage.getMetaMap().put("totalArea", totalAreas);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		
	}
}
