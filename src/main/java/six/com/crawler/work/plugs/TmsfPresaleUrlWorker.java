package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月22日 下午4:59:10
 */
public class TmsfPresaleUrlWorker extends AbstractCrawlWorker {

	RedisWorkQueue presaleInfoQueue;
	RedisWorkQueue houseInfoQueue;
	private String sidTemplate = "<<sid>>";
	private String presaleidTemplate = "<<presaleid>>";
	private String propertyidTemplate = "<<propertyid>>";

	private String buildingidTemplate = "<<buildingid>>";

	private String areaTemplate = "<<area>>";
	private String allpriceTemplate = "<<allprice>>";
	private String housestateTemplate = "<<housestate>>";
	private String housetypeTemplate = "<<housetype>>";
	private String presaleJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewPropertyHz_createPresellInfo.jspx?"
			+ "sid=" + sidTemplate + "&presellid=" + presaleidTemplate + "&propertyid=" + propertyidTemplate;
	private String houseJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewPropertyHz_showbox.jspx?" + "buildingid="
			+ buildingidTemplate + "&presellid=" + presaleidTemplate + "&sid=" + sidTemplate + "&area=" + areaTemplate
			+ "&allprice=" + allpriceTemplate + "&housestate=" + housestateTemplate + "&housetype=" + housetypeTemplate;
	private String propertyidCss = "input[id=propertyid]";
	private String presaleIdCss = "div[id=presell_dd]>div>a";
	private String buildingIdCss = "div[id=building_dd]>div>a";
	private String sidCss = "input[id=sid]";
	private String areaCss = "input[id=area]";
	private String allpriceCss = "input[id=allprice]";
	private String housestateCss = "input[id=housestate]";
	private String housetypeCss = "input[id=housetype]";

	public TmsfPresaleUrlWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		presaleInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_presale_info");
		houseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_house_info");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
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

		String propertyid = null;
		Elements propertyidElements = doingPage.getDoc().select(propertyidCss);
		for (Element propertyidElement : propertyidElements) {
			String tempPropertyid = propertyidElement.attr("value");
			if (StringUtils.isNotBlank(tempPropertyid)) {
				propertyid = tempPropertyid;
				break;
			}
		}
		Elements presaleElements = doingPage.getDoc().select(presaleIdCss);
		for (Element prasaleElement : presaleElements) {
			String presaleId = prasaleElement.attr("id");
			presaleId = StringUtils.remove(presaleId, "presell_");
			if (!"all".equals(presaleId)) {
				String presaleJsonUrl = StringUtils.replace(presaleJsonUrlTemplate, sidTemplate, sid);
				presaleJsonUrl = StringUtils.replace(presaleJsonUrl, presaleidTemplate, presaleId);
				presaleJsonUrl = StringUtils.replace(presaleJsonUrl, propertyidTemplate, propertyid);
				presaleJsonUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), presaleJsonUrl);
				Page preSalePage = new Page(doingPage.getSiteCode(), 1, presaleJsonUrl, presaleJsonUrl);
				preSalePage.setReferer(doingPage.getFinalUrl());
				preSalePage.setType(PageType.JSON.value());
				presaleInfoQueue.push(preSalePage);
			}
		}

		Elements buildingIdElements = doingPage.getDoc().select(buildingIdCss);
		for (Element buildingIdElement : buildingIdElements) {
			String buildingId = buildingIdElement.attr("id");
			buildingId = StringUtils.remove(buildingId, "building_");
			if (!"all".equals(buildingId)) {
				String houseJsonUrl = StringUtils.replace(houseJsonUrlTemplate, sidTemplate, sid);
				houseJsonUrl = StringUtils.replace(houseJsonUrl, buildingidTemplate, buildingId);
				houseJsonUrl = StringUtils.replace(houseJsonUrl, presaleidTemplate, "");
				houseJsonUrl = StringUtils.replace(houseJsonUrl, areaTemplate, area);
				houseJsonUrl = StringUtils.replace(houseJsonUrl, allpriceTemplate, allprice);
				houseJsonUrl = StringUtils.replace(houseJsonUrl, housestateTemplate, housestate);
				houseJsonUrl = StringUtils.replace(houseJsonUrl, housetypeTemplate, housetype);
				houseJsonUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseJsonUrl);
				Page preSalePage = new Page(doingPage.getSiteCode(), 1, houseJsonUrl, houseJsonUrl);
				preSalePage.setReferer(doingPage.getFinalUrl());
				preSalePage.setType(PageType.JSON.value());
				houseInfoQueue.push(preSalePage);
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

}
