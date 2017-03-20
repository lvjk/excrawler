package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.utils.JsoupUtils.TableResult;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月7日 下午1:12:09
 */
public class TmsfPresellInfo1Worker extends AbstractCrawlWorker {

	RedisWorkQueue houseInfoQueue;
	String tableCss = "dd[id=myCont2]>div>table";
	Map<String, String> tableKeyMap;

	@Override
	protected void insideInit() {
		houseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_house_info_1");
		tableKeyMap = new HashMap<>();
		tableKeyMap.put("预售证名称", "presellName");
		tableKeyMap.put("预售证号", "presellCode");
		tableKeyMap.put("预售证核发时间", "presellIssueDate");
		tableKeyMap.put("预售证申领单位", "presellApplyCompany");
		tableKeyMap.put("发证机关", "issueCompany");
		tableKeyMap.put("资金监管银行", "superviseBank");
		tableKeyMap.put("资金监管银行账号", "superviseBankAccount");
		tableKeyMap.put("预售审批项目名称", "presellApprovalProjectName");
		tableKeyMap.put("开发商名称", "developer");
		tableKeyMap.put("楼盘坐落", "buildingAddress");
		tableKeyMap.put("开盘时间", "openingDate");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String tableCss = "//dd[@id='myCont2']/div/table";
		Elements tableElements = doingPage.getDoc().select(tableCss);
		List<TableResult> tableResults = new ArrayList<>();
		for (Element tableElement : tableElements) {
			List<TableResult> tempTableResults = JsoupUtils.paserTable(tableElement);
			tableResults.addAll(tempTableResults);
		}
		for (TableResult tableResult : tableResults) {
			for (String mapKey : tableKeyMap.keySet()) {
				if (StringUtils.contains(tableResult.getKey(), mapKey)) {
					String key = tableKeyMap.get(mapKey);
					String value = tableResult.getValue();
					if (StringUtils.contains(tableResult.getKey(), "资金监管银行")) {
						String[] texts = StringUtils.split(tableResult.getValue(), "\n");
						for (String text : texts) {
							value = text;
							if (NumberUtils.isNumber(text)) {
								key = "superviseBankAccount";

							} else {
								key = "superviseBank";
							}
							doingPage.getMetaMap().put(key, Arrays.asList(value));
						}
					}else{
						doingPage.getMetaMap().put(key, Arrays.asList(value));
					}
				}
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String formCss = "//form[@id='search']";
		Element formElement = doingPage.getDoc().select(formCss).first();
		String formAction = formElement.attr("action");

		String sidCss = "//input[@id='sid']";
		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = sidElement.attr("value");

		String propertyidCss = "//input[@id='propertyid']";
		Element propertyidElement = doingPage.getDoc().select(propertyidCss).first();
		String propertyid = propertyidElement.attr("value");

		String tidCss = "//input[@id='tid']";
		Element tidElement = doingPage.getDoc().select(tidCss).first();
		String tid = tidElement.attr("value");

		String presellCss = "//input[@id='presellid']";
		Element presellElement = doingPage.getDoc().select(presellCss).first();
		String presellid = presellElement.attr("value");

		String buildingidCss = "//div[@id='yf_one']/dl[2]/dd/a";
		Elements buildingidElements = doingPage.getDoc().select(buildingidCss);
		for (Element buildingidElement : buildingidElements) {
			String buildingid = buildingidElement.attr("href");
			buildingid = StringUtils.substringBetween(buildingid, "javascript:doBuilding('", "')");
			String houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), formAction);
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("sid", sid);
			paramMap.put("propertyid", propertyid);
			paramMap.put("tid", tid);
			paramMap.put("presellid", presellid);
			paramMap.put("buildingid", buildingid);
			Page houseInfoPage = new Page(getSite().getCode(), 1, houseInfoUrl, houseInfoUrl);
			houseInfoPage.setReferer(doingPage.getFinalUrl());
			houseInfoPage.setMethod(HttpMethod.POST);
			houseInfoPage.setParameters(paramMap);
			houseInfoQueue.push(houseInfoPage);
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
}
