package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
 * @date 创建时间：2017年1月12日 下午4:08:24
 */
public class TjfdcPresaleInfoWorker extends AbstractCrawlWorker {

	private RedisWorkSpace<Page> tjfdcBuildingInfoQueue;
	String nextPageXpath = "a[id=LouDongList1_SplitPageIconModule1_lbnNextPage]";
	String loudongNameXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(0)>span";
	String loudongNoXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(1)>a";
	String presalePermitXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(2)>span";
	String openTimeXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(3)>span";
	String nonResidentialRiceXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(4)>span";
	String esidenceRiceXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(5)>span";
	String totalhousesXpath = "div[id=divLouDongList]>div>table>tbody>tr>td:eq(6)>span";
	String formCss = "form[name=form1]";
	String __EVENTARGUMENT_Css = "input[id=__EVENTARGUMENT]";
	String __VIEWSTATE_Css = "input[id=__VIEWSTATE]";
	String __VIEWSTATEGENERATOR_Css = "input[id=__VIEWSTATEGENERATOR]";
	String __EVENTVALIDATION_Css = "input[id=__EVENTVALIDATION]";
	String hidDoing_Css = "input[id=hidDoing]";
	String txtJD_Css = "input[id=txtJD]";
	String txtWD_Css = "input[id=txtWD]";
	String txtProName_Css = "input[id=txtProName]";


	@Override
	protected void insideInit() {
		tjfdcBuildingInfoQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "tjfdc_building_info",Page.class);
	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage){

		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);

		List<String> projectNames = doingPage.getMeta("projectName");
		String projectName = projectNames.get(0);
		projectNames = new ArrayList<>();
		List<String> loudongNames = new ArrayList<>();
		List<String> loudongNos = new ArrayList<>();
		List<String> presalePermits = new ArrayList<>();
		List<String> openTimes = new ArrayList<>();
		List<String> nonResidentialRices = new ArrayList<>();
		List<String> residenceRices = new ArrayList<>();
		List<String> totalhousess = new ArrayList<>();

		Elements loudongNameElements = doc.select(loudongNameXpath);
		if (null != loudongNameElements && loudongNameElements.size() > 0) {
			Elements loudongNoElements = doc.select(loudongNoXpath);
			Elements presalePermitElements = doc.select(presalePermitXpath);
			Elements openTimeElements = doc.select(openTimeXpath);
			Elements nonResidentialRiceElements = doc.select(nonResidentialRiceXpath);
			Elements esidenceRiceElements = doc.select(esidenceRiceXpath);
			Elements totalhousesElements = doc.select(totalhousesXpath);

			Element formElement = doc.select(formCss).first();
			String action = formElement.attr("action");
			action = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), action);

			String __EVENTARGUMENT = doc.select(__EVENTARGUMENT_Css).first().attr("value");
			String __VIEWSTATE = doc.select(__VIEWSTATE_Css).first().attr("value");
			String __VIEWSTATEGENERATOR = doc.select(__VIEWSTATEGENERATOR_Css).first().attr("value");
			String __EVENTVALIDATION = doc.select(__EVENTVALIDATION_Css).first().attr("value");
			String hidDoing = doc.select(hidDoing_Css).first().attr("value");
			String txtJD = doc.select(txtJD_Css).first().attr("value");
			String txtWD = doc.select(txtWD_Css).first().attr("value");
			String txtProName = doc.select(txtProName_Css).first().attr("value");

			for (int i = 0; i < loudongNameElements.size(); i++) {
				Element loudongNameElement = loudongNameElements.get(i);
				Element loudongNoElement = loudongNoElements.get(i);
				Element presalePermitElement = presalePermitElements.get(i);
				Element openTimeElement = openTimeElements.get(i);
				Element nonResidentialRiceElement = nonResidentialRiceElements.get(i);
				Element esidenceRiceElement = esidenceRiceElements.get(i);
				Element totalhousesElement = totalhousesElements.get(i);

				String __EVENTTARGET = loudongNoElement.attr("href");
				__EVENTTARGET = StringUtils.substringBetween(__EVENTTARGET, "javascript:__doPostBack('", "','')");
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("__EVENTTARGET", __EVENTTARGET);
				parameters.put("__EVENTARGUMENT", __EVENTARGUMENT);
				parameters.put("__VIEWSTATE", __VIEWSTATE);
				parameters.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
				parameters.put("__EVENTVALIDATION", __EVENTVALIDATION);
				parameters.put("hidDoing", hidDoing);
				parameters.put("txtJD", txtJD);
				parameters.put("txtWD", txtWD);
				parameters.put("txtProName", txtProName);
				Page newPage = new Page(doingPage.getSiteCode(), 1, action, action);
				newPage.setMethod(HttpMethod.POST);
				newPage.setParameters(parameters);
				newPage.setReferer(doingPage.getFinalUrl());
				newPage.setType(PageType.DATA.value());
				tjfdcBuildingInfoQueue.push(newPage);

				String loudongName = loudongNameElement.text();
				String loudongNo = loudongNoElement.text();
				String presalePermit = presalePermitElement.text();
				String openTime = openTimeElement.text();
				String nonResidentialRice = nonResidentialRiceElement.text();
				String residenceRice = esidenceRiceElement.text();
				String totalhouses = totalhousesElement.text();

				projectNames.add(projectName);
				loudongNames.add(loudongName);
				loudongNos.add(loudongNo);
				presalePermits.add(presalePermit);
				openTimes.add(openTime);
				nonResidentialRices.add(nonResidentialRice);
				residenceRices.add(residenceRice);
				totalhousess.add(totalhouses);
			}
			Element nextPageElement = doc.select(nextPageXpath).first();
			if (null != nextPageElement) {
				String __EVENTTARGET = nextPageElement.attr("href");
				if (StringUtils.isNotBlank(__EVENTTARGET)) {
					__EVENTTARGET = StringUtils.substringBetween(__EVENTTARGET, "javascript:__doPostBack('", "','')");
					Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put("__EVENTTARGET", __EVENTTARGET);
					parameters.put("__EVENTARGUMENT", __EVENTARGUMENT);
					parameters.put("__VIEWSTATE", __VIEWSTATE);
					parameters.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
					parameters.put("__EVENTVALIDATION", __EVENTVALIDATION);
					parameters.put("hidDoing", hidDoing);
					parameters.put("txtJD", txtJD);
					parameters.put("txtWD", txtWD);
					parameters.put("txtProName", txtProName);
					Page nextPage = new Page(doingPage.getSiteCode(), doingPage.getPageNum() + 1, action, action);
					nextPage.setMethod(HttpMethod.POST);
					nextPage.setParameters(parameters);
					nextPage.setReferer(doingPage.getFinalUrl());
					nextPage.getMetaMap().put("projectName", Arrays.asList(projectName));
					nextPage.setType(PageType.DATA.value());
					getWorkSpace().push(nextPage);
				}
			}

			doingPage.getMetaMap().put("projectName", projectNames);
			doingPage.getMetaMap().put("loudongName", loudongNames);
			doingPage.getMetaMap().put("loudongNo", loudongNos);
			doingPage.getMetaMap().put("presalePermit", presalePermits);
			doingPage.getMetaMap().put("openTime", openTimes);
			doingPage.getMetaMap().put("nonResidentialRice", nonResidentialRices);
			doingPage.getMetaMap().put("residenceRice", residenceRices);
			doingPage.getMetaMap().put("totalhouses", totalhousess);

		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage,ResultContext resultContext) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
}
