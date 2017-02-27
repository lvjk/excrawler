package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月13日 下午2:59:02
 */
public class ShFangDiBuildingInfoWorker extends AbstractCrawlWorker {

	RedisWorkQueue houseInfoQueue;


	@Override
	protected void insideInit() {
		houseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "sh_fangdi_house_info");

	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String layerXpath = "table[id=Table1]>tbody>tr>td>table:eq(1)>tbody>tr";
		Document doc = doingPage.getDoc();
		if (null == doc) {
			String html = doingPage.getPageSrc();
			doc = Jsoup.parse(html);
		}
		Elements floorElements = doc.select(layerXpath);

		List<String> floorList = new ArrayList<>();
		List<String> houseList = new ArrayList<>();
		List<String> houseTypeList = new ArrayList<>();
		List<String> statusList = new ArrayList<>();
		List<String> houseInfoUrlList = new ArrayList<>();

		for (int i = 0; i < floorElements.size(); i++) {
			Element trElement = floorElements.get(i);
			Element floorElement = trElement.select("td[width=80]").first();
			if (null == floorElement) {
				continue;
			}
			Elements houseElements = trElement.select("td[width!=80]");
			String floor = "#";
			if (null != floorElement) {
				floor = floorElement.text();
			}
			for (int j = 0; j < houseElements.size(); j++) {
				String houserElementStyle = null;
				String houseStatus = null;
				String houseType = "";
				String bgcolor = null;
				String houseInfoUrl = null;

				Element houseElement = houseElements.get(j);
				String houser = houseElement.text();

				Element houseInfoElement = houseElement.select("a").first();
				if (null != houseInfoElement) {
					houseInfoUrl = houseInfoElement.attr("href");
					houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseInfoUrl);
					houseInfoUrlList.add(houseInfoUrl);
				}

				houserElementStyle = houseElement.attr("style");
				if (StringUtils.contains(houserElementStyle, "color:808080")) {
					houseStatus = "unknow";
				} else {
					bgcolor = houseElement.attr("bgcolor");
					if (StringUtils.contains(bgcolor, "#FF0000")) {
						houseStatus = "已登记";
					} else if (StringUtils.contains(bgcolor, "#FFFF00")) {
						houseStatus = "已签";
					} else if (StringUtils.contains(bgcolor, "#00C200")) {
						houseStatus = "可售";
					} else if (StringUtils.contains(bgcolor, "#FF00FF")) {
						houseStatus = "已付定金";
					} else if (StringUtils.contains(bgcolor, "FFFFFF")) {
						houseStatus = "未纳入网上销售";
					} else {
						throw new RuntimeException("unknow houseType:" + bgcolor);
					}
				}
				if (StringUtils.contains(houser, "△")) {
					houseType = "动迁安置房";
				} else if (StringUtils.contains(bgcolor, "☆")) {
					houseType = "配套商品房";
				} else if (StringUtils.contains(bgcolor, "●")) {
					houseType = "动迁房";
				}
				floorList.add(floor);
				houseList.add(houser);
				houseTypeList.add(houseType);
				statusList.add(houseStatus);
			}

		}

		List<String> projectNamelist = doingPage.getMeta("projectName");
		List<String> presalePremitlist = doingPage.getMeta("presalePermit");
		List<String> buildinglist = doingPage.getMeta("louDongName");
		String projectName = null;
		String presalePremit = null;
		String building = null;
		if (null != projectNamelist && !projectNamelist.isEmpty()) {
			projectName = projectNamelist.get(0);
		}
		if (null != presalePremitlist && !presalePremitlist.isEmpty()) {
			presalePremit = presalePremitlist.get(0);
		}
		if (null != buildinglist && !buildinglist.isEmpty()) {
			building = buildinglist.get(0);
		}

		projectNamelist = new ArrayList<>(houseList.size());
		presalePremitlist = new ArrayList<>(houseList.size());
		buildinglist = new ArrayList<>(houseList.size());
		for (int i = 0; i < houseList.size(); i++) {
			projectNamelist.add(projectName);
			presalePremitlist.add(presalePremit);
			buildinglist.add(building);
		}
		doingPage.getMetaMap().put("projectName", projectNamelist);
		doingPage.getMetaMap().put("presalePermit", presalePremitlist);
		doingPage.getMetaMap().put("louDongName", buildinglist);
		doingPage.getMetaMap().put("floor", floorList);
		doingPage.getMetaMap().put("houseID", houseList);
		doingPage.getMetaMap().put("houseType", houseTypeList);
		doingPage.getMetaMap().put("status", statusList);
		doingPage.getMetaMap().put("房间信息url_5", houseInfoUrlList);

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		List<String> houseInfoUrlList = resultContext.getExtractResult("房间信息url_5");
		if (null != houseInfoUrlList && !houseInfoUrlList.isEmpty()) {
			List<String> projectNamelist = resultContext.getExtractResult("projectName");
			List<String> presalePremitList = resultContext.getExtractResult("presalePermit");
			List<String> louDongNameList = resultContext.getExtractResult("louDongName");
			String projectName = null;
			String presalePremit = null;
			String louDongName = null;
			if (null != projectNamelist && !projectNamelist.isEmpty()) {
				projectName = projectNamelist.get(0);
			}
			if (null != presalePremitList && !presalePremitList.isEmpty()) {
				presalePremit = presalePremitList.get(0);
			}
			if (null != louDongNameList && !louDongNameList.isEmpty()) {
				louDongName = louDongNameList.get(0);
			}
			String houseInfoUrl = null;
			List<String> tempProjectNamelist = null;
			List<String> tempPresalePremitList = null;
			List<String> tempLouDongNameList = null;
			for (int i = 0; i < houseInfoUrlList.size(); i++) {
				houseInfoUrl = houseInfoUrlList.get(i);
				tempProjectNamelist = new ArrayList<>();
				tempPresalePremitList = new ArrayList<>();
				tempLouDongNameList = new ArrayList<>();
				tempProjectNamelist.add(projectName);
				tempPresalePremitList.add(presalePremit);
				tempLouDongNameList.add(louDongName);
				Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
				houseInfoPage.setType(PageType.DATA.value());
				houseInfoPage.getMetaMap().put("projectName", tempProjectNamelist);
				houseInfoPage.getMetaMap().put("presalePermit", tempPresalePremitList);
				houseInfoPage.getMetaMap().put("louDongName", tempLouDongNameList);
				houseInfoPage.setReferer(doingPage.getFinalUrl());
				if (!houseInfoQueue.duplicateKey(houseInfoPage.getPageKey())) {
					houseInfoQueue.push(houseInfoPage);
				}
			}
		}

	}

	@Override
	public void onComplete(Page p,ResultContext resultContext) {

	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

}
