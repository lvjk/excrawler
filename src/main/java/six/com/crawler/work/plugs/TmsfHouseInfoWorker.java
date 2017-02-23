package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 上午9:08:57
 */
public class TmsfHouseInfoWorker extends AbstractCrawlWorker {

	Map<String, String> jsonKeyMap;

	public TmsfHouseInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		jsonKeyMap = new HashMap<>();
		jsonKeyMap.put("buildingName", "buildingname");
		jsonKeyMap.put("houseId", "houseno");
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
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		String floorDivCss = "div[class=raphael_box][types=1]";
		Elements floorDivs = doc.select(floorDivCss);
		Map<String, String> floorMap = new HashMap<>();
		for (Element floorDiv : floorDivs) {
			String floorKey = floorDiv.attr("floor");
			String floor = floorDiv.attr("title");
			floorMap.put(floorKey, floor);
		}
		String internalidTemplate = "<<internalid>>";
		String houseDivCssTemplate = "div[id=lpb_" + internalidTemplate + "]";
		String presaleJson = doingPage.getPageSrc();
		Map<String, Object> map = JsonUtils.toObject(presaleJson, Map.class);
		List<Map<String, Object>> houseList = (List<Map<String, Object>>) map.get("list");
		for (Map<String, Object> houseMap : houseList) {
			String internalid = jsonKeyMap.get("internalid");
			String houseDivCss = StringUtils.replace(houseDivCssTemplate, internalidTemplate, internalid);
			Element houseDiv = doc.select(houseDivCss).first();
			String houseFloorKey = houseDiv.attr("floor");
			String houseFloor = floorMap.get(houseFloorKey);
			houseMap.put("floor", houseFloor);
			for (String field : jsonKeyMap.keySet()) {
				String jsonKey = jsonKeyMap.get(field);
				Object value = (Map<String, Object>) houseMap.get(jsonKey);
				doingPage.getMetaMap().computeIfAbsent(field, mapKey -> new ArrayList<>())
						.add(null != value ? value.toString() : "");
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage,ResultContext resultContext) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

}
