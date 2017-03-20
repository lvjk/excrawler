package six.com.crawler.work.plugs;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月22日 下午4:59:10
 */
public class TmsfPresellUrlWorker extends AbstractCrawlWorker {

	RedisWorkQueue presellInfoQueue;
	private String sidTemplate = "<<sid>>";
	private String presellIdTemplate = "<<presellId>>";
	private String propertyidTemplate = "<<propertyid>>";

	private String presaleJsonUrlTemplate = "http://www.tmsf.com/newhouse/NewPropertyHz_createPresellInfo.jspx?"
			+ "sid=" + sidTemplate + "&presellid=" + presellIdTemplate + "&propertyid=" + propertyidTemplate;
	private String propertyidCss = "input[id=propertyid]";
	private String presellIdCss = "div[id=presell_dd]>div>a";
	private String sidCss = "input[id=sid]";


	@Override
	protected void insideInit() {
		presellInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "tmsf_presell_info");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Element sidElement = doingPage.getDoc().select(sidCss).first();
		String sid = "";
		if (null != sidElement) {
			sid = sidElement.attr("value");
		}
		String propertyid = null;
		Elements propertyidElements = doingPage.getDoc().select(propertyidCss);
		for (Element propertyidElement : propertyidElements) {
			String tempPropertyid = propertyidElement.attr("value");
			if (StringUtils.isNotBlank(tempPropertyid)) {
				propertyid = tempPropertyid;
				break;
			}
		}
		Elements presaleElements = doingPage.getDoc().select(presellIdCss);
		for (Element prasaleElement : presaleElements) {
			String presaleId = prasaleElement.attr("id");
			presaleId = StringUtils.remove(presaleId, "presell_");
			if (!"all".equals(presaleId)) {
				String presellJsonUrl = StringUtils.replace(presaleJsonUrlTemplate, sidTemplate, sid);
				presellJsonUrl = StringUtils.replace(presellJsonUrl, presellIdTemplate, presaleId);
				presellJsonUrl = StringUtils.replace(presellJsonUrl, propertyidTemplate, propertyid);
				presellJsonUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), presellJsonUrl);
				Page presellPage = new Page(doingPage.getSiteCode(), 1, presellJsonUrl, presellJsonUrl);
				presellPage.setReferer(doingPage.getFinalUrl());
				presellPage.setType(PageType.JSON.value());
				presellPage.getMetaMap().putAll(doingPage.getMetaMap());
				presellInfoQueue.push(presellPage);
			}
		}
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
