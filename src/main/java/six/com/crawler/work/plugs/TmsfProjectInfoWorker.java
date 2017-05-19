package six.com.crawler.work.plugs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.exception.ProcessWorkerCrawlerException;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月21日 下午5:46:17
 */
public class TmsfProjectInfoWorker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(TmsfProjectInfoWorker.class);
	int longitudeMax = 135;
	int longitudeMin = 73;
	int latitudeMax = 53;
	int latitudeMix = 4;
	WorkSpace<Page> projectInfo1Queue;
	WorkSpace<Page> presellUrlQueue;
	String longitude_latitude_div_css = "div[id=boxid1]>div[class=border3 positionr]";
	String mapDivCss = "div[id=boxid1]>div>div";
	String projectNameCss = "div[class=lpxqtop]>div>div>span[class=buidname colordg]";
	String brandNameCss = "div[class=lpxqtop]>div>div>span[class=extension famwei ft14 mgr10]>ul>li:eq(1)";

	@Override
	protected void insideInit() {
		presellUrlQueue = getManager().getWorkSpaceManager().newWorkSpace("tmsf_presell_url", Page.class);
		projectInfo1Queue = getManager().getWorkSpaceManager().newWorkSpace("tmsf_project_info_1", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String projectNameCss = "div[id=head]>ul>li";
		Elements projectNameElements = doingPage.getDoc().select(projectNameCss);
		if (null != projectNameElements && !projectNameElements.isEmpty()) {
			projectInfo1Queue.push(doingPage);
			throw new ProcessWorkerCrawlerException("different pages:" + doingPage.getFinalUrl());
		} else {
			Elements mapDivs = doingPage.getDoc().select(mapDivCss);
			if (null != mapDivs && !mapDivs.isEmpty()) {
				Element longitude_latitude_div = doingPage.getDoc().select(longitude_latitude_div_css).first();
				String text = longitude_latitude_div.html();
				String start = "var point = new BMap.Point(";
				String end = ")";
				String[] result = StringUtils.substringsBetween(text, start, end);
				if (null == result || result.length != 1) {
					throw new ProcessWorkerCrawlerException("find longitude and latitude err");
				}
				String longitudeLatitudeStr = result[0];
				longitudeLatitudeStr = StringUtils.replace(longitudeLatitudeStr, "'", "");
				String[] longitudeAndLatitude = StringUtils.split(longitudeLatitudeStr, ",");
				if (longitudeAndLatitude.length != 2) {
					throw new ProcessWorkerCrawlerException("find longitude and latitude err");
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
				doingPage.getMetaMap().put("latitude", ArrayListUtils.asList(String.valueOf(latitude)));
				doingPage.getMetaMap().put("longitude", ArrayListUtils.asList(String.valueOf(longitude)));
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> presellUrls = resultContext.getExtractResult("presellUrl");
		if (null != presellUrls && presellUrls.size() > 0) {
			String presaleUrl = presellUrls.get(0);
			if (StringUtils.isNotBlank(presaleUrl)) {
				String sid = resultContext.getExtractResult("sid").get(0);
				String projectId = resultContext.getOutResults().get(0).get(Extracter.DEFAULT_RESULT_ID);
				if (StringUtils.isNotBlank(projectId)) {
					Page presellPage = new Page(doingPage.getSiteCode(), 1, presaleUrl, presaleUrl);
					presellPage.setReferer(doingPage.getFinalUrl());
					presellPage.setMethod(HttpMethod.GET);
					presellPage.setType(PageType.DATA.value());
					presellPage.getMetaMap().putAll(doingPage.getMetaMap());
					presellPage.getMetaMap().put("sid", ArrayListUtils.asList(sid));
					presellPage.getMetaMap().put("projectId", ArrayListUtils.asList(projectId));
					presellUrlQueue.push(presellPage);
				} else {
					throw new RuntimeException("did not get projectId");
				}
			}
		} else {
			log.error("did not find presellUrl:" + doingPage.getFinalUrl());
		}
	}

}
