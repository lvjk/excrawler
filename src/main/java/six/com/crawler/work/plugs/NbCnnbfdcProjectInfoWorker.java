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
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;

public class NbCnnbfdcProjectInfoWorker extends AbstractCrawlWorker {

	
	RedisWorkQueue unitInfoQueue;
	private Map<String, String> fieldMap = new HashMap<String, String>();
	String tabaleCss = "td[valign=top]:eq(0)>table[bgcolor=#DDDDDD]:eq(1)";

	@Override
	protected void insideInit() {
		unitInfoQueue=new RedisWorkQueue(getManager().getRedisManager(), "nb_cnnbfdc_unit_info");
		fieldMap.put("项目名称：", "projectName");
		fieldMap.put("项目地址：", "address");
		fieldMap.put("开发公司：", "developer");
		fieldMap.put("发证机关：", "issueCompany");
		fieldMap.put("预(现)售证名称：", "presellName");
		fieldMap.put("预(现)售许可证号：", "presellCode");
		fieldMap.put("纳入网上可售面积：", "forSellArea");
		fieldMap.put("纳入网上可售套数：", "forSellHouseCount");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Element table = doingPage.getDoc().select(tabaleCss).first();
		if (null == table) {
			throw new RuntimeException("don't find table:" + tabaleCss);
		}
		List<TableResult> results = JsoupUtils.paserTable(table);
		for (TableResult result : results) {
			String key=fieldMap.get(result.getKey());
			if(null!=key){
				String value=result.getValue();
				if("projectName".equals(key)){
					value=StringUtils.replace(value,"地图定位", "");
					value=StringUtils.trim(value);
				}
				doingPage.getMetaMap().computeIfAbsent(key,mapKey->new ArrayList<>()).add(value);
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String tabaleCss = "td[valign=top]:eq(0)>table[bgcolor=#DDDDDD]:eq(3)";
		String projectId = resultContext.getOutResults().get(0).get(Constants.DEFAULT_RESULT_ID);
		Map<String, String> fieldMap = new HashMap<String, String>();
		fieldMap.put("楼栋名称", "unitName");
		fieldMap.put("纳入网上可售住宅套数", "forSellHouseCount");
		fieldMap.put("纳入网上可售非住宅套数", "forSellNoHouseCount");
		Element table = doingPage.getDoc().select(tabaleCss).first();
		if (null == table) {
			throw new RuntimeException("don't find table:" + tabaleCss);
		}
		String headCssSelect = "table>tbody>tr:eq(0)>td";
		String dataCssSelect = "table>tbody>tr:gt(0)";
		Map<String,List<String>> mp = new HashMap<String,List<String>>();
		Map<String,List<String>> map = JsoupUtils.paserTable(table, headCssSelect, dataCssSelect);
		for (String field : map.keySet()) {
			String resultKey=fieldMap.get(field);
			if(null!=resultKey) {
				mp.put(resultKey, map.get(field));
			}
		}
		List<String> unitNames = mp.get("unitName");
		if(unitNames!=null && unitNames.size()>0){
			List<String> projectIds = new ArrayList<String>();
			for (int i = 0; i < unitNames.size(); i++) {
				projectIds.add(projectId);
			}
			List<String> unitUrls = resultContext.getExtractResult("unitUrl");
			Page unitInfoPage = new Page(doingPage.getSiteCode(), 1, doingPage.getFirstUrl(), doingPage.getFinalUrl());
			unitInfoPage.setNoNeedDown(1);
			unitInfoPage.setReferer(doingPage.getFinalUrl());
			unitInfoPage.getMetaMap().put("projectId", projectIds);
			unitInfoPage.getMetaMap().put("unitName", unitNames);
			unitInfoPage.getMetaMap().put("unitUrl", unitUrls);
			unitInfoPage.getMetaMap().put("forSellHouseCount", mp.get("forSellHouseCount"));
			unitInfoPage.getMetaMap().put("forSellNoHouseCount", mp.get("forSellNoHouseCount"));
			unitInfoQueue.push(unitInfoPage);
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
	
}
