package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月5日 上午11:58:28
 */
public class TmsfHouseStatusWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(TmsfHouseStatusWorker.class);
	RedisWorkSpace<Page> houseInfoQueue;
	Map<String, String> jsonKeyMap;
	private String sidTemplate = "<<sid>>";
	private String projectIdTemplate = "<<projectId>>";
	private String houseIdTemplate = "<<houseId>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";
	private String areaTemplate = "<<area>>";
	private String allpriceTemplate = "<<allprice>>";
	private String housestateTemplate = "<<housestate>>";
	private String housetypeTemplate = "<<housetype>>";
	private String houseJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewPropertyHz_showbox.jspx?" + "buildingid="
			+ buildingidTemplate + "&presellid=" + presellIdTemplate + "&sid=" + sidTemplate + "&area=" + areaTemplate
			+ "&allprice=" + allpriceTemplate + "&housestate=" + housestateTemplate + "&housetype=" + housetypeTemplate;
	private String sidCss = "input[id=sid]";
	private String areaCss = "input[id=area]";
	private String allpriceCss = "input[id=allprice]";
	private String housestateCss = "input[id=housestate]";
	private String housetypeCss = "input[id=housetype]";
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
		jsonKeyMap.put("address", "located");
		jsonKeyMap.put("internalArea", "setinsidefloorarea");
		jsonKeyMap.put("houseStyle", "huxing");
	}

	protected void beforeDown(Page doingPage) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {

		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = "";
		if (null != sidElement) {
			sid = sidElement.attr("value");
		}
		Element areaElement = doingPage.getDoc().select(areaCss).first();
		String area = "";
		if (null != areaElement) {
			area = areaElement.attr("value");
		}
		Element allpriceElement = doingPage.getDoc().select(allpriceCss).first();
		String allprice = "";
		if (null != allpriceElement) {
			allprice = allpriceElement.attr("value");
		}
		Element housestateElement = doingPage.getDoc().select(housestateCss).first();
		String housestate = "";
		if (null != housestateElement) {
			housestate = housestateElement.attr("value");
		}

		Element housetypeElement = doingPage.getDoc().select(housetypeCss).first();
		String housetype = "";
		if (null != housetypeElement) {
			housetype = housetypeElement.attr("value");
		}

		String presellId_org = doingPage.getMetaMap().get("presellId_org").get(0);
		String buildingId = doingPage.getMetaMap().get("buildingId").get(0);
		String houseJsonUrl = StringUtils.replace(houseJsonUrlTemplate, buildingidTemplate, buildingId);
		houseJsonUrl = StringUtils.replace(houseJsonUrl, presellIdTemplate, presellId_org);
		houseJsonUrl = StringUtils.replace(houseJsonUrl, sidTemplate, sid);
		houseJsonUrl = StringUtils.replace(houseJsonUrl, areaTemplate, area);
		houseJsonUrl = StringUtils.replace(houseJsonUrl, allpriceTemplate, allprice);
		houseJsonUrl = StringUtils.replace(houseJsonUrl, housestateTemplate, housestate);
		houseJsonUrl = StringUtils.replace(houseJsonUrl, housetypeTemplate, housetype);
		houseJsonUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseJsonUrl);
		Page housePage = new Page(doingPage.getSiteCode(), 1, houseJsonUrl, houseJsonUrl);
		housePage.setReferer(doingPage.getFinalUrl());
		housePage.setType(PageType.JSON.value());
		getDowner().down(housePage);
		String houseInfoJson = housePage.getPageSrc();
		Map<String, Object> map = JsonUtils.toObject(houseInfoJson, Map.class);
		List<Map<String, Object>> houseList = (List<Map<String, Object>>) map.get("list");

		String floorDivCss = "div[class=raphael_box][types=1]";
		Elements floorDivs = doingPage.getDoc().select(floorDivCss);

		Map<String, String> floorMap = new HashMap<>();
		for (Element floorDiv : floorDivs) {
			String floorKey = floorDiv.attr("floor");
			String floor = floorDiv.attr("title");
			floorMap.put(floorKey, floor);
		}
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
		
		String presellId_org = doingPage.getMetaMap().get("presellId_org").get(0);
		String buildingId = doingPage.getMetaMap().get("buildingId").get(0);
		
		String buildingName = doingPage.getMetaMap().get("buildingName").get(0);
		
		List<String> houseIds = resultContext.getExtractResult("houseId");
		List<String> houseNos = resultContext.getExtractResult("houseNo");
		
		List<String> unitNames = resultContext.getExtractResult("unitName");
		List<String> houseUsages = resultContext.getExtractResult("houseUsage");
		List<String> buildingAreas = resultContext.getExtractResult("buildingArea");
		
		List<String> roughPrices = resultContext.getExtractResult("roughPrice");
		List<String> totalPrices = resultContext.getExtractResult("totalPrice");
		List<String> addresss = resultContext.getExtractResult("address");
		
		List<String> internalAreas = resultContext.getExtractResult("internalArea");
		List<String> houseStyles = resultContext.getExtractResult("houseStyle");
		
		
		if (null != houseIds && houseIds.size() > 0) {
			String houseId = null;
			String houseNo = null;
			String unitName = null;
			String houseUsage = null;
			String buildingArea = null;
			String roughPrice = null;
			String totalPrice = null;
			String address = null;
			String internalArea = null;
			String houseStyle = null;
			
			for (int i = 0; i < houseIds.size(); i++) {
				houseId = houseIds.get(i);
				houseNo = houseNos.get(i);
				
				unitName = unitNames.get(i);
				houseUsage = houseUsages.get(i);
				buildingArea = buildingAreas.get(i);
				roughPrice = roughPrices.get(i);
				totalPrice = totalPrices.get(i);
				address = addresss.get(i);
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
				houseInfoPage.getMetaMap().put("presellId_org", ArrayListUtils.asList(presellId_org));
				houseInfoPage.getMetaMap().put("buildingId", ArrayListUtils.asList(buildingId));
				houseInfoPage.getMetaMap().put("buildingName", ArrayListUtils.asList(buildingName));
				houseInfoPage.getMetaMap().put("houseNo", ArrayListUtils.asList(houseNo));
				houseInfoPage.getMetaMap().put("houseId", ArrayListUtils.asList(houseId));
				
				houseInfoPage.getMetaMap().put("unitName", ArrayListUtils.asList(unitName));
				houseInfoPage.getMetaMap().put("houseUsage", ArrayListUtils.asList(houseUsage));
				houseInfoPage.getMetaMap().put("buildingArea", ArrayListUtils.asList(buildingArea));
				houseInfoPage.getMetaMap().put("roughPrice", ArrayListUtils.asList(roughPrice));
				
				houseInfoPage.getMetaMap().put("totalPrice", ArrayListUtils.asList(totalPrice));
				houseInfoPage.getMetaMap().put("address", ArrayListUtils.asList(address));
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
