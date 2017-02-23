package six.com.crawler.work.plugs;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月21日 下午5:46:17
 */
public class TmsfProjectInfoWorker extends AbstractCrawlWorker {

	int longitudeMax = 135;
	int longitudeMin = 73;
	int latitudeMax = 53;
	int latitudeMix = 4;
	RedisWorkQueue projectHouseUrlQueue;
	RedisWorkQueue projectPresaleUrlQueue;
	String presaleUrlCss = "div[id=buildnavbar]>a:contains(一房一价)";
	String longitude_latitude_div_css = "div[id=boxid1]>div[class=border3 positionr]";
	String mapDivCss = "div[id=boxid1]>div>div";

	public TmsfProjectInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		projectPresaleUrlQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_presale_url");
		projectHouseUrlQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_house_url");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		Elements mapDivs = doc.select(mapDivCss);
		if (null != mapDivs && !mapDivs.isEmpty()) {
			Element longitude_latitude_div = doc.select(longitude_latitude_div_css).first();
			String text = longitude_latitude_div.html();
			String start = "var point = new BMap.Point(";
			String end = ")";
			String[] result = StringUtils.substringsBetween(text, start, end);
			if (null == result || result.length != 1) {
				throw new RuntimeException("find longitude and latitude err");
			}
			String longitudeLatitudeStr = result[0];
			longitudeLatitudeStr = StringUtils.replace(longitudeLatitudeStr, "'", "");
			String[] longitudeAndLatitude = StringUtils.split(longitudeLatitudeStr, ",");
			if (longitudeAndLatitude.length != 2) {
				throw new RuntimeException("find longitude and latitude err");
			}
			double longitude = 0;
			double latitude = 0;
			for (String numStr : longitudeAndLatitude) {
				double num = Double.valueOf(numStr);
				if (num > longitudeMin && num < longitudeMax) {
					longitude = num;
				} else {
					latitude = num;
				}
			}
			doingPage.getMetaMap().put("latitude", Arrays.asList(String.valueOf(latitude)));
			doingPage.getMetaMap().put("longitude", Arrays.asList(String.valueOf(longitude)));
		}

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String presaleUrl = resultContext.getExtractResult("presaleUrl").get(0);
		String projectId = resultContext.getOutResults().get(0).get(Constants.DEFAULT_RESULT_ID);
		Page presalePage = new Page(doingPage.getSiteCode(), 1, presaleUrl, presaleUrl);
		presalePage.setReferer(doingPage.getFinalUrl());
		presalePage.setMethod(HttpMethod.GET);
		presalePage.setType(PageType.DATA.value());
		presalePage.getMetaMap().put("projectid", Arrays.asList(projectId));
		projectPresaleUrlQueue.push(presalePage);
	}

}
