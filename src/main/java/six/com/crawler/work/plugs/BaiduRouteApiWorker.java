package six.com.crawler.work.plugs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 下午4:00:47
 */
public class BaiduRouteApiWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(BaiduRouteApiWorker.class);
	private String startCity = "上海";
	private String endCity = "上海";
	private String startLongitude = "121.93047185079";
	private String startLatitude = "30.9213703312958";
	private String modeTemplate = "<<mode>>";
	private String startLongitudeTemplate = "<<startLongitude>>";
	private String startLatitudeTemplate = "<<startLatitude>>";
	private String endLongitudeTemplate = "<<endLongitude>>";
	private String endLatitudeTemplate = "<<endLatitude>>";
	private String startCityTemplate = "<<startCity>>";
	private String endCityTemplate = "<<endCity>>";
	private String akTemplate = "<<ak>>";
	Map<String, String> jsonKeyMap;
	RedisWorkQueue routeStepInfoQueue;

	@Override
	protected void insideInit() {
		jsonKeyMap = new HashMap<>();
		jsonKeyMap.put("buildingName", "buildingname");
		jsonKeyMap.put("unitName", "unitname");
		jsonKeyMap.put("houseId", "houseid");
		jsonKeyMap.put("houseNo", "houseno");
		jsonKeyMap.put("status", "housestatename");
		List<String> modes = new ArrayList<>();
		modes.add("driving");
		modes.add("transit");

		List<String> aks = new ArrayList<>();
		aks.add("pcG3mRmGb2NmAtGL1HbzruDsE7Rr6osH");
		aks.add("BIwtU5ZlDaXYlM0pcebvF9KwGB7YkKgR");
		aks.add("0zgSbUShzGbOB70SquamKtPW1BuTaiU9");
		aks.add("MbYCmKFz6gQC66eAIEjuwndyA8vTgRW4");
		

		String url = "http://api.map.baidu.com/direction/v1?" + "mode=driving" + "&origin=" + startLatitudeTemplate
				+ "," + startLongitudeTemplate + "&destination=" + endLatitudeTemplate + "," + endLongitudeTemplate
				+ "&origin_region=" + startCityTemplate + "&destination_region=" + endCityTemplate + "&output=json"
				+ "&ak=" + akTemplate;
		List<String> longitudes = null;
		List<String> latitudes = null;
		try {
			longitudes = FileUtils.readLines(new File("C:/Users/38134/Desktop/masa需求/longitude.txt"));
			latitudes = FileUtils.readLines(new File("C:/Users/38134/Desktop/masa需求/latitude.txt"));
		} catch (IOException e) {
			log.error("read longitudes or latitudes err", e);
			throw new RuntimeException("read longitudes or latitudes err", e);
		}
		String endLongitude = null;
		String endLatitude = null;
		String ak = null;

		int requestCount = modes.size() * longitudes.size();
		int avgAkRequestCount = requestCount / aks.size();
		int requestCountIndex = 0;
		int akIndex = 0;
		for (String mode : modes) {
			for (int i = 0; i < longitudes.size(); i++) {
				endLongitude = longitudes.get(i);
				endLatitude = latitudes.get(i);
				if (requestCountIndex > avgAkRequestCount) {
					akIndex++;
					if (akIndex >= aks.size()) {
						akIndex = 0;
					}
				}
				ak = aks.get(akIndex);
				String requestUrl = StringUtils.replace(url, modeTemplate, mode);
				requestUrl = StringUtils.replace(requestUrl, akTemplate, ak);

				requestUrl = StringUtils.replace(requestUrl, startLongitudeTemplate, startLongitude);
				requestUrl = StringUtils.replace(requestUrl, startLatitudeTemplate, startLatitude);

				requestUrl = StringUtils.replace(requestUrl, endLongitudeTemplate, endLongitude);
				requestUrl = StringUtils.replace(requestUrl, endLatitudeTemplate, endLatitude);

				requestUrl = StringUtils.replace(requestUrl, startCityTemplate, startCity);
				requestUrl = StringUtils.replace(requestUrl, endCityTemplate, endCity);

				Page requestPage = new Page(getSite().getCode(), 1, requestUrl, requestUrl);
				requestPage.getMetaMap().put("mode",Arrays.asList(mode));
				requestPage.getMetaMap().put("startLongitude",Arrays.asList(startLongitude));
				requestPage.getMetaMap().put("startLatitude",Arrays.asList(startLatitude));
				
				requestPage.getMetaMap().put("endLongitude",Arrays.asList(endLongitude));
				requestPage.getMetaMap().put("endLatitude",Arrays.asList(endLatitude));
				
				requestPage.getMetaMap().put("startCity",Arrays.asList(startCity));
				requestPage.getMetaMap().put("endCity",Arrays.asList(endCity));
				if(!getWorkQueue().duplicateKey(requestPage.getPageKey())){
					getWorkQueue().push(requestPage);
				}
				requestCountIndex++;
			}
		}
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String json = doingPage.getPageSrc();
		json = StringUtils.remove(json, "<b>");
		json = StringUtils.remove(json, "<\\\\/b>");
//		StringBuffer string = new StringBuffer();
//		String[] hexs = json.split("\\\\u");
//		for (int i =0; i < hexs.length; i++) {
//			String hex=hexs[i];
//			try{
//				int data = Integer.parseInt(hex, 16);// 转换出每一个代码点
//				string.append((char) data);// 追加成string
//			}catch (Exception e) {
//				string.append(hex);// 追加成string
//			}
//		}
//		json=string.toString();
		Map<String, Object> map=(Map<String, Object>)JSONUtils.parse(json);
		Map<String, Object> result = (Map<String, Object>) map.get("result");
		List<Map<String, Object>> routes = (List<Map<String, Object>>) result.get("routes");
		Map<String, Object> route = routes.get(0);

		List<String> modes = new ArrayList<>();
		List<String> startCitys = new ArrayList<>();
		List<String> endCitys = new ArrayList<>();
		List<String> startLongitudes = new ArrayList<>();
		List<String> startLatitudes = new ArrayList<>();
		List<String> endtLongitudes = new ArrayList<>();
		List<String> endLatitudes = new ArrayList<>();
		List<String> durations = new ArrayList<>();
		List<String> distances = new ArrayList<>();
		List<String> stepSerialNubs=new ArrayList<>();
		List<String> stepAreas = new ArrayList<>();
		List<String> stepDirections = new ArrayList<>();
		List<String> stepDistances = new ArrayList<>();
		List<String> stepDurations = new ArrayList<>();
		List<String> stepInstructionses = new ArrayList<>();
		List<String> stepOriginLngs = new ArrayList<>();
		List<String> stepOriginLats = new ArrayList<>();
		List<String> stepDestinationLngs = new ArrayList<>();
		List<String> stepDestinationLats = new ArrayList<>();
		List<String> stepPaths = new ArrayList<>();

		String mode = doingPage.getMetaMap().get("mode").get(0);
		String startCity = doingPage.getMetaMap().get("startCity").get(0);
		String endCity = doingPage.getMetaMap().get("endCity").get(0);
		String startLongitude = doingPage.getMetaMap().get("startLongitude").get(0);
		String startLatitude = doingPage.getMetaMap().get("startLatitude").get(0);
		String endLongitude = doingPage.getMetaMap().get("endLongitude").get(0);
		String endLatitude = doingPage.getMetaMap().get("endLatitude").get(0);
		String duration = route.get("duration").toString();
		String distance = route.get("distance").toString();
		List<Map<String, Object>> steps = (List<Map<String, Object>>) route.get("steps");
		int stepSerialNub=0;
		for (Map<String, Object> stepMap : steps) {
			String stepArea = stepMap.get("area").toString();
			String stepDirection = stepMap.get("direction").toString();

			String stepDistance = stepMap.get("distance").toString();
			String stepDuration = stepMap.get("duration").toString();

			String stepInstructions = stepMap.get("instructions").toString();

			Map<String, Object> stepOriginLngAndLat = (Map<String, Object>) stepMap.get("stepOriginLocation");
			String stepOriginLng = stepOriginLngAndLat.get("lng").toString();
			String stepOriginLat = stepOriginLngAndLat.get("lat").toString();

			Map<String, Object> stepDestinationLngAndLat = (Map<String, Object>) stepMap.get("stepDestinationLocation");
			String stepDestinationLng = stepDestinationLngAndLat.get("lng").toString();
			String stepDestinationLat = stepDestinationLngAndLat.get("lat").toString();
			String stepPath = stepMap.get("path").toString();
			stepSerialNub++;
			modes.add(mode);
			startCitys.add(startCity);
			endCitys.add(endCity);
			startLongitudes.add(startLongitude);
			startLatitudes.add(startLatitude);
			endtLongitudes.add(endLongitude);
			endLatitudes.add(endLatitude);
			durations.add(duration);
			distances.add(distance);
			stepSerialNubs.add(String.valueOf(stepSerialNub));
			stepAreas.add(stepArea);
			stepDirections.add(stepDirection);
			stepDistances.add(stepDistance);
			stepDurations.add(stepDuration);
			stepInstructionses.add(stepInstructions);
			stepOriginLngs.add(stepOriginLng);
			stepOriginLats.add(stepOriginLat);
			stepDestinationLngs.add(stepDestinationLng);
			stepDestinationLats.add(stepDestinationLat);
			stepPaths.add(stepPath);
		}
		doingPage.getMetaMap().put("mode", modes);
		doingPage.getMetaMap().put("startCity", startCitys);
		doingPage.getMetaMap().put("endCity", endCitys);
		doingPage.getMetaMap().put("startLongitude", startLongitudes);
		doingPage.getMetaMap().put("startLatitude", startLatitudes);
		doingPage.getMetaMap().put("endtLongitude", endtLongitudes);
		doingPage.getMetaMap().put("endLatitude", endLatitudes);
		doingPage.getMetaMap().put("duration", durations);
		doingPage.getMetaMap().put("distance", distances);
		doingPage.getMetaMap().put("stepSerialNub", stepSerialNubs);
		doingPage.getMetaMap().put("stepArea", stepAreas);
		doingPage.getMetaMap().put("stepDirection", stepDirections);
		doingPage.getMetaMap().put("stepDistance", stepDistances);
		doingPage.getMetaMap().put("stepDuration", stepDurations);
		doingPage.getMetaMap().put("stepInstructions", stepInstructionses);
		doingPage.getMetaMap().put("stepOriginLng", stepOriginLngs);
		doingPage.getMetaMap().put("stepOriginLat", stepOriginLats);
		doingPage.getMetaMap().put("stepDestinationLng", stepDestinationLngs);
		doingPage.getMetaMap().put("stepDestinationLat", stepDestinationLats);
		doingPage.getMetaMap().put("stepPath", stepPaths);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected boolean insideOnError(Exception e, Page doingPage) {
		return false;
	}

}
