package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;

public class NbCnnbfdcUnitInfoWorker extends AbstractCrawlWorker{
	
	
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
		List<String> unitUrls = resultContext.getExtractResult("unitUrl");
		List<String> unitName = resultContext.getExtractResult("unitName");
		for(int i=0;i<unitName.size();i++){
			String unitId = resultContext.getOutResults().get(i).get(Constants.DEFAULT_RESULT_ID);
			String roomInfoPageUrl =unitUrls.get(i);
			Page roomStatePage = new Page(doingPage.getSiteCode(), 1, roomInfoPageUrl, roomInfoPageUrl);
			roomStatePage.setReferer(doingPage.getFinalUrl());
			roomStatePage.getMetaMap().computeIfAbsent("unitId",mapKey->new ArrayList<>()).add(unitId);
			roomStateInfoQueue.push(roomStatePage);
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
