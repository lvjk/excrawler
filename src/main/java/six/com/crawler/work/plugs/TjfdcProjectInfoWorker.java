package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.common.utils.JsoupUtils.TableResult;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月11日 上午11:04:32
 */
public class TjfdcProjectInfoWorker extends AbstractCrawlWorker {

	private RedisWorkQueue tjfdcPresaleInfoQueue;
	private Map<String, String> fieldMap;

	@Override
	protected void insideInit() {
		tjfdcPresaleInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tjfdc_presale_info");
		fieldMap = new HashMap<String, String>();
		fieldMap.put("楼盘名称", "projectName");
		fieldMap.put("区域", "district");
		fieldMap.put("板块", "region");
		fieldMap.put("所属商圈", "section");
		fieldMap.put("计划种类", "projectType");
		fieldMap.put("装修状况", "decorate");
		fieldMap.put("物业地址", "propertyAddress");
		fieldMap.put("容积率", "plotRatio");
		fieldMap.put("绿化率", "greeningRate");
		fieldMap.put("开盘时间", "openTime");
		fieldMap.put("建筑结构", "buildingStructure");
		fieldMap.put("入住时间", "checkInTime");
		fieldMap.put("行政区划", "administrationArea");
		fieldMap.put("物业费", "propertyCost");
		fieldMap.put("环线位置", "loopLineAddress");
		fieldMap.put("物业公司", "propertyCompany");
		fieldMap.put("物业类别", "propertyType");
		fieldMap.put("售楼地址", "saleAddress");
		fieldMap.put("项目特色", "projectFeature");
		fieldMap.put("交通状况", "traffic");
		fieldMap.put("开发商", "developer");
		fieldMap.put("售楼电话", "saleTelphone");
		fieldMap.put("项目介绍", "projectIntroduction");
		fieldMap.put("周边配套", "ambitus");
		fieldMap.put("建材装修", "buildingDecoration");
		fieldMap.put("楼层状况", "floorCondition");
		fieldMap.put("车位信息", "parkingSpace");
		fieldMap.put("相关信息", "relatedInformation");
		fieldMap.put("总建面积", "buildingArea");
		fieldMap.put("用地面积", "landArea");
	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String tableCss = "div[id=divBasicInfo]>table";
		String louPanUlrCss = "div[class=nav_left]>div[class=lnav_box]>ul[class=left_menu]>li:eq(1)>dl[class=tree-menu]>dd:eq(1)>a";
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);

		Element table = doc.select(tableCss).first();
		List<TableResult> results = JsoupUtils.paserTable(table);
		for (TableResult result : results) {
			for (String field : fieldMap.keySet()) {
				if (result.getKey().contains(field)) {
					String realField = fieldMap.get(field);
					doingPage.getMetaMap().put(realField, Arrays.asList(result.getValue()));
					break;
				}
			}
		}

		Element louPanPresaleInfoUrlElement = doc.select(louPanUlrCss).first();
		String louPanPresaleInfoUrl = louPanPresaleInfoUrlElement.attr("href");
		louPanPresaleInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), louPanPresaleInfoUrl);
		Page newPage = new Page(doingPage.getSiteCode(), 1, louPanPresaleInfoUrl, louPanPresaleInfoUrl);
		newPage.setReferer(doingPage.getFinalUrl());
		newPage.setType(PageType.DATA.value());
		newPage.getMetaMap().put("projectName", doingPage.getMeta("projectName"));
		tjfdcPresaleInfoQueue.push(newPage);
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
