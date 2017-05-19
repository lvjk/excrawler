package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月22日 下午5:55:15
 */
public class TmsfHouseUrlWorker extends AbstractCrawlWorker {

	WorkSpace<Page> houseStatusQueue;
	private String propertyidTemplate = "<<propertyid>>";
	private String sidTemplate = "<<sid>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";
	private String areaTemplate = "<<area>>";
	private String allpriceTemplate = "<<allprice>>";
	private String housestateTemplate = "<<housestate>>";
	private String housetypeTemplate = "<<housetype>>";
	String houseInfoUrlTemplate = "http://www.tmsf.com/newhouse/" + "property_" + sidTemplate + "_" + propertyidTemplate
			+ "_control.htm?" + "presellid=" + presellIdTemplate + "&buildingid=" + buildingidTemplate + "&area="
			+ areaTemplate + "&allprice=" + allpriceTemplate + "&housestate=" + housestateTemplate + "&housetype="
			+ housetypeTemplate + "&isopen=";
	private String propertyidCss = "input[id=propertyid]";
	private String sidCss = "input[id=sid]";
	private String areaCss = "input[id=area]";
	private String allpriceCss = "input[id=allprice]";
	private String housestateCss = "input[id=housestate]";
	private String housetypeCss = "input[id=housetype]";

	@Override
	protected void insideInit() {
		houseStatusQueue = getManager().getWorkSpaceManager().newWorkSpace("tmsf_house_status", Page.class);
	}

	protected void beforeDown(Page doingPage) {
	}

	@Override
	protected void beforeExtract(Page doingPage) {

		Element propertyidElement = doingPage.getDoc().select(propertyidCss).first();
		String propertyid = "";
		if (null != propertyidElement) {
			propertyid = propertyidElement.attr("value");
		}

		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = "";
		if (null != sidElement) {
			sid = sidElement.attr("value");
		}
		Element areaElement = doingPage.getDoc().select(areaCss).first();
		String area = "";
		if (null != areaElement) {
			area = areaElement.attr("value");
		}
		Element allpriceElement = doingPage.getDoc().select(allpriceCss).first();
		String allprice = "";
		if (null != allpriceElement) {
			allprice = allpriceElement.attr("value");
		}
		Element housestateElement = doingPage.getDoc().select(housestateCss).first();
		String housestate = "";
		if (null != housestateElement) {
			housestate = housestateElement.attr("value");
		}

		Element housetypeElement = doingPage.getDoc().select(housetypeCss).first();
		String housetype = "";
		if (null != housetypeElement) {
			housetype = housetypeElement.attr("value");
		}

		String presellId = doingPage.getMetaMap().get("presellId_org").get(0);
		String buildingidCss = "div[id=building_dd]>div[class=lptypebarin]>a";
		Elements buildingidElements = doingPage.getDoc().select(buildingidCss);
		for (Element buildingidElement : buildingidElements) {
			String buildingId = buildingidElement.attr("href");
			buildingId = StringUtils.substringBetween(buildingId, "javascript:doBuilding('", "');");
			String houseInfoUrl = StringUtils.replace(houseInfoUrlTemplate, sidTemplate, sid);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, propertyidTemplate, propertyid);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, presellIdTemplate, presellId);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, buildingidTemplate, buildingId);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, areaTemplate, area);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, allpriceTemplate, allprice);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, housestateTemplate, housestate);
			houseInfoUrl = StringUtils.replace(houseInfoUrl, housetypeTemplate, housetype);

			Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
			houseInfoPage.setReferer(doingPage.getFinalUrl());
			houseInfoPage.setMethod(HttpMethod.GET);
			houseInfoPage.setType(PageType.DATA.value());
			houseInfoPage.addMeta("buildingId", buildingId);
			houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
			houseStatusQueue.push(houseInfoPage);
		}
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
