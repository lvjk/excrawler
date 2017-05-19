package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
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
public class DLDCJZUnitInfoWorker  extends AbstractCrawlWorker{

	WorkSpace<Page> roomStateInfoQueue;
	
	String unitDetailCss="form[id=ysxkzForm]>table>tbody>tr:eq(0)>td>table>tbody>tr>td>table>tbody>tr>td";
	@Override
	protected void insideInit() {
		// TODO Auto-generated method stub
		roomStateInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("dldc_jz_room_state_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Elements details=doingPage.getDoc().select(unitDetailCss);
		String buildStructure=StringUtils.substringAfter(details.get(0).ownText(),"建筑结构：");
		String totalLayer=StringUtils.substringAfter(details.get(1).ownText(),"总层数：");
		String groundLayer=StringUtils.substringAfter(details.get(3).ownText(),"地上层数：");
		doingPage.getMetaMap().put("buildStructure", ArrayListUtils.asList(buildStructure));
		doingPage.getMetaMap().put("totalLayer", ArrayListUtils.asList(totalLayer));
		doingPage.getMetaMap().put("groundLayer", ArrayListUtils.asList(groundLayer));
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String url=doingPage.getMeta("url").get(0);
		Page roomStateInfoPage= new Page(doingPage.getSiteCode(), 1, url, url);
		roomStateInfoPage.setReferer(doingPage.getFinalUrl());
		roomStateInfoPage.setMethod(HttpMethod.GET);
		
		roomStateInfoPage.getMetaMap().put("presellId", doingPage.getMeta("presellId"));
		roomStateInfoPage.getMetaMap().put("unitId", doingPage.getMeta("unitId"));
		roomStateInfoPage.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
		roomStateInfoQueue.push(roomStateInfoPage);
	}
}
