package six.com.crawler.work.plugs;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月2日 下午4:57:46
 */
public class HeatQQWorker extends AbstractCrawlWorker {

	String dateTemplate = "<<date>>";
	String cityIdTemplate = "<<cityId>>";
	String directionTemplate = "<<direction>>";
	String typeTemplate = "<<type>>";
	String searchUrlTemplate = "http://lbs.gtimg.com/maplbs/qianxi/" + dateTemplate + "/" + cityIdTemplate
			+ directionTemplate + typeTemplate + ".js";
	String referer = "https://heat.qq.com/qianxi/index.html";
	String cityDateUrl = "https://heat.qq.com/qianxi/js/data/city.js";
	String dateFormat = "yyyyMMdd";
	//20150201 20170302
	String startDateStr;
	String endDateStr;
	NumberFormat ptNf = NumberFormat.getPercentInstance();

	static class SearchParam {
		String city;
		String cityId;
		Date date;
		String direction;// 0迁入 1迁出
		String type;// 1汽车 2火车 3飞机 6全部
	}

	@Override
	protected void insideInit() {
		startDateStr =getJob().getParam("startDateStr");
		endDateStr = getJob().getParam("endDateStr");
		initSearchParam();
	}

	@SuppressWarnings("unchecked")
	protected void initSearchParam() {
		Map<String, String> directionMap = new HashMap<>();
		directionMap.put("迁入", "0");
		directionMap.put("迁出", "1");
		Map<String, String> typeMap = new HashMap<>();
		typeMap.put("汽车", "1");
		typeMap.put("火车", "2");
		typeMap.put("飞机", "3");
		typeMap.put("全部", "6");
		List<String> cityList = new ArrayList<>();
		cityList.add("杭州");
		cityList.add("上海");
		cityList.add("宁波");
		cityList.add("温州");
		cityList.add("嘉兴");
		cityList.add("湖州");
		cityList.add("绍兴");
		cityList.add("金华");
		cityList.add("衢州");
		cityList.add("舟山");
		cityList.add("台州");
		cityList.add("丽水");
		cityList.add("合肥");
		cityList.add("滁州");
		cityList.add("马鞍山");
		cityList.add("芜湖");
		cityList.add("宣城");
		cityList.add("黄山");
		cityList.add("南京");
		cityList.add("无锡");
		cityList.add("苏州");
		cityList.add("常州");
		cityList.add("镇江");
		cityList.add("南通");
		cityList.add("泰州");
		cityList.add("扬州");
		cityList.add("盐城");
		cityList.add("淮安");
		cityList.add("宿迁");
		cityList.add("徐州");
		cityList.add("连云港");
		List<Date> dateList = new ArrayList<>();
		try {
			Date startDate = DateUtils.parseDate(startDateStr, dateFormat);
			Date endDate = DateUtils.parseDate(endDateStr, dateFormat);
			dateList.add(startDate);
			int i = 1;
			while (true) {
				Date date = DateUtils.addDays(startDate, i++);
				if (!date.before(endDate)) {
					break;
				} else {
					dateList.add(date);
				}
			}
			dateList.add(endDate);
		} catch (Exception e) {
			throw new RuntimeException("init err", e);
		}

		Page cityDatePage = new Page(getSite().getCode(), 1, cityDateUrl, cityDateUrl);
		cityDatePage.setReferer(referer);
		getDowner().down(cityDatePage);
		String cityDataJson = cityDatePage.getPageSrc();
		cityDataJson = StringUtils.removeStart(cityDataJson, "define(");
		cityDataJson = StringUtils.removeEnd(cityDataJson, ");");
		Map<String, Object> cityMap = JsonUtils.toObject(cityDataJson, Map.class);
		getWorkQueue().clearDoing();
		for (String direction : directionMap.keySet()) {
			String directionValue = directionMap.get(direction);
			for (String type : typeMap.keySet()) {
				String typeValue = typeMap.get(type);
				for (String city : cityList) {
					String cityId = null;
					for (String cityName : cityMap.keySet()) {
						if (StringUtils.contains(cityName, city) || StringUtils.contains(city, cityName)) {
							List<Object> info = (List<Object>) cityMap.get(cityName);
							cityId = info.get(2).toString();
							break;
						}
					}
					for (Date date : dateList) {
						String searchUrl = StringUtils.replace(searchUrlTemplate, cityIdTemplate, cityId);
						String dateStr = DateFormatUtils.format(date, dateFormat);
						searchUrl = StringUtils.replace(searchUrl, dateTemplate, dateStr);
						searchUrl = StringUtils.replace(searchUrl, directionTemplate, directionValue);
						searchUrl = StringUtils.replace(searchUrl, typeTemplate, typeValue);
						Page searchPage = new Page(getSite().getCode(), 1, searchUrl, searchUrl);
						searchPage.setReferer(referer);
						searchPage.getMetaMap().put("fromCity", Arrays.asList(city));
						searchPage.getMetaMap().put("date", Arrays.asList(dateStr));
						searchPage.getMetaMap().put("type", Arrays.asList(type));
						searchPage.getMetaMap().put("direction", Arrays.asList(direction));
						if(!getWorkQueue().isDone(searchPage.getPageKey())){
							getWorkQueue().push(searchPage);
						}
					}
				}
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
		json = StringUtils.removeStart(json, "JSONP_LOADER&&JSONP_LOADER([");
		json = StringUtils.removeEnd(json, ",])");
		json = "{'list':[" + json + "]}";
		Map<String, Object> jsonMap = JsonUtils.toObject(json, Map.class);
		List<Object> jsonList = (List<Object>) jsonMap.get("list");
		String fromCity = doingPage.getMetaMap().get("fromCity").get(0);
		String date = doingPage.getMetaMap().get("date").get(0);
		String type = doingPage.getMetaMap().get("type").get(0);
		String direction = doingPage.getMetaMap().get("direction").get(0);
		Map<String, List<String>> resultMap = new HashMap<>();
		DecimalFormat df = new DecimalFormat("0.0");
		for (int i = 0; i < jsonList.size(); i++) {
			List<Object> dataList = (List<Object>) jsonList.get(i);
			String toCity = dataList.get(0).toString();
			Double heat = Double.valueOf(dataList.get(1).toString());
			heat = Math.sqrt(heat) * 10 / 100;
			String heatStr = df.format(heat);
			String carMoveOut = "";
			String trainMoveOut = "";
			String planeMoveOut = "";
			if (StringUtils.contains("汽车", type)) {
				Double carMoveOutd = Double.valueOf(dataList.get(2).toString());
				carMoveOut = percentage(carMoveOutd);
			} else if (StringUtils.contains("火车", type)) {
				Double trainMoveOutd = Double.valueOf(dataList.get(2).toString());
				trainMoveOut = percentage(trainMoveOutd);
			} else if (StringUtils.contains("飞机", type)) {
				Double planeMoveOutd = Double.valueOf(dataList.get(2).toString());
				planeMoveOut = percentage(planeMoveOutd);
			} else if (StringUtils.contains("全部", type)) {
				Double carMoveOutd = Double.valueOf(dataList.get(2).toString());
				Double trainMoveOutd = Double.valueOf(dataList.get(3).toString());
				Double planeMoveOutd = Double.valueOf(dataList.get(4).toString());
				carMoveOut = percentage(carMoveOutd);
				trainMoveOut = percentage(trainMoveOutd);
				planeMoveOut = percentage(planeMoveOutd);
			}
			resultMap.computeIfAbsent("fromCity", mapkey -> new ArrayList<>()).add(fromCity);
			resultMap.computeIfAbsent("toCity", mapkey -> new ArrayList<>()).add(toCity);
			resultMap.computeIfAbsent("date", mapkey -> new ArrayList<>()).add(date);
			resultMap.computeIfAbsent("type", mapkey -> new ArrayList<>()).add(type);
			resultMap.computeIfAbsent("direction", mapkey -> new ArrayList<>()).add(direction);
			resultMap.computeIfAbsent("heat", mapkey -> new ArrayList<>()).add(heatStr);
			resultMap.computeIfAbsent("carMoveOut", mapkey -> new ArrayList<>()).add(carMoveOut);
			resultMap.computeIfAbsent("trainMoveOut", mapkey -> new ArrayList<>()).add(trainMoveOut);
			resultMap.computeIfAbsent("planeMoveOut", mapkey -> new ArrayList<>()).add(planeMoveOut);
		}
		doingPage.getMetaMap().clear();
		doingPage.getMetaMap().putAll(resultMap);
	}

	private String percentage(double num) {
		return ptNf.format(num);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
