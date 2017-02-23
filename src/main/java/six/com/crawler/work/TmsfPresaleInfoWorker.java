package six.com.crawler.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月22日 下午1:10:41
 */
public class TmsfPresaleInfoWorker extends HtmlCommonWorker {

	Map<String, String> jsonKeyMap;

	public TmsfPresaleInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		jsonKeyMap = new HashMap<>();
		jsonKeyMap.put("projectName", "property.propertyname");
		jsonKeyMap.put("presaleName", "presell.presellname");
		jsonKeyMap.put("presaleCode", "presell.persellno");
		jsonKeyMap.put("presaleIssueDate", "presell.applydate");
		jsonKeyMap.put("presaleApplyCompany", "presell.applycorp");
		jsonKeyMap.put("issueCompany", "presell.sendcorp");
		jsonKeyMap.put("superviseBank", "presell.bank");
		jsonKeyMap.put("superviseBankAccount", "presell.bankaccno");
		jsonKeyMap.put("presaleApprovalProjectName", "presell.projname");
		jsonKeyMap.put("developer", "property.chname");
		jsonKeyMap.put("buildingAddress", "property.located");
		jsonKeyMap.put("openingDate", "presell.openingdate");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String presaleJson = doingPage.getPageSrc();
		Map<String, Object> map = JsonUtils.toObject(presaleJson, Map.class);
		for (String field : jsonKeyMap.keySet()) {
			String jsonKey = jsonKeyMap.get(field);
			Map<String, Object> presellMap = (Map<String, Object>) map.get("presell");
			String buildings=presellMap.get("located")!=null?presellMap.get("located").toString():null;
			String[] jsonKeys = StringUtils.split(jsonKey, ".");
			Map<String, Object> tempJsonMap = (Map<String, Object>) map.get(jsonKeys[0]);
			Object jsonValue = tempJsonMap.get(jsonKeys[1]);
			doingPage.getMetaMap().computeIfAbsent(field, mapKey -> new ArrayList<>())
					.add(null != jsonValue ? jsonValue.toString() : "");
		}
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {

	}

	@Override
	protected void onComplete(Page doingPage) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

}
