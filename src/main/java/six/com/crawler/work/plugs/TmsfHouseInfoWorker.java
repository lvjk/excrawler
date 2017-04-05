package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 上午9:08:57
 */
public class TmsfHouseInfoWorker extends AbstractCrawlWorker {

	Map<String, String> jsonKeyMap;
	private String sidTemplate = "<<sid>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";
	private String areaTemplate = "<<area>>";
	private String allpriceTemplate = "<<allprice>>";
	private String housestateTemplate = "<<housestate>>";
	private String housetypeTemplate = "<<housetype>>";
	private String houseJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewPropertyHz_showbox.jspx?" 
			+ "buildingid="+ buildingidTemplate 
			+ "&presellid=" + presellIdTemplate 
			+ "&sid=" + sidTemplate 
			+ "&area=" + areaTemplate
			+ "&allprice=" + allpriceTemplate 
			+ "&housestate=" + housestateTemplate 
			+ "&housetype=" + housetypeTemplate;
	private String sidCss = "input[id=sid]";
	private String areaCss = "input[id=area]";
	private String allpriceCss = "input[id=allprice]";
	private String housestateCss = "input[id=housestate]";
	private String housetypeCss = "input[id=housetype]";

	@Override
	protected void insideInit() {
		jsonKeyMap = new HashMap<>();
		jsonKeyMap.put("buildingName", "buildingname");
		jsonKeyMap.put("unitName", "unitname");
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
		jsonKeyMap.put("floor", "floor");
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
		String internalidTemplate = "<<internalid>>";
		String houseDivCssTemplate = "div[id=lpb_" + internalidTemplate + "]";
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
			String internalidOb = houseMap.get("internalid").toString();
			if (null == internalidOb || StringUtils.isBlank(internalidOb.toString())) {
				throw new RuntimeException("don't find  house internalid ");
			}
			String internalid = internalidOb.toString();
			String houseDivCss = StringUtils.replace(houseDivCssTemplate, internalidTemplate, internalid);
			Element houseDiv = doingPage.getDoc().select(houseDivCss).first();
			/**
			 * 如果没有拿到楼层div那么此条数据 是没有在页面显示的
			 */
			if (null == houseDiv) {
				continue;
				//throw new RuntimeException("don't find  house's div");
			}
			String houseFloorKey = houseDiv.attr("floor");
			String houseFloor = floorMap.get(houseFloorKey);
			if(null==houseFloor){
				continue;
				//throw new RuntimeException("don't find  house's floor");
			}
			houseMap.put("floor", houseFloor);
			
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

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
