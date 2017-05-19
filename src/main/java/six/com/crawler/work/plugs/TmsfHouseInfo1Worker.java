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
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月7日 下午1:22:43 
*/
public class TmsfHouseInfo1Worker extends AbstractCrawlWorker {

	Map<String, String> jsonKeyMap;

	private String sidTemplate = "<<sid>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";

	String houseInfoUrlTemplate = "http://www.tmsf.com/newhouse/NewProperty_showbox.jspx?"
			+ "buildingid="+buildingidTemplate
			+ "&presellid="+presellIdTemplate
			+ "&sid="+sidTemplate;

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
		String floorDivCss = "div[class=raphael_box][types=1]";
		Elements floorDivs = doingPage.getDoc().select(floorDivCss);
		Map<String, String> floorMap = new HashMap<>();
		for (Element floorDiv : floorDivs) {
			String floorKey = floorDiv.attr("floor");
			String floor = floorDiv.attr("title");
			floorMap.put(floorKey, floor);
		}
		
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
		
		String houseInfoUrl = StringUtils.replace(houseInfoUrlTemplate, sidTemplate, sid);
		houseInfoUrl = StringUtils.replace(houseInfoUrl, presellIdTemplate, presellId);
		houseInfoUrl = StringUtils.replace(houseInfoUrl, buildingidTemplate, buildingId);

		Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
		houseInfoPage.setReferer(doingPage.getFinalUrl());
		houseInfoPage.setMethod(HttpMethod.GET);
		houseInfoPage.setType(PageType.JSON.value());
		houseInfoPage.getMetaMap().put("buildingId", ArrayListUtils.asList(buildingId));
		houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
		getDowner().down(houseInfoPage);
		String internalidTemplate = "<<internalid>>";
		String houseDivCssTemplate = "div[id=lpb_" + internalidTemplate + "]";
		String houseInfoJson = houseInfoPage.getPageSrc();
		Map<String, Object> map = JsonUtils.toObject(houseInfoJson, Map.class);
		List<Map<String, Object>> houseList = (List<Map<String, Object>>) map.get("list");
		List<String> presellIds=new ArrayList<>();
		String sysPresellId=doingPage.getMeta("presellId").get(0);
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
			presellIds.add(sysPresellId);
		}                           
		doingPage.getMetaMap().put("presellId",presellIds);
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
