package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.extract.Extracter;

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
		mapDivCss="div[id=container]";
		Element divElement = doingPage.getDoc().select(mapDivCss).first();
		Element script=divElement.parent().select("script").first();
		//pageLoad('万星·悦城可售883套','119.784346','29.863116','21077908');
		if (null != script) {
			String scriptHtml=script.html();
			if(StringUtils.contains(scriptHtml, "pageLoad")){
				String[] tempLongitudeAndLatitude=StringUtils.split(scriptHtml,"','");
				if (tempLongitudeAndLatitude.length !=6) {
					throw new RuntimeException("find longitude and latitude err");
				}
				double longitude = 0;
				double latitude = 0;
				String[] longitudeAndLatitude=new String[2];
				longitudeAndLatitude[0]=tempLongitudeAndLatitude[2];
				longitudeAndLatitude[1]=tempLongitudeAndLatitude[3];
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
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		List<String> fields=new ArrayList<>();
		fields.add("capacityRate");
		fields.add("greeningRate");
		fields.add("coversArea");
		fields.add("buildArea");
		fields.add("totalHouses");
		fields.add("parkingInfo");
		fields.add("propertyLife");
		for(String field:fields){
			List<String> list=resultContext.getExtractResult(field);
			if(null!=list&&list.size()>0){
				String value=list.get(0);
				if(null!=value&&value.length()>45){
					list.clear();
					list.add("");
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
		if(StringUtils.isBlank(projectId)){
			throw new RuntimeException("system id is blank");
		}else{
			if (null != sellControlUrls && sellControlUrls.size() > 0) {
				String sellControlUrl = sellControlUrls.get(0);
				Page sellControlPage = new Page(doingPage.getSiteCode(), 1, sellControlUrl, sellControlUrl);
				sellControlPage.setReferer(doingPage.getFinalUrl());
				sellControlPage.setType(PageType.DATA.value());
				sellControlPage.getMetaMap().put("projectId", Arrays.asList(projectId));
				sellControlUrlQueue.push(sellControlPage);
			}
		}
		
	}


}
