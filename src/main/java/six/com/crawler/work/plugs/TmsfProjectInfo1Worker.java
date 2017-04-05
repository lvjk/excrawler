package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月7日 下午12:02:34
 */
public class TmsfProjectInfo1Worker extends AbstractCrawlWorker {

	final static Logger log = LoggerFactory.getLogger(TmsfProjectInfo1Worker.class);

	int longitudeMax = 135;
	int longitudeMin = 73;
	int latitudeMax = 53;
	int latitudeMix = 4;
	RedisWorkSpace<Page> sellControlUrlQueue;
	String mapDivCss = "div[id=container]>script";

	@Override
	protected void insideInit() {
		sellControlUrlQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "tmsf_presell_url_1",
				Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		mapDivCss = "div[id=container]";
		Element divElement = doingPage.getDoc().select(mapDivCss).first();
		Element script = divElement.parent().select("script").first();
		// pageLoad('万星·悦城可售883套','119.784346','29.863116','21077908');
		if (null != script) {
			String scriptHtml = script.html();
			if (StringUtils.contains(scriptHtml, "pageLoad")) {
				String[] tempLongitudeAndLatitude = StringUtils.split(scriptHtml, "','");
				if (tempLongitudeAndLatitude.length != 6) {
					throw new RuntimeException("find longitude and latitude err");
				}
				double longitude = 0;
				double latitude = 0;
				String[] longitudeAndLatitude = new String[2];
				longitudeAndLatitude[0] = tempLongitudeAndLatitude[2];
				longitudeAndLatitude[1] = tempLongitudeAndLatitude[3];
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
		List<String> fields = new ArrayList<>();
		fields.add("capacityRate");
		fields.add("greeningRate");
		fields.add("coversArea");
		fields.add("buildArea");
		fields.add("totalHouses");
		fields.add("parkingInfo");
		fields.add("propertyLife");
		List<Map<String, String>> results=resultContext.getOutResults();
		if(null!=results){
			for (Map<String, String> result : results) {
				for (String field : fields) {
					String value = result.get(field);
					if (null != value && value.length() > 45) {
						result.put(field,"");
					}
				}
			}
		}

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> sellControlUrls = resultContext.getExtractResult("sellControlUrl_1");
		String projectId = resultContext.getOutResults().get(0).get(Extracter.DEFAULT_RESULT_ID);
		if (StringUtils.isBlank(projectId)) {
			throw new RuntimeException("system id is blank");
		} else {
			if (null != sellControlUrls && sellControlUrls.size() > 0) {
				String sellControlUrl = sellControlUrls.get(0);
				Page sellControlPage = new Page(doingPage.getSiteCode(), 1, sellControlUrl, sellControlUrl);
				sellControlPage.setReferer(doingPage.getFinalUrl());
				sellControlPage.setType(PageType.DATA.value());
				sellControlPage.getMetaMap().putAll(doingPage.getMetaMap());
				sellControlPage.getMetaMap().put("projectId", ArrayListUtils.asList(projectId));
				sellControlUrlQueue.push(sellControlPage);
			} else {
				log.warn("did not find presellUrl:" + doingPage.getFinalUrl());
				log.warn(doingPage.getPageSrc());
			}
		}

	}

}
