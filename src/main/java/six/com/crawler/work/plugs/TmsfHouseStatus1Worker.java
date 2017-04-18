package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月6日 上午9:17:56 
*/
public class TmsfHouseStatus1Worker extends AbstractCrawlWorker {

	Map<String, String> jsonKeyMap;
	RedisWorkSpace<Page> houseInfoQueue;
	private String sidTemplate = "<<sid>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";
	private String projectIdTemplate = "<<projectId>>";
	private String houseIdTemplate = "<<houseId>>";
	
	private String houseJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewProperty_showbox.jspx?"
			+ "buildingid="+buildingidTemplate
			+ "&presellid="+presellIdTemplate
			+ "&sid="+sidTemplate;
	private String houseInfoUrlTemplate = "http://www.tmsf.com/newhouse/property_house_" + sidTemplate + "_"
			+ projectIdTemplate + "_" + houseIdTemplate + ".htm";

	@Override
	protected void insideInit() {
		houseInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "tmsf_house_info", Page.class);
		jsonKeyMap = new HashMap<>();
		jsonKeyMap.put("buildingName", "buildingname");
		jsonKeyMap.put("unitName", "unitname");
		jsonKeyMap.put("internalId", "internalid");
		jsonKeyMap.put("houseId", "houseid");
		jsonKeyMap.put("houseNo", "houseno");
		jsonKeyMap.put("status", "housestatename");
		jsonKeyMap.put("houseUsage", "houseusage");
		jsonKeyMap.put("buildingArea", "builtuparea");
		jsonKeyMap.put("roughPrice", "declarationofroughprice");
		jsonKeyMap.put("totalPrice", "totalprice");
		jsonKeyMap.put("houseAddress", "located");
		jsonKeyMap.put("internalArea", "setinsidefloorarea");
		jsonKeyMap.put("houseStyle", "huxing");
		jsonKeyMap.put("floor", "floor");
	}

	protected void beforeDown(Page doingPage) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		
		String sidCss = "input[id=sid]";
		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = "";
		if (null != sidElement) {
			sid = sidElement.attr("value");
		}
		
		String presellidCss = "input[id=presellid]";
		Element presellidElement = doingPage.getDoc().select(presellidCss).first();
		String presellId = "";
		if (null != presellidElement) {
			presellId = presellidElement.attr("value");
		}
		String buildingidCss = "input[id=buildingid]";
		Element buildingidElement = doingPage.getDoc().select(buildingidCss).first();
		String buildingId = "";
		if (null != buildingidElement) {
			buildingId = buildingidElement.attr("value");
		}
		
		String houseInfoUrl = StringUtils.replace(houseJsonUrlTemplate, sidTemplate, sid);
		houseInfoUrl = StringUtils.replace(houseInfoUrl, presellIdTemplate, presellId);
		houseInfoUrl = StringUtils.replace(houseInfoUrl, buildingidTemplate, buildingId);

		Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
		houseInfoPage.setReferer(doingPage.getFinalUrl());
		houseInfoPage.setMethod(HttpMethod.GET);
		houseInfoPage.setType(PageType.JSON.value());
		houseInfoPage.getMetaMap().put("buildingId", ArrayListUtils.asList(buildingId));
		houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
		getDowner().down(houseInfoPage);
		
		String houseInfoJson = houseInfoPage.getPageSrc();
		Map<String, Object> map = JsonUtils.toObject(houseInfoJson, Map.class);
		List<Map<String, Object>> houseList = (List<Map<String, Object>>) map.get("list");
		
		for (Map<String, Object> houseMap : houseList) {
			for (String field : jsonKeyMap.keySet()) {
				String jsonKey = jsonKeyMap.get(field);
				Object valueOb = houseMap.get(jsonKey);
				doingPage.getMetaMap().computeIfAbsent(field, mapKey -> new ArrayList<>())
				.add(null != valueOb ? valueOb.toString() : "");
			}
		}                           
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {

		String sid = doingPage.getMeta("sid").get(0);
		String propertyId = doingPage.getMeta("propertyId").get(0);
		String projectName = doingPage.getMeta("projectName").get(0);
		String presellName = doingPage.getMeta("presellName").get(0);
		String presellCode = doingPage.getMeta("presellCode").get(0);
		
		
		String buildingName = doingPage.getMetaMap().get("buildingName").get(0);
		
		List<String> houseIds = resultContext.getExtractResult("houseId");
		List<String> houseNos = resultContext.getExtractResult("houseNo");
		List<String> internalIds = resultContext.getExtractResult("internalId");
		List<String> unitNames = resultContext.getExtractResult("unitName");
		List<String> houseUsages = resultContext.getExtractResult("houseUsage");
		List<String> buildingAreas = resultContext.getExtractResult("buildingArea");
		
		List<String> roughPrices = resultContext.getExtractResult("roughPrice");
		List<String> totalPrices = resultContext.getExtractResult("totalPrice");
		List<String> addresss = resultContext.getExtractResult("houseAddress");
		
		List<String> internalAreas = resultContext.getExtractResult("internalArea");
		List<String> houseStyles = resultContext.getExtractResult("houseStyle");
		
		
		if (null != houseIds && houseIds.size() > 0) {
			String houseId = null;
			String houseNo = null;
			String internalId = null;
			String unitName = null;
			String houseUsage = null;
			String buildingArea = null;
			String roughPrice = null;
			String totalPrice = null;
			String houseAddress = null;
			String internalArea = null;
			String houseStyle = null;
			
			for (int i = 0; i < houseIds.size(); i++) {
				houseId = houseIds.get(i);
				houseNo = houseNos.get(i);
				internalId= internalIds.get(i);
				unitName = unitNames.get(i);
				houseUsage = houseUsages.get(i);
				buildingArea = buildingAreas.get(i);
				roughPrice = roughPrices.get(i);
				totalPrice = totalPrices.get(i);
				houseAddress = addresss.get(i);
				internalArea = internalAreas.get(i);
				houseStyle = houseStyles.get(i);
				
				String houseInfoUrl = StringUtils.replace(houseInfoUrlTemplate, sidTemplate, sid);
				houseInfoUrl = StringUtils.replace(houseInfoUrl, projectIdTemplate, propertyId);
				houseInfoUrl = StringUtils.replace(houseInfoUrl, houseIdTemplate, houseId);
				Page houseInfoPage = new Page(getSite().getCode(), 1, houseInfoUrl, houseInfoUrl);
				houseInfoPage.setReferer(doingPage.getFinalUrl());
				
				houseInfoPage.getMetaMap().put("sid", ArrayListUtils.asList(sid));
				houseInfoPage.getMetaMap().put("propertyId", ArrayListUtils.asList(propertyId));
				houseInfoPage.getMetaMap().put("projectName", ArrayListUtils.asList(projectName));
				houseInfoPage.getMetaMap().put("presellName", ArrayListUtils.asList(presellName));
				houseInfoPage.getMetaMap().put("presellCode", ArrayListUtils.asList(presellCode));
				houseInfoPage.getMetaMap().put("buildingName", ArrayListUtils.asList(buildingName));
				houseInfoPage.getMetaMap().put("houseNo", ArrayListUtils.asList(houseNo));
				houseInfoPage.getMetaMap().put("houseId", ArrayListUtils.asList(houseId));
				houseInfoPage.getMetaMap().put("internalId", ArrayListUtils.asList(internalId));
				houseInfoPage.getMetaMap().put("unitName", ArrayListUtils.asList(unitName));
				houseInfoPage.getMetaMap().put("houseUsage", ArrayListUtils.asList(houseUsage));
				houseInfoPage.getMetaMap().put("buildingArea", ArrayListUtils.asList(buildingArea));
				houseInfoPage.getMetaMap().put("roughPrice", ArrayListUtils.asList(roughPrice));
				
				houseInfoPage.getMetaMap().put("totalPrice", ArrayListUtils.asList(totalPrice));
				houseInfoPage.getMetaMap().put("houseAddress", ArrayListUtils.asList(houseAddress));
				houseInfoPage.getMetaMap().put("internalArea", ArrayListUtils.asList(internalArea));
				houseInfoPage.getMetaMap().put("houseStyle", ArrayListUtils.asList(houseStyle));
				
				if (!houseInfoQueue.isDone(houseInfoPage.getPageKey())) {
					houseInfoQueue.push(houseInfoPage);
				}
			}

		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}


}
