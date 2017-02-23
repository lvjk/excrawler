package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月4日 下午4:51:31
 */
public class Cq315houseHouseStateWorker extends HtmlCommonWorker {

	RedisWorkQueue suiteInfoQueue;
	Map<String, String> stateMap;
	Map<String, String> fieldMap;

	public Cq315houseHouseStateWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		suiteInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "cq315house_house_info");
		stateMap = new HashMap<>();
		stateMap.put("655360", "限制销售");
		stateMap.put("524292", "可售");
		stateMap.put("525316", "预定");
		stateMap.put("2621444", "已售");
		stateMap.put("2398237", "已登记");
		stateMap.put("2398229", "已登记");
		stateMap.put("2361349", "已登记");
		stateMap.put("2394117", "已登记");//
		stateMap.put("2365461", "已登记");
		stateMap.put("524288", "524288");
	}

	@Override
	public void onComplete(Page p) {

	}

	@Override
	public void insideOnError(Exception t, Page p) {
		
	}

	private String getState(long value, List<Map<String, Object>> stateList) {
		for (int i=stateList.size()-1;i>=0;i--){
			Map<String, Object> state=stateList.get(i);
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
	protected void beforePaser(Page doingPage) throws Exception {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		List<String> tempProjectName = doingPage.getMetaMap().get("projectName");
		List<String> tempSaleUnitName = doingPage.getMetaMap().get("company");
		List<String> tempAddress = doingPage.getMetaMap().get("address");
		List<String> tempPresalePermit = doingPage.getMetaMap().get("presalePermit");
		List<String> tempBuildingUnit = doingPage.getMetaMap().get("buildingUnit");
		String projectName = tempProjectName.get(0);
		String saleUnitName = tempSaleUnitName.get(0);
		String address = tempAddress.get(0);
		String presalePermit = tempPresalePermit.get(0);
		String buildingUnit = tempBuildingUnit.get(0);
		List<String> projectNames = new ArrayList<>();
		List<String> saleUnitNames = new ArrayList<>();
		List<String> addresses = new ArrayList<>();
		List<String> presalePermits = new ArrayList<>();
		List<String> buildingUnits = new ArrayList<>();
		List<String> unitNums = new ArrayList<>();
		List<String> houseNums = new ArrayList<>();
		List<String> states = new ArrayList<>();
		List<String> physicalLayers = new ArrayList<>();
		List<String> logicLayers = new ArrayList<>();

		String stateUrl = "http://www.cq315house.com/315web/webservice/jsonstatus.ashx";
		Page statePage=new Page(doingPage.getSiteCode(), 1, stateUrl, stateUrl);
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
				Object stateOb=room.get("status");
				String state=null;
				long tempState=0;
				if(stateOb instanceof Double){
					String stateStr=stateOb.toString();
					Double d=Double.valueOf(stateStr);
					tempState =d.longValue();;// 状态
					
				}else{
					String stateStr=stateOb.toString();
					tempState=Long.valueOf(stateStr);
				}
				state = getState(tempState, stateList);
				if (null == state) {
					continue;
				}
				projectNames.add(projectName);
				saleUnitNames.add(saleUnitName);
				presalePermits.add(presalePermit);
				addresses.add(address);
				buildingUnits.add(buildingUnit);
				unitNums.add(unitNum);
				houseNums.add(houseNum);
				states.add(state);
				physicalLayers.add(physicalLayer);
				logicLayers.add(logicLayer);
				String houseInfoUrl = "../YanZhengCode/YanZhengPage.aspx?fid=" + houseId;
				houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseInfoUrl);
				Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
				houseInfoPage.setReferer(doingPage.getFinalUrl());
				houseInfoPage.setType(PageType.DATA.value());
				houseInfoPage.getMetaMap().put("projectName", Arrays.asList(projectName));
				houseInfoPage.getMetaMap().put("buildingUnit", Arrays.asList(buildingUnit));
				if (!suiteInfoQueue.duplicateKey(houseInfoPage.getPageKey())) {
					suiteInfoQueue.push(houseInfoPage);
				}
			}
		}
		doingPage.getMetaMap().put("projectName", projectNames);
		doingPage.getMetaMap().put("saleUnitName", saleUnitNames);
		doingPage.getMetaMap().put("address", addresses);
		doingPage.getMetaMap().put("presalePermit", presalePermits);
		doingPage.getMetaMap().put("buildingUnit", buildingUnits);
		doingPage.getMetaMap().put("unitNum", unitNums);
		doingPage.getMetaMap().put("houseNum", houseNums);
		doingPage.getMetaMap().put("state", states);
		doingPage.getMetaMap().put("physicalLayer", physicalLayers);
		doingPage.getMetaMap().put("logicLayer", logicLayers);

	}


	@Override
	protected void afterPaser(Page doingPage) throws Exception {

	}

}
