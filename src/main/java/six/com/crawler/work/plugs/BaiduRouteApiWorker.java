package six.com.crawler.work.plugs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import six.com.crawler.work.space.RedisWorkSpace;

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
	RedisWorkSpace<Page> routeStepInfoQueue;
	int akIndex = 0;
	List<String> aks= new ArrayList<String>(){
		private static final long serialVersionUID = 8445868846711096516L;
	{
//		add("YiWu1O7BzBnRUCAmGkoNZ4IFnGhtOTSr");
//		add("IasXYGANDW47rNQBxjEj3e3kZ5sy08Nr");
//		add("165DD4X80fpRYcwRHIAxXyWgIRuusWPQ");
//		add("QMdtjG0DGspcXyNdBYtShHI6q9uxQMa9");
//		add("pcG3mRmGb2NmAtGL1HbzruDsE7Rr6osH");
//		//add("BIwtU5ZlDaXYlM0pcebvF9KwGB7YkKgR");//已被限制
//		add("0zgSbUShzGbOB70SquamKtPW1BuTaiU9");
//		add("MbYCmKFz6gQC66eAIEjuwndyA8vTgRW4");
//		add("ZshtZVOkR0yraBzLwhMangkKwjUwCqKS");
//		add("58tXn4x29OkXYo5ilmG1GfVboSQyKIFS");
//		add("t0FF83ULDf9nOMFPOfZRjos91WBTn2zu");
//		add("kWUDaueq7Vqx8yY5vdYPqbLwY49PfVuX");
//		add("Z2Xr4KYr8XFDPhy0UgTZREC9S119TZuk");
//		add("xnyxmuxcmfwzdhf1fmLE9mQTN8XFlnXW");
//		add("mcanugXZldT2e3XateP7AYoWcIVrzonX");
//		add("2S63Kkmh4Xtlc1QeSrE0KuGAx7CaaByU");
//		add("ECBmNRcf38QUvz3FZr3ypaoYXEWYvPgm");
		add("UNGpPmpwa4yhVLNqWU6Iia2esPtYX9ng");
		add("8Y3hSoR7KyX1djl1NgL1GXr0XpSim0yR");
		add("mvdagzwkgQhKjmwrgxjKD7qP7GrDbSTe");
		add("2NFXA9KflWqYlnMEjcFZheSEcK0LTiQ8");
		
	}};

	@Override
	protected void insideInit() {
		List<String> modes = new ArrayList<>();
		modes.add("driving");
		modes.add("transit");	

		String url = "http://api.map.baidu.com/direction/v1?" 
				+ "mode="+modeTemplate
				+ "&origin=" + startLatitudeTemplate
				+ "," + startLongitudeTemplate + "&destination=" + endLatitudeTemplate + "," + endLongitudeTemplate
				+ "&origin_region=" + startCityTemplate + "&destination_region=" + endCityTemplate + "&output=json"
				+ "&ak=" + akTemplate;
//		List<String> longitudes = null;
//		List<String> latitudes = null;
//		try {
//			longitudes = FileUtils.readLines(new File("C:/Users/38134/Desktop/masa需求/longitude.txt"));
//			latitudes = FileUtils.readLines(new File("C:/Users/38134/Desktop/masa需求/latitude.txt"));
//		} catch (IOException e) {
//			log.error("read longitudes or latitudes err", e);
//			throw new RuntimeException("read longitudes or latitudes err", e);
//		}
//		String endLongitude = null;
//		String endLatitude = null;
//		int addCount=0;
//		for (String mode : modes) {
//			for (int i = 0; i < longitudes.size(); i++) {
//				endLongitude = longitudes.get(i);
//				endLatitude = latitudes.get(i);
//				String requestUrl = StringUtils.replace(url, modeTemplate, mode);
//			
//				requestUrl = StringUtils.replace(requestUrl, startLongitudeTemplate, startLongitude);
//				requestUrl = StringUtils.replace(requestUrl, startLatitudeTemplate, startLatitude);
//
//				requestUrl = StringUtils.replace(requestUrl, endLongitudeTemplate, endLongitude);
//				requestUrl = StringUtils.replace(requestUrl, endLatitudeTemplate, endLatitude);
//
//				requestUrl = StringUtils.replace(requestUrl, startCityTemplate, startCity);
//				requestUrl = StringUtils.replace(requestUrl, endCityTemplate, endCity);
//
//				Page requestPage = new Page(getSite().getCode(), 1, requestUrl, requestUrl);
//				requestPage.getMetaMap().put("mode",Arrays.asList(mode));
//				requestPage.getMetaMap().put("startLongitude",Arrays.asList(startLongitude));
//				requestPage.getMetaMap().put("startLatitude",Arrays.asList(startLatitude));
//				
//				requestPage.getMetaMap().put("endLongitude",Arrays.asList(endLongitude));
//				requestPage.getMetaMap().put("endLatitude",Arrays.asList(endLatitude));
//				
//				requestPage.getMetaMap().put("startCity",Arrays.asList(startCity));
//				requestPage.getMetaMap().put("endCity",Arrays.asList(endCity));
//				if(!getWorkQueue().duplicateKey(requestPage.getPageKey())){
//					if(getWorkQueue().push(requestPage)){
//						log.info("add requestPage succeed");
//					}else{
//						log.info("add requestPage failed");
//					}
//				}else{
//					log.info("requestPage was be do");
//				}
//				addCount++;
//				log.info("add requestPage:"+addCount);
//			}
//		}
	}

	@Override
	protected void beforeDown(Page doingPage) {
		if(akIndex>=aks.size()){
			akIndex=0;
		}
		String ak=aks.get(akIndex++);
		String url=doingPage.getOriginalUrl();
		url=StringUtils.substringBefore(url, "ak=");
		url+="ak="+ak;
		url = StringUtils.replace(url, akTemplate, ak);
		doingPage.setOriginalUrl(url);
		doingPage.setFirstUrl(url);
		doingPage.setFinalUrl(url);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String json = doingPage.getPageSrc();	
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
		Map<String, Object> map=(Map<String, Object>)JSONUtils.parse(json);
		Map<String, Object> result = (Map<String, Object>) map.get("result");
		
		String duration=null;
		String distance =null;
		List<Map<String, Object>> steps =null;
		int stepSerialNub=0;
		if("driving".equals(mode)){
			List<Map<String, Object>> routes = (List<Map<String, Object>>) result.get("routes");
			Map<String, Object> route = routes.get(0);
			duration = route.get("duration").toString();
			distance = route.get("distance").toString();
			steps = (List<Map<String, Object>>) route.get("steps");
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
		}else{
			List<Map<String, Object>> routes = (List<Map<String, Object>>) result.get("routes");
			Map<String, Object> scheme = routes.get(0);
			Map<String, Object> route=(Map<String, Object>)((List<Map<String, Object>>)scheme.get("scheme")).get(0);
			duration = route.get("duration").toString();
			distance = route.get("distance").toString();
			steps = (List<Map<String, Object>>) route.get("steps");
			for(Object stepOb:steps){
				if(stepOb instanceof List){
					List<Map<String, Object>> stepObList=(List<Map<String, Object>>)stepOb;
					if(stepObList.size()>0){
						Map<String, Object> stepMap=((List<Map<String, Object>>)stepOb).get(0);

						String stepArea = "";
						String stepDirection = "";

						String stepDistance = stepMap.get("distance").toString();
						String stepDuration = stepMap.get("duration").toString();
	
						String stepInstructions = stepMap.get("stepInstruction").toString();

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
				}
			}
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
