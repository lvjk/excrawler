package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月4日 下午4:51:31
 */
public class Cq315houseHouseStateWorker extends AbstractCrawlWorker {

	RedisWorkQueue houseInfoQueue;

	@Override
	protected void insideInit() {
		houseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "cq315house_house_info");
	}

	protected void beforeDown(Page page) {

	}

	private String getState(long value, List<Map<String, Object>> stateList) {
		for (int i = stateList.size() - 1; i >= 0; i--) {
			Map<String, Object> state = stateList.get(i);
			int showType = (int) state.get("showType");
			int val = (int) state.get("val");
			String name = (String) state.get("name");
			if (showType == 0) {
				if ((value & val) == val) {
					if (name == "可售") {
						if ((524288 & val) == 524288) {
							boolean ispass = ((524292 & value) == 524292) ? ((7518186 & value) == 0) : false;
							if (!ispass) {
								continue;
							} else {
								return name;
							}
						}
						if ((262144 & val) == 262144) {
							boolean ispass = ((262146 & value) == 262146) ? ((7516136 & value) == 0) : false;
							if (!ispass) {
								continue;
							} else {
								return name;
							}
						}
					} else {
						return name;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		List<String> tempPresellId = doingPage.getMetaMap().get("presellId");
		List<String> tempBuildingUnit = doingPage.getMetaMap().get("buildingUnit");
		String presellId = tempPresellId.get(0);
		String buildingUnit = tempBuildingUnit.get(0);
		List<String> houseIds = new ArrayList<>();
		List<String> presellIds = new ArrayList<>();
		List<String> buildingUnits = new ArrayList<>();
		List<String> unitNums = new ArrayList<>();
		List<String> houseNums = new ArrayList<>();
		List<String> states = new ArrayList<>();
		List<String> physicalLayers = new ArrayList<>();
		List<String> logicLayers = new ArrayList<>();
		String stateUrl = "http://www.cq315house.com/315web/webservice/jsonstatus.ashx";
		Page statePage = new Page(doingPage.getSiteCode(), 1, stateUrl, stateUrl);
		statePage.setReferer(doingPage.getFinalUrl());
		statePage.setType(PageType.JSON.value());
		getDowner().down(statePage);
		String stateJson = statePage.getPageSrc();
		stateJson = "{\"stateJson\":" + stateJson;
		stateJson = stateJson + "}";
		Map<String, Object> stateMap = JsonUtils.toObject(stateJson, Map.class);
		List<Map<String, Object>> stateList = (List<Map<String, Object>>) stateMap.get("stateJson");
		String jsonDataCss = "input[id=DataHF]";
		Element jsonDataElement = doc.select(jsonDataCss).first();
		String jsonData = jsonDataElement.attr("value");
		jsonData = StringUtils.replace(jsonData, "&quot;", "\"");
		jsonData = "{\"json\":" + jsonData;
		jsonData = jsonData + "}";
		Map<String, Object> jsonMap = JsonUtils.toObject(jsonData, Map.class);
		List<Map<String, Object>> arrasMap = (List<Map<String, Object>>) jsonMap.get("json");
		for (Map<String, Object> tempMap : arrasMap) {
			List<Map<String, Object>> rooms = (List<Map<String, Object>>) tempMap.get("rooms");
			for (Map<String, Object> room : rooms) {
				String houseId = room.get("id").toString();// id
				String logicLayer = room.get("flr").toString();// 名义层
				String rn = room.get("rn").toString();// 房屋号
				String houseNum = logicLayer + "-" + rn;
				String physicalLayer = room.get("y").toString();// 物理层
				String unitNum = room.get("unitnumber").toString();// 单元号
				Object stateOb = room.get("status");
				String state = null;
				long tempState = 0;
				if (stateOb instanceof Double) {
					String stateStr = stateOb.toString();
					Double d = Double.valueOf(stateStr);
					tempState = d.longValue();// 状态
				} else {
					String stateStr = stateOb.toString();
					tempState = Long.valueOf(stateStr);
				}
				state = getState(tempState, stateList);
				if (null == state) {
					continue;
				}
				houseIds.add(houseId);
				presellIds.add(presellId);
				buildingUnits.add(buildingUnit);
				unitNums.add(unitNum);
				houseNums.add(houseNum);
				states.add(state);
				physicalLayers.add(physicalLayer);
				logicLayers.add(logicLayer);
			}
		}
		doingPage.getMetaMap().put("houseId", houseIds);
		doingPage.getMetaMap().put("presellId_2", presellIds);
		doingPage.getMetaMap().put("buildingUnit", buildingUnits);
		doingPage.getMetaMap().put("unitNum", unitNums);
		doingPage.getMetaMap().put("houseNum", houseNums);
		doingPage.getMetaMap().put("status", states);
		doingPage.getMetaMap().put("physicalLayer", physicalLayers);
		doingPage.getMetaMap().put("logicLayer", logicLayers);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	public void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> houseIds = resultContext.getExtractResult("houseId");
		for (int i = 0; i < houseIds.size(); i++) {
			String systemHouseId = resultContext.getOutResults().get(i).get(Constants.DEFAULT_RESULT_ID);
			String houseId = houseIds.get(i);
			String houseInfoUrl = "../YanZhengCode/YanZhengPage.aspx?fid=" + houseId;
			houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseInfoUrl);
			Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
			houseInfoPage.setReferer(doingPage.getFinalUrl());
			houseInfoPage.setType(PageType.DATA.value());
			houseInfoPage.getMetaMap().put("houseId", Arrays.asList(systemHouseId));
			if (!houseInfoQueue.duplicateKey(houseInfoPage.getPageKey())) {
				houseInfoQueue.push(houseInfoPage);
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
