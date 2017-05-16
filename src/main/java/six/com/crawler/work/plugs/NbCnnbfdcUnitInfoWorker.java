package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

public class NbCnnbfdcUnitInfoWorker extends AbstractCrawlWorker {

	Map<String, String> roomStates = new HashMap<String, String>();

	WorkSpace<Page> roomStateInfoQueue;
	
	String BASE_URL="http://newhouse.cnnbfdc.com/GetHouseTable.aspx?qrykey=";

	@Override
	protected void insideInit() {
		roomStateInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("nb_cnnbfdc_room_state_info", Page.class);
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
		// 获取楼栋名称的详情链接地址
		List<String> unitNames = resultContext.getExtractResult("unitName");
		List<String> unitIds = resultContext.getExtractResult("unitId");
		List<String> projectIds = resultContext.getExtractResult("projectId");
		List<String> projectNames = resultContext.getExtractResult("projectName");
		for (int i = 0; i < unitNames.size(); i++) {
			String url=BASE_URL+unitIds.get(i);
			Page roomStatePage = new Page(doingPage.getSiteCode(), 1, url, url);
			roomStatePage.setReferer(doingPage.getFinalUrl());

			roomStatePage.getMetaMap().put("projectId", ArrayListUtils.asList(projectIds.get(i)));
			roomStatePage.getMetaMap().put("projectName", ArrayListUtils.asList(projectNames.get(i)));
			roomStatePage.getMetaMap().put("unitName", ArrayListUtils.asList(unitNames.get(i)));
			roomStatePage.getMetaMap().put("unitId", ArrayListUtils.asList(unitIds.get(i)));
			roomStateInfoQueue.push(roomStatePage);
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
