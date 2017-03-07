package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月17日 下午2:57:10
 */
public class TjfdcHouseStateWorker extends AbstractCrawlWorker {

	RedisWorkQueue tjfdcHouseInfoQueue;
	String houseCss = "table[id=LouDongInfo1_dgData]>tbody>tr>td:gt(0)";
	Map<String, String> stateMap;

	@Override
	protected void insideInit() {
		tjfdcHouseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tjfdc_house_info");
		stateMap = new HashMap<>();
		stateMap.put("background-color:#ff5e59", "已售");
		stateMap.put("background-color:#aade9c", "未售");
		stateMap.put("background-color:#fff28c", "预定");
		stateMap.put("background-color:Gray", "其它不可售房");
		stateMap.put("background-color:#a0a0a0", "其它不可售房");

	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage){
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		Elements houseTdElements = doc.select(houseCss);
		List<String> houseNoList = new ArrayList<>();
		List<String> houseStateList = new ArrayList<String>();
		for (int i = 0; i < houseTdElements.size(); i++) {
			Element houseTdElement = houseTdElements.get(i);
			String houseNo = houseTdElement.text();
			if (StringUtils.isNotBlank(StringUtils.trim(houseNo))) {
				String backGround = houseTdElement.attr("style");
				String houseState = null;
				for (String flagKey : stateMap.keySet()) {
					if (StringUtils.contains(backGround, flagKey)) {
						houseState = stateMap.get(flagKey);
						break;
					}
				}
				if (null == houseState) {
					throw new RuntimeException("unkonw house's state[" + backGround + "]:" + doingPage.getFinalUrl());
				}
				houseNoList.add(houseNo);
				houseStateList.add(houseState);
				Element houseAElement = houseTdElement.select("a").first();
				if (null != houseAElement) {
					String houseInfoUrl = houseAElement.attr("onclick");
					houseInfoUrl = StringUtils.substringBetween(houseInfoUrl, "window.showModalDialog(\"",
							"\",window,\"");
					houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseInfoUrl);
					Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
					houseInfoPage.setMethod(HttpMethod.GET);
					houseInfoPage.setReferer(doingPage.getFinalUrl());
					houseInfoPage.setType(PageType.DATA.value());
					if (!tjfdcHouseInfoQueue.duplicateKey(houseInfoPage.getPageKey())) {
						houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
						tjfdcHouseInfoQueue.push(houseInfoPage);
					}
				}
			}
		}
		doingPage.getMetaMap().put("houseNo", houseNoList);
		doingPage.getMetaMap().put("status", houseStateList);
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
