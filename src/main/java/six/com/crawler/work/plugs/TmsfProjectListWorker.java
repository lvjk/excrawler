package six.com.crawler.work.plugs;

import six.com.crawler.utils.ArrayListUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月21日 下午2:20:30
 */
public class TmsfProjectListWorker extends AbstractCrawlWorker {

	final static Logger LOG = LoggerFactory.getLogger(TmsfProjectListWorker.class);

	String sidFlag = "<<sid>>";
	String propertyidFlag = "<<propertyid>>";
	String projectUrilTemplate = "/newhouse/property_" + sidFlag + "_" + propertyidFlag + "_info.htm";
	String projectDivCss = "div[class=build_txt line26]";
	RedisWorkSpace<Page> projectInfoQueue;
	String pageCountCss = "div[class=pagenuber_info]>font:eq(1)";
	String pageIndexTemplate = "<<pageIndex>>";
	String urlTemplate = "http://www.tmsf.com/newhouse/" + "property_searchall.htm?" + "searchkeyword=&" + "keyword=&"
			+ "sid=&" + "districtid=&" + "areaid=&" + "dealprice=&" + "propertystate=&" + "propertytype=&"
			+ "ordertype=&" + "priceorder=&" + "openorder=&" + "view720data=&" + "page=" + pageIndexTemplate + "&"
			+ "bbs=&" + "avanumorder=&" + "comnumorder=";
	// 第一页从1开始
	int pageIndex = 1;
	int pageCount = -1;
	String refererUrl;

	private Page buildPage(int pageIndex, String refererUrl) {
		String pageUrl = StringUtils.replace(urlTemplate, pageIndexTemplate, String.valueOf(pageIndex));
		Page page = new Page(getSite().getCode(), 1, pageUrl, pageUrl);
		page.setReferer(refererUrl);
		page.setMethod(HttpMethod.GET);
		page.setType(PageType.LISTING.value());
		return page;
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "tmsf_project_info",Page.class);
		Page firstPage = buildPage(pageIndex, refererUrl);// 初始化第一页
		getWorkQueue().clearDoing();
		getWorkQueue().push(firstPage);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		if (pageCount == -1) {
			Element pageCountElement = doc.select(pageCountCss).first();
			String pageCountElementText = pageCountElement.text();
			String[] pageCountParams = StringUtils.split(pageCountElementText, "/");
			String pageCountStr = pageCountParams[1];
			pageCount = Integer.valueOf(pageCountStr);
		}
		Elements projectDivElements = doc.select(projectDivCss);
		String projectNameCss = "div[class='build_word01']>a";
		String brandNameCss = "div[class='build_word01']>div:contains(推广名)";
		String districtAndAddressCss = "div:contains(项目位置)>p";
		for (Element projecrDivElement : projectDivElements) {
			Element projectNameElement = projecrDivElement.select(projectNameCss).first();
			Map<String, List<String>> metaMap = new HashMap<>();
			String projectName = null;
			if (null != projectNameElement) {
				projectName = projectNameElement.text();
				if (StringUtils.isNotBlank(projectName)) {
					metaMap.put("projectName",ArrayListUtils.asList(projectName));
				}
			}
			Element brandNameElement = projecrDivElement.select(brandNameCss).first();
			String brandName = null;
			if (null != brandNameElement) {
				brandName = brandNameElement.text();
				if (StringUtils.isNotBlank(brandName)) {
					brandName = StringUtils.remove(brandName, "推广名");
					brandName = StringUtils.remove(brandName, ":");
					brandName = StringUtils.remove(brandName, "：");
					metaMap.put("brandName", ArrayListUtils.asList(brandName));
				}
			}
			Element districtAndAddressElement = projecrDivElement.select(districtAndAddressCss).first();
			String district = null;
			String address = null;
			if (null != districtAndAddressElement) {
				String districtAndAddress = districtAndAddressElement.text();
				district = StringUtils.substringBetween(districtAndAddress, "[", "]");
				address = StringUtils.substringAfter(districtAndAddress, "]");
				if (StringUtils.isNotBlank(district)) {
					metaMap.put("district", ArrayListUtils.asList(district));
				}
				if (StringUtils.isNotBlank(address)) {
					metaMap.put("address", ArrayListUtils.asList(address));
				}
			}

			String onclick = projecrDivElement.attr("onclick");
			onclick = StringUtils.substringBetween(onclick, "toPropertyInfo(", ")");
			String[] params = StringUtils.split(onclick, ",");
			
			metaMap.put("sid", ArrayListUtils.asList(params[0]));
			metaMap.put("propertyId", ArrayListUtils.asList(params[1]));
			
			String projectUrl = StringUtils.replace(projectUrilTemplate, sidFlag, params[0]);
			projectUrl = StringUtils.replace(projectUrl, propertyidFlag, params[1]);
			projectUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), projectUrl);
			Page projectPage = new Page(doingPage.getSiteCode(), 1, projectUrl, projectUrl);
			projectPage.setReferer(doingPage.getFinalUrl());
			projectPage.setType(PageType.DATA.value());
			projectPage.getMetaMap().putAll(metaMap);
			projectInfoQueue.push(projectPage);
		}
		pageIndex++;
		if (pageIndex<=pageCount) {
			Page page = buildPage(pageIndex, doingPage.getFinalUrl());// 初始化第一页
			getWorkQueue().push(page);
		}

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {

	}

}
