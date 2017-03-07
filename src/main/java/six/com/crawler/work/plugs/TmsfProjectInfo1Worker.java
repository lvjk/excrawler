package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月7日 下午12:02:34 
*/
public class TmsfProjectInfo1Worker extends AbstractCrawlWorker{


	int longitudeMax = 135;
	int longitudeMin = 73;
	int latitudeMax = 53;
	int latitudeMix = 4;
	RedisWorkQueue sellControlUrlQueue;
	String mapDivCss ="div[id=container]>script";

	@Override
	protected void insideInit() {
		sellControlUrlQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_presell_url_1");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Element script = doingPage.getDoc().select(mapDivCss).first();
		//pageLoad('万星·悦城可售883套','119.784346','29.863116','21077908');
		if (null != script) {
			String scriptHtml=script.html();
			String[] tempLongitudeAndLatitude=StringUtils.split(scriptHtml,"','");
			if (tempLongitudeAndLatitude.length != 4) {
				throw new RuntimeException("find longitude and latitude err");
			}
			double longitude = 0;
			double latitude = 0;
			String[] longitudeAndLatitude=new String[2];
			longitudeAndLatitude[0]=tempLongitudeAndLatitude[1];
			longitudeAndLatitude[1]=tempLongitudeAndLatitude[2];
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
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> sellControlUrls = resultContext.getExtractResult("sellControlUrl_1");
		if (null != sellControlUrls && sellControlUrls.size() > 0) {
			String sellControlUrl = sellControlUrls.get(0);
			String projectId = resultContext.getOutResults().get(0).get(Constants.DEFAULT_RESULT_ID);
			Page sellControlPage = new Page(doingPage.getSiteCode(), 1, sellControlUrl, sellControlUrl);
			sellControlPage.setReferer(doingPage.getFinalUrl());
			sellControlPage.setType(PageType.DATA.value());
			sellControlPage.getMetaMap().put("projectId", Arrays.asList(projectId));
			sellControlUrlQueue.push(sellControlPage);
		}
	}


}
