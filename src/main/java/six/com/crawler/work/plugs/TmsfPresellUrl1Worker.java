package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月7日 下午12:55:42
 */
public class TmsfPresellUrl1Worker extends AbstractCrawlWorker {

	RedisWorkQueue presellInfoQueue;

	@Override
	protected void insideInit() {
		presellInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_presell_info_1");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String formCss = "form[id=search]";
		Element formElement = doingPage.getDoc().select(formCss).first();
		String formAction = formElement.attr("action");

		String sidCss = "input[id=sid]";
		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = sidElement.attr("value");

		String propertyidCss = "input[id=propertyid]";
		Element propertyidElement = doingPage.getDoc().select(propertyidCss).first();
		String propertyid = propertyidElement.attr("value");

		String tidCss = "input[id=tid]";
		Element tidElement = doingPage.getDoc().select(tidCss).first();
		String tid = tidElement.attr("value");
		String buildingid = "";

		String presellCss = "div[id=yf_one]>dl:eq(0)>dd>a";
		Elements presellElements = doingPage.getDoc().select(presellCss);
		for (Element presellElement : presellElements) {
			String presellid = presellElement.attr("href");
			presellid = StringUtils.substringBetween(presellid, "javascript:doPresell('", "')");
			String presellUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), formAction);
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("sid", sid);
			paramMap.put("propertyid", propertyid);
			paramMap.put("tid", tid);
			paramMap.put("presellid", presellid);
			paramMap.put("buildingid", buildingid);
			Page presellPage = new Page(getSite().getCode(), 1, presellUrl, presellUrl);
			presellPage.setReferer(doingPage.getFinalUrl());
			presellPage.setMethod(HttpMethod.POST);
			presellPage.setParameters(paramMap);
			presellPage.getMetaMap().putAll(doingPage.getMetaMap());
			presellInfoQueue.push(presellPage);
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
