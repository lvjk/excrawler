package six.com.crawler.work.plugs;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月22日 下午1:10:41
 */
public class TmsfPresellInfoWorker extends AbstractCrawlWorker {

	Map<String, String> jsonKeyMap;
	RedisWorkSpace<Page> houseUrlQueue;
	String sidTemplate = "<<sid>>";
	String propertyidTemplate = "<<propertyid>>";
	String presellIdTemplate = "<<presellId>>";
	String houseUrlTemplate = "http://www.tmsf.com/newhouse/" + "property_" + sidTemplate + "_" + propertyidTemplate
			+ "_control.htm?" + "presellid=" + presellIdTemplate
			+ "&buildingid=&area=&allprice=&housestate=&housetype=&isopen=";

	@Override
	protected void insideInit() {
		houseUrlQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "tmsf_house_url",Page.class);
		jsonKeyMap = new HashMap<>();
		jsonKeyMap.put("presellid", "presell.presellid");
		jsonKeyMap.put("presellName", "presell.presellname");
		jsonKeyMap.put("presellCode", "presell.persellno");
		jsonKeyMap.put("presellIssueDate", "presell.applydate");
		jsonKeyMap.put("presellApplyCompany", "presell.applycorp");
		jsonKeyMap.put("issueCompany", "presell.sendcorp");
		jsonKeyMap.put("superviseBank", "presell.bank");
		jsonKeyMap.put("superviseBankAccount", "presell.bankaccno");
		jsonKeyMap.put("presellApprovalProjectName", "presell.projname");
		jsonKeyMap.put("developer", "property.chname");
		jsonKeyMap.put("buildingAddress", "property.located");
		jsonKeyMap.put("openingDate", "presell.openingdate");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String presaleJson = doingPage.getPageSrc();
		Map<String, Object> map = JsonUtils.toObject(presaleJson, Map.class);
		for (String field : jsonKeyMap.keySet()) {
			String jsonKey = jsonKeyMap.get(field);
			String[] jsonKeys = StringUtils.split(jsonKey, ".");
			Map<String, Object> tempJsonMap = (Map<String, Object>) map.get(jsonKeys[0]);
			Object jsonValue = tempJsonMap.get(jsonKeys[1]);
			doingPage.addMeta(field, null != jsonValue ? jsonValue.toString() : "");
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String sid = resultContext.getExtractResult("sid").get(0);
		String propertyid = resultContext.getExtractResult("propertyId").get(0);
		String presellid = resultContext.getExtractResult("presellid").get(0);
		String houseUrl = StringUtils.replace(houseUrlTemplate, sidTemplate, sid);
		houseUrl = StringUtils.replace(houseUrl, propertyidTemplate, propertyid);
		houseUrl = StringUtils.replace(houseUrl, presellIdTemplate, presellid);
		Page housePage = new Page(doingPage.getSiteCode(), 1, houseUrl, houseUrl);
		housePage.setReferer(doingPage.getFinalUrl());
		housePage.setMethod(HttpMethod.GET);
		housePage.setType(PageType.DATA.value());
		housePage.getMetaMap().putAll(doingPage.getMetaMap());
		housePage.addMeta("propertyId", propertyid);
		housePage.addMeta("presellId_org", presellid);
		houseUrlQueue.push(housePage);
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
