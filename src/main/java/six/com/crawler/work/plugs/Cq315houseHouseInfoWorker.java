package six.com.crawler.work.plugs;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.ocr.ImageDistinguish;
import six.com.crawler.common.ocr.ImageUtils;
import six.com.crawler.common.utils.JsUtils;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.downer.PostContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月10日 下午3:00:49
 */
public class Cq315houseHouseInfoWorker extends AbstractCrawlWorker {

	private ImageDistinguish imageDistinguish;
	private Map<String, String> fieldMap;

	public Cq315houseHouseInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
		imageDistinguish = getManager().getImageDistinguish();
	}

	@Override
	protected void insideInit() {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("roomNum", "roomNum");// 房号
		fieldMap.put("block", "buildingNum");// 楼号
		fieldMap.put("iArea", "innerArea");// 套内面积
		fieldMap.put("bArea", "buildingArea");// 建筑面积
		fieldMap.put("use", "usage");// 使用用途
		fieldMap.put("rType", "houseType");// 户型
		fieldMap.put("nsjg", "priceOfInner"); // 拟售单价（套内）
		fieldMap.put("nsjmjg", "priceOfBuilding");// 拟售单价（建面）
		fieldMap.put("F_PRESALE_CERT", "presalePermit");// 预售许可证
		fieldMap.put("stru", "buildingStructure");// 建筑结构
		fieldMap.put("S_ISONLINESIGN", "signedStatus");// 签约情况
		fieldMap.put("S_ISOWNERSHIP", "houseStatus");// 房屋状态
	}

	@Override
	public void onComplete(Page p,ResultContext resultContext) {

	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

	protected void beforeDown(Page page) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String validCodeUrlCss = "img[id=imgRandom]";
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		Element validCodeElement = doc.select(validCodeUrlCss).first();
		String validCodeUrl = validCodeElement.attr("src");
		validCodeUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), validCodeUrl);
		Element formElement = doc.select("form[id=form1]").first();
		String formAction = formElement.attr("action");
		formAction = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), formAction);
		String __VIEWSTATE = formElement.select("input[id=__VIEWSTATE]").first().attr("value");
		String __VIEWSTATEGENERATOR = formElement.select("input[id=__VIEWSTATEGENERATOR]").first().attr("value");
		String __EVENTVALIDATION = formElement.select("input[id=__EVENTVALIDATION]").first().attr("value");
		String txtCode = null;
		String Button1 = formElement.select("input[id=Button1]").first().attr("value");
		String hfTableNum = null;

		Page validCodePage = new Page(doingPage.getSiteCode(), 1, validCodeUrl, validCodeUrl);
		validCodePage.setReferer(doingPage.getFinalUrl());
		byte[] imageBytes = getDowner().downBytes(validCodePage);
		BufferedImage croppedImage = ImageUtils.loadImage(imageBytes);
		// ImageUtils.writeImage(new File("F:/test/cq315image",
		// System.currentTimeMillis() + ".gif"), croppedImage);
		try {
			String result = imageDistinguish.distinguish(croppedImage);
			txtCode = JsUtils.eval(result, result);
			hfTableNum = txtCode;
		} catch (Exception e) {
			throw new RuntimeException("image code distinguish err", e);
		}
		Map<String, Object> postMap = new HashMap<>();
		postMap.put("__VIEWSTATE", __VIEWSTATE);
		postMap.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
		postMap.put("__EVENTVALIDATION", __EVENTVALIDATION);
		postMap.put("txtCode", txtCode);
		postMap.put("Button1", Button1);
		postMap.put("hfTableNum", hfTableNum);
		Page formPage = new Page(doingPage.getSiteCode(), 1, formAction, formAction);
		formPage.setMethod(HttpMethod.POST);
		formPage.setParameters(postMap);
		formPage.setPostContentType(PostContentType.FORM);
		formPage.setReferer(doingPage.getFinalUrl());
		getDowner().down(formPage);

		String formHtml = formPage.getPageSrc();
		Document formDoc = Jsoup.parse(formHtml);
		Element scriptElement = formDoc.select("form>script").first();
		String houseInfoUrl = null;
		String tempHtml = scriptElement.html();
		if (StringUtils.contains(tempHtml, "jieguo(")) {
			tempHtml = StringUtils.substringBetween(tempHtml, "jieguo(", ");");
			String[] tempText = StringUtils.split(tempHtml, ",");
			if (StringUtils.isBlank(tempHtml) || tempText.length != 2) {
				throw new RuntimeException("valiCode is not correct");
			}
			String fid = tempText[0];
			fid = StringUtils.remove(fid, "'");
			String bid = tempText[1];
			bid = StringUtils.remove(bid, "'");
			houseInfoUrl = "../HtmlPage/RoomInfo.aspx?fid=" + fid + "&bid=" + bid;
			houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseInfoUrl);
		}
		if (StringUtils.isBlank(houseInfoUrl)) {
			throw new RuntimeException("don't found houseInfoUrl");
		}

		Page dataPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
		dataPage.setReferer(doingPage.getFinalUrl());
		getDowner().down(dataPage);
		String houseInHtml = dataPage.getPageSrc();
		Document dataDoc = Jsoup.parse(houseInHtml);
		String houseInfoInputCss = "input[id=DataHF]";
		Element houseInfoInput = dataDoc.select(houseInfoInputCss).first();
		if (null == houseInfoInput) {
			throw new RuntimeException("input of houseInfo is null:" + houseInfoInputCss);
		}
		String json = dataDoc.select("input[id=DataHF]").first().attr("value");
		if (StringUtils.isBlank(json)) {
			throw new RuntimeException("houseInfo json is blank");
		}
		json = StringUtils.replace(json, "&quot;", "\"");
		Map<String, Object> resultMap = JsonUtils.toObject(json, Map.class);
		String flr = getJsonValue("flr", resultMap);// 楼层
		String x = getJsonValue("x", resultMap);// 编号
		String roomNum = flr + "-" + x;
		resultMap.put("roomNum", roomNum);
		for (String key : fieldMap.keySet()) {
			String field = fieldMap.get(key);
			String value = getJsonValue(key, resultMap);
			doingPage.getMetaMap().put(field, Arrays.asList(value));
		}

	}

	public String getJsonValue(String key, Map<String, Object> resultMap) {
		if (!resultMap.containsKey(key)) {
			throw new RuntimeException("this map do not containsKey[" + key + "]:" + resultMap);
		}
		Object result = resultMap.get(key);
		return String.valueOf(result);

	}

	@Override
	protected void afterExtract(Page page, ResultContext resultContext) {

	}
}
