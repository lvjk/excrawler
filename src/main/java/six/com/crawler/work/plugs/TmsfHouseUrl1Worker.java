package six.com.crawler.work.plugs;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月7日 下午1:29:51 
*/
public class TmsfHouseUrl1Worker extends AbstractCrawlWorker {


	RedisWorkQueue houseInfoQueue;
	private String sidTemplate = "<<sid>>";
	private String presellIdTemplate = "<<presellid>>";
	private String buildingidTemplate = "<<buildingid>>";

	String houseInfoUrlTemplate = "http://www.tmsf.com/newhouse/NewProperty_showbox.jspx?"
			+ "buildingid="+buildingidTemplate
			+ "&presellid="+presellIdTemplate
			+ "&sid="+sidTemplate;
	

	@Override
	protected void insideInit() {
		houseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_house_info");
	}

	protected void beforeDown(Page doingPage) {
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String sidCss = "input[id=sid]";
		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = "";
		if (null != sidElement) {
			sid = sidElement.attr("value");
		}
		
		String presellidCss = "input[id=presellid]";
		Element presellidElement = doingPage.getDoc().select(presellidCss).first();
		String presellId = "";
		if (null != presellidElement) {
			presellId = presellidElement.attr("value");
		}
		String buildingidCss = "input[id=buildingid]";
		Element buildingidElement = doingPage.getDoc().select(buildingidCss).first();
		String buildingId = "";
		if (null != buildingidElement) {
			buildingId = buildingidElement.attr("value");
		}
		
		String houseInfoUrl = StringUtils.replace(houseInfoUrlTemplate, sidTemplate, sid);
		houseInfoUrl = StringUtils.replace(houseInfoUrl, presellIdTemplate, presellId);
		houseInfoUrl = StringUtils.replace(houseInfoUrl, buildingidTemplate, buildingId);

		Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
		houseInfoPage.setReferer(doingPage.getFinalUrl());
		houseInfoPage.setMethod(HttpMethod.GET);
		houseInfoPage.setType(PageType.DATA.value());
		houseInfoPage.getMetaMap().put("buildingId", Arrays.asList(buildingId));
		houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
		houseInfoQueue.push(houseInfoPage);
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
