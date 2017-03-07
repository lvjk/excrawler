package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.common.utils.JsoupUtils.TableResult;
import six.com.crawler.work.AbstractCrawlWorker;

public class NbCnnbfdcRoomInfoWorker extends AbstractCrawlWorker{
	
	Map<String, String> fieldMap = new HashMap<String, String>();
	
	private String tableCss = "table";
	
	@Override
	protected void insideInit() {
		fieldMap.put("户室名称（室号）", "houseName");
		fieldMap.put("实际楼层", "actualFloor");
		fieldMap.put("户型", "houseType");
		fieldMap.put("房屋结构", "buildingStructure");
		fieldMap.put("房屋用途", "houseUsage");
		fieldMap.put("预测建筑面积", "predictionFloorArea");
		fieldMap.put("预测套内面积", "predictionTeachingArea");
		fieldMap.put("预测分摊面积", "predictionSharingSrea");
		fieldMap.put("实测建筑面积", "actualFloorArea");
		fieldMap.put("实测套内面积", "actualTeachingArea");
		fieldMap.put("实测分摊面积", "actualSharingSrea");
		fieldMap.put("附记", "excursus");
	}

	@Override
	protected void beforeDown(Page doingPage) {
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Element table = doingPage.getDoc().select(tableCss).first();
		if (null == table) {
			throw new RuntimeException("don't find table:" + tableCss);
		}
		List<TableResult> results = JsoupUtils.paserTable(table);
		for (TableResult result : results) {
			String key=fieldMap.get(result.getKey());
			if(null!=key){
				//doingPage.getMetaMap().put("roomStateId", doingPage.getMeta("roomStateId"));
				doingPage.getMetaMap().computeIfAbsent(key,mapKey->new ArrayList<>()).add(StringUtils.trim(result.getValue()));
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
