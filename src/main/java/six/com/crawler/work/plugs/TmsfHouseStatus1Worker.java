package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.utils.JsUtils;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 上午9:17:56
 */
public class TmsfHouseStatus1Worker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(TmsfHouseStatus1Worker.class);
	private Map<String, String> jsonKeyMap;
	private WorkSpace<Page> houseInfoQueue;
	private String sidTemplate = "<<sid>>";
	private String propertyIdTemplate = "<<propertyId>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";
	private String projectIdTemplate = "<<projectId>>";
	private String houseIdTemplate = "<<houseId>>";

	private String houseStatusJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewProperty_showbox.jspx?" + "buildingid="
			+ buildingidTemplate + "&sid=" + sidTemplate + "&propertyid=" + propertyIdTemplate + "&presellid="
			+ presellIdTemplate;
	private String houseInfoUrlTemplate = "http://www.tmsf.com/newhouse/property_house_" + sidTemplate + "_"
			+ projectIdTemplate + "_" + houseIdTemplate + ".htm";

	private String houseStatusJsUrlTemplate = "" + "/newhouse/NewPropertyControl_load.jspx?" + "sid=" + sidTemplate
			+ "&propertyid=" + projectIdTemplate + "&presellid=" + presellIdTemplate + "&buildingid="
			+ buildingidTemplate;

	private java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

	@Override
	protected void insideInit() {
		houseInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("tmsf_house_info", Page.class);
		jsonKeyMap = new HashMap<>();
		// jsonKeyMap.put("buildingName", "buildingname");
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

		String propertyId = doingPage.getMetaMap().get("propertyId").get(0);

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

		if (isTableLayout(doingPage)) {
			doTable(doingPage, sid, propertyId, presellId, buildingId);
		}else{
			String houseInfoUrl = StringUtils.replace(houseStatusJsonUrlTemplate, sidTemplate, sid);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, propertyIdTemplate, propertyId);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, presellIdTemplate, presellId);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, buildingidTemplate, buildingId);

			Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
			houseInfoPage.setReferer(doingPage.getFinalUrl());
			houseInfoPage.setMethod(HttpMethod.GET);
			houseInfoPage.getMetaMap().put("buildingId", ArrayListUtils.asList(buildingId));
			getDowner().down(houseInfoPage);

			String houseInfoJson = houseInfoPage.getPageSrc();
			Map<String, Object> map = JsonUtils.toObject(houseInfoJson, Map.class);
			List<Map<String, Object>> houseList = (List<Map<String, Object>>) map.get("list");
			if (null != houseList && !houseList.isEmpty()) {
				for (Map<String, Object> houseMap : houseList) {
					Object valueOb = null;
					String result = null;
					for (String field : jsonKeyMap.keySet()) {
						String jsonKey = jsonKeyMap.get(field);
						valueOb = houseMap.get(jsonKey);
						if (valueOb instanceof Double) {
							result = df.format((Double) valueOb);
						} else if (valueOb instanceof Float) {
							result = df.format((Float) valueOb);
						} else {
							result = null != valueOb ? valueOb.toString() : "";
						}
						doingPage.getMetaMap().computeIfAbsent(field, mapKey -> new ArrayList<>()).add(result);
					}
				}
			}
		}
	}

	private void doTable(Page doingPage,String sid,String propertyId,String presellId,String buildingId){
		String jsUrl = StringUtils.replace(houseStatusJsUrlTemplate, sidTemplate, sid);
		jsUrl = StringUtils.replace(jsUrl, projectIdTemplate, propertyId);
		jsUrl = StringUtils.replace(jsUrl, presellIdTemplate, presellId);
		jsUrl = StringUtils.replace(jsUrl, buildingidTemplate, buildingId);
		String dataUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), jsUrl);
		Page houseJsPage = new Page(doingPage.getSiteCode(), 1, dataUrl, dataUrl);
		houseJsPage.setReferer(doingPage.getFinalUrl());
		houseJsPage.setMethod(HttpMethod.GET);
		getDowner().down(houseJsPage);
		String js = houseJsPage.getPageSrc();
		js = StringUtils.remove(js, "document.writeln(");
		js = StringUtils.remove(js, ");");
		js = JsUtils.evalJs(js);
		
		Element tablesHtml = Jsoup.parse(js);
		Elements tables = tablesHtml.select("body>table");
		for (Element table : tables) {
			Elements unitTds = table.select("tbody>tr:eq(0)>td:gt(0)");
			List<String> unitList = new ArrayList<>();
			for (Element unitTd : unitTds) {
				String unitName = unitTd.text();
				if (StringUtils.contains(unitName, "单元")) {
					unitList.add(unitName);
				}
			}
			Element unitHouseTrElement = table.select("tbody>tr:eq(1)").first();
			if (null != unitHouseTrElement) {
				Elements unitHouseTdElements = JsoupUtils.children(unitHouseTrElement, "td");
				for (int i = 1; i < unitHouseTdElements.size(); i++) {
					String unitName = StringUtils.EMPTY;
					int unitIndex = i - 1;
					if (unitIndex < unitList.size()) {
						unitName = unitList.get(unitIndex);
					}
					Element unitHouseElement = unitHouseTdElements.get(i);
					Elements houseElements = unitHouseElement.select("a");
					for (Element element : houseElements) {
						String housePageUrl = element.attr("href");
						if (StringUtils.isNotBlank(housePageUrl)) {
							String houseId = element.attr("id");
							if (StringUtils.isNotBlank(houseId)) {
								houseId = StringUtils.replace(houseId, "H", "");
							} else {
								houseId = StringUtils.substringAfterLast(housePageUrl, "_");
								houseId = StringUtils.remove(houseId, ".htm");
							}

							String houseNo = element.text();
							String onmouseoverHtml = element.attr("onmouseover");
							String houseData = StringUtils.substringBetween(onmouseoverHtml,
									"<table><tr><td width=240>", "</td><td width=150>tupian</td>");
							houseData = StringUtils.replace(houseData, "<br/>", "");
							houseData = StringUtils.replace(houseData, "　", "");
							houseData = StringUtils.replace(houseData, " ", "");
							houseData = StringUtils.replace(houseData, "：", "");
							houseData = StringUtils.replace(houseData, ":", "");
							String houseStatus = StringUtils.substringBetween(houseData, "当前状态", "房屋用途");
							String houseUsage = StringUtils.substringBetween(houseData, "房屋用途", "建筑面积");
							String buildingArea = StringUtils.substringBetween(houseData, "建筑面积", "毛坯单价");
							String roughPrice = StringUtils.substringBetween(houseData, "毛坯单价", "总价");
							String totalPrice = StringUtils.substringBetween(houseData, "总价", "房屋坐落");
							String houseAddress = StringUtils.substringAfterLast(houseData, "房屋坐落");

							doingPage.getMetaMap().computeIfAbsent("unitName", mapKey -> new ArrayList<>())
									.add(unitName);

							doingPage.getMetaMap().computeIfAbsent("houseId", mapKey -> new ArrayList<>())
									.add(houseId);

							doingPage.getMetaMap().computeIfAbsent("houseNo", mapKey -> new ArrayList<>())
									.add(houseNo);
							doingPage.getMetaMap().computeIfAbsent("status", mapKey -> new ArrayList<>())
									.add(houseStatus);
							doingPage.getMetaMap().computeIfAbsent("houseUsage", mapKey -> new ArrayList<>())
									.add(houseUsage);
							doingPage.getMetaMap().computeIfAbsent("buildingArea", mapKey -> new ArrayList<>())
									.add(buildingArea);
							doingPage.getMetaMap().computeIfAbsent("roughPrice", mapKey -> new ArrayList<>())
									.add(roughPrice);
							doingPage.getMetaMap().computeIfAbsent("totalPrice", mapKey -> new ArrayList<>())
									.add(totalPrice);
							doingPage.getMetaMap().computeIfAbsent("houseAddress", mapKey -> new ArrayList<>())
									.add(houseAddress);

						}

					}

				}

			}
		}
	}

	private boolean isTableLayout(Page doingPage) {
		return getDivLayoutDivs(doingPage).isEmpty() ? true : false;
	}

	private Elements getDivLayoutDivs(Page doingPage) {
		return doingPage.getDoc().select("div[id=PropertyTable]>div[class=raphael_box]");
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

		List<String> buildingNames = resultContext.getExtractResult("buildingName");

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

		// List<String> houseInfoUrls =
		// resultContext.getExtractResult("houseInfoUrl");
		// if (null != houseInfoUrls) {
		// String houseId = null;
		// String houseNo = null;
		// String internalId = null;
		// String unitName = null;
		// String houseUsage = null;
		// String buildingArea = null;
		// String roughPrice = null;
		// String totalPrice = null;
		// String houseAddress = null;
		// String internalArea = null;
		// String houseStyle = null;
		// String buildingName = null;
		// for (int i = 0; i < houseInfoUrls.size(); i++) {
		// String houseInfoUrl = houseInfoUrls.get(i);
		// houseId = houseIds.get(i);
		// houseNo = houseNos.get(i);
		// internalId = internalIds.get(i);
		// houseNo = houseNos.get(i);
		// unitName = unitNames.get(i);
		// houseUsage = houseUsages.get(i);
		// buildingArea = buildingAreas.get(i);
		// roughPrice = roughPrices.get(i);
		// totalPrice = totalPrices.get(i);
		// houseAddress = addresss.get(i);
		// internalArea = internalAreas.get(i);
		// houseStyle = houseStyles.get(i);
		// buildingName = buildingNames.get(i);
		//
		// Page houseInfoPage = new Page(getSite().getCode(), 1, houseInfoUrl,
		// houseInfoUrl);
		// houseInfoPage.setReferer(doingPage.getFinalUrl());
		//
		// houseInfoPage.getMetaMap().put("sid", ArrayListUtils.asList(sid));
		// houseInfoPage.getMetaMap().put("propertyId",
		// ArrayListUtils.asList(propertyId));
		// houseInfoPage.getMetaMap().put("projectName",
		// ArrayListUtils.asList(projectName));
		// houseInfoPage.getMetaMap().put("presellName",
		// ArrayListUtils.asList(presellName));
		// houseInfoPage.getMetaMap().put("presellCode",
		// ArrayListUtils.asList(presellCode));
		// houseInfoPage.getMetaMap().put("buildingName",
		// ArrayListUtils.asList(buildingName));
		// houseInfoPage.getMetaMap().put("houseNo",
		// ArrayListUtils.asList(houseNo));
		// houseInfoPage.getMetaMap().put("houseId",
		// ArrayListUtils.asList(houseId));
		// houseInfoPage.getMetaMap().put("internalId",
		// ArrayListUtils.asList(internalId));
		// houseInfoPage.getMetaMap().put("unitName",
		// ArrayListUtils.asList(unitName));
		// houseInfoPage.getMetaMap().put("houseUsage",
		// ArrayListUtils.asList(houseUsage));
		// houseInfoPage.getMetaMap().put("buildingArea",
		// ArrayListUtils.asList(buildingArea));
		// houseInfoPage.getMetaMap().put("roughPrice",
		// ArrayListUtils.asList(roughPrice));
		//
		// houseInfoPage.getMetaMap().put("totalPrice",
		// ArrayListUtils.asList(totalPrice));
		// houseInfoPage.getMetaMap().put("houseAddress",
		// ArrayListUtils.asList(houseAddress));
		// houseInfoPage.getMetaMap().put("internalArea",
		// ArrayListUtils.asList(internalArea));
		// houseInfoPage.getMetaMap().put("houseStyle",
		// ArrayListUtils.asList(houseStyle));
		//
		// if (!houseInfoQueue.isDone(houseInfoPage.getPageKey())) {
		// houseInfoQueue.push(houseInfoPage);
		// }
		// }
		// }
		if (null != houseNos && houseNos.size() > 0) {
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
			String buildingName = null;

			for (int i = 0; i < houseNos.size(); i++) {
				houseId = houseIds.get(i);
				houseNo = houseNos.get(i);
				internalId = internalIds.get(i);
				unitName = unitNames.get(i);
				houseUsage = houseUsages.get(i);
				buildingArea = buildingAreas.get(i);
				roughPrice = roughPrices.get(i);
				totalPrice = totalPrices.get(i);
				houseAddress = addresss.get(i);
				internalArea = internalAreas.get(i);
				houseStyle = houseStyles.get(i);
				buildingName = buildingNames.get(i);

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
