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

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.common.utils.JsoupUtils.TableResult;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月13日 上午10:19:49
 */
public class TjfdcBuildingInfoWorker extends HtmlCommonWorker {


	private RedisWorkQueue tjfdcHouseStateQueue;
	private Map<String, String> fieldMap;
	private String tableCss="table[id=LouDongInfo]";
	private String doorNoUrlCss="div[id=divLouDongInfo]>div>div:eq(1)>div>table>tbody>tr:eq(1)>td>a";
	
	String formCss = "form[name=form1]";
	String __EVENTARGUMENT_Css = "input[id=__EVENTARGUMENT]";
	String __VIEWSTATE_Css = "input[id=__VIEWSTATE]";
	String __VIEWSTATEGENERATOR_Css = "input[id=__VIEWSTATEGENERATOR]";
	String __EVENTVALIDATION_Css = "input[id=__EVENTVALIDATION]";
	String hidDoing_Css = "input[id=hidDoing]";
	String txtJD_Css = "input[id=txtJD]";
	String txtWD_Css = "input[id=txtWD]";
	String txtProName_Css = "input[id=txtProName]";
	
	public TjfdcBuildingInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	@Override
	protected void insideInit() {
		tjfdcHouseStateQueue = new RedisWorkQueue(getManager().getRedisManager(), "tjfdc_house_state");
		fieldMap = new HashMap<String, String>();
		fieldMap.put("楼盘名称", "projectName");
		fieldMap.put("楼栋名称", "loudongName");
		fieldMap.put("楼号", "loudongNo");
		fieldMap.put("许可证号", "presalePermit");
		fieldMap.put("开盘日期", "openTime");
		fieldMap.put("公司名称", "company");
		fieldMap.put("所在区", "district");
		fieldMap.put("房屋坐落", "address");
	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		Element tableElement = doc.select(tableCss).first();
		List<TableResult> results = JsoupUtils.paserTable(tableElement);
		for (TableResult result : results) {
			for (String field : fieldMap.keySet()) {
				if (result.getKey().contains(field)) {
					String realField = fieldMap.get(field);
					doingPage.getMetaMap().computeIfAbsent(realField, mapKey -> new ArrayList<>())
							.add(result.getValue());
					break;
				}
			}
		}
		Element formElement =doc.select(formCss).first();
		String action = formElement.attr("action");
		action = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), action);
		String __EVENTARGUMENT = doc.select(__EVENTARGUMENT_Css).first().attr("value");
		String __VIEWSTATE =doc.select(__VIEWSTATE_Css).first().attr("value");
		String __VIEWSTATEGENERATOR = doc.select(__VIEWSTATEGENERATOR_Css).first().attr("value");
		String __EVENTVALIDATION =doc.select(__EVENTVALIDATION_Css).first().attr("value");
		String hidDoing = doc.select(hidDoing_Css).first().attr("value");
		String txtJD = doc.select(txtJD_Css).first().attr("value");
		String txtWD =doc.select(txtWD_Css).first().attr("value");
		String txtProName = doc.select(txtProName_Css).first().attr("value");
		
		

		Elements doorNoUrlElements = doc.select(doorNoUrlCss);
		for (Element doorNoUrlElement : doorNoUrlElements) {
			String doorNo=doorNoUrlElement.text();
			String doorNo__EVENTTARGET = doorNoUrlElement.attr("href");
			doorNo__EVENTTARGET = StringUtils.substringBetween(doorNo__EVENTTARGET, "javascript:__doPostBack('",
					"','')");
			
			Map<String, Object> parameters=new HashMap<String, Object>();
			parameters.put("__EVENTTARGET", doorNo__EVENTTARGET);
			parameters.put("__EVENTARGUMENT", __EVENTARGUMENT);
			parameters.put("__VIEWSTATE", __VIEWSTATE);
			parameters.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
			parameters.put("__EVENTVALIDATION", __EVENTVALIDATION);
			parameters.put("hidDoing", hidDoing);
			parameters.put("txtJD", txtJD);
			parameters.put("txtWD", txtWD);
			parameters.put("txtProName", txtProName);
			
			Page houseStatePage = new Page(doingPage.getSiteCode(), 1, action,action);
			houseStatePage.setReferer(doingPage.getFinalUrl());
			houseStatePage.setType(PageType.DATA.value());
			houseStatePage.setMethod(HttpMethod.POST);
			houseStatePage.setParameters(parameters);
			houseStatePage.getMetaMap().put("projectName", Arrays.asList(doingPage.getMeta("projectName").get(0)));
			houseStatePage.getMetaMap().put("loudongName", Arrays.asList(doingPage.getMeta("loudongName").get(0)));
			houseStatePage.getMetaMap().put("loudongNo", Arrays.asList(doingPage.getMeta("loudongNo").get(0)));
			houseStatePage.getMetaMap().put("presalePermit",Arrays.asList(doingPage.getMeta("presalePermit").get(0)));
			houseStatePage.getMetaMap().put("doorNo", Arrays.asList(doorNo));
			tjfdcHouseStateQueue.push(houseStatePage);
		}
	
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {

	}

	@Override
	protected void onComplete(Page doingPage) {

	}


}
