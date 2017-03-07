package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;

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
	RedisWorkQueue projectInfo1Queue;
	RedisWorkQueue presellUrlQueue;
	String longitude_latitude_div_css = "div[id=boxid1]>div[class=border3 positionr]";
	String mapDivCss = "div[id=boxid1]>div>div";

	@Override
	protected void insideInit() {
		presellUrlQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_presell_url");
		projectInfo1Queue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_project_info_1");
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
	protected boolean insideOnError(Exception t, Page doingPage) {
		String projectNameCss = "div[id=head]>ul>li";
		Elements projectNameElements = doingPage.getDoc().select(projectNameCss);
		if (null != projectNameElements && !projectNameElements.isEmpty()) {
			projectInfo1Queue.push(doingPage);
			getWorkQueue().finish(doingPage);
			return true;
		}
		return false;
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> presellUrls = resultContext.getExtractResult("presellUrl");
		if (null != presellUrls && presellUrls.size() > 0) {
			String presaleUrl = presellUrls.get(0);
			String sid = resultContext.getExtractResult("sid").get(0);
			String projectId = resultContext.getOutResults().get(0).get(Constants.DEFAULT_RESULT_ID);
			Page presellPage = new Page(doingPage.getSiteCode(), 1, presaleUrl, presaleUrl);
			presellPage.setReferer(doingPage.getFinalUrl());
			presellPage.setMethod(HttpMethod.GET);
			presellPage.setType(PageType.DATA.value());
			presellPage.getMetaMap().put("sid", Arrays.asList(sid));
			presellPage.getMetaMap().put("projectId", Arrays.asList(projectId));
			presellUrlQueue.push(presellPage);
		}
	}

}
