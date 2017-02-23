package six.com.crawler.work;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月10日 上午9:38:59
 */
public class Cq315housePresale2Worker extends HtmlCommonWorker {

	RedisWorkQueue suiteStateQueue;
	Map<String, String> fieldMap;

	public Cq315housePresale2Worker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	public void insideInit() {
		suiteStateQueue = new RedisWorkQueue(getManager().getRedisManager(), "cq315house_house_state");
		fieldMap = new HashMap<>();
		fieldMap.put("PARENTPROJNAME", "projectName");//
		fieldMap.put("F_ENTERPRISE_NAME", "company");
		fieldMap.put("F_ADDR", "address");
		fieldMap.put("F_PRESALE_CERT", "presalePermit");
		fieldMap.put("F_PROJECT_NAME", "buildingName");
		fieldMap.put("F_BLOCK", "loudong");
		fieldMap.put("KSZZNUM", "forSalehouse");
		fieldMap.put("KSFZZNUM", "forSaleNonhouse");
		fieldMap.put("PRESALECOUNT", "forSaleNum");
		fieldMap.put("PRESALEAREA", "forSaleArea");
		fieldMap.put("F_REGISTER_DATE", "banjieTime");
	}

	@Override
	public void onComplete(Page p) {

	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String jsonData = doingPage.getPageSrc();
		String proxyJsonKey = "json";
		jsonData = "{'" + proxyJsonKey + "':" + jsonData + "}";
		Map<String, Object> jsonMap = JsonUtils.toObject(jsonData, Map.class);
		List<Map<String, Object>> projectPresaleList = (List<Map<String, Object>>) jsonMap.get(proxyJsonKey);
		Map<String, List<String>> result = new HashMap<>();
		String projectNameKey = "PARENTPROJNAME";
		String companyKey = "F_ENTERPRISE_NAME";
		String presalePermitKey = "F_PRESALE_CERT";
		String addressKey = "F_ADDR";
		String buildidKey = "BUILDID";
		String blockKey = "F_BLOCK";
		for (Map<String, Object> projectPresaleMap : projectPresaleList) {
			String projectName = (String) projectPresaleMap.get(projectNameKey);
			String company = (String) projectPresaleMap.get(companyKey);
			String address = (String) projectPresaleMap.get(addressKey);
			String presalePermit = (String) projectPresaleMap.get(presalePermitKey);
			String buildidStr = (String) projectPresaleMap.get(buildidKey);
			String blockStr = (String) projectPresaleMap.get(blockKey);
			String[] buildids = buildidStr.split(",");
			String[] blocks = blockStr.split(",");
			for (int i = 0; i < buildids.length; i++) {
				String buildid = buildids[i];
				String block = blocks[i];
				String buildingUrl = "ShowRoomsNew.aspx?block=" + block + "&buildingid=" + buildid;
				buildingUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getReferer(), buildingUrl);
				Page buildingPage = new Page(doingPage.getSiteCode(), 1, buildingUrl, buildingUrl);
				buildingPage.setReferer(doingPage.getFinalUrl());
				buildingPage.setType(PageType.DATA.value());
				buildingPage.getMetaMap().put("projectName", Arrays.asList(projectName));
				buildingPage.getMetaMap().put("company", Arrays.asList(company));
				buildingPage.getMetaMap().put("address", Arrays.asList(address));
				buildingPage.getMetaMap().put("presalePermit", Arrays.asList(presalePermit));
				buildingPage.getMetaMap().put("buildingUnit", Arrays.asList(block));
				suiteStateQueue.push(buildingPage);
			}
			for (String jsonKey : fieldMap.keySet()) {
				Object value = projectPresaleMap.get(jsonKey);
				if (null == value) {
					throw new RuntimeException("jsonMap don't contain this key[" + jsonKey + "] ");
				}

				String field = fieldMap.get(jsonKey);
				result.computeIfAbsent(field, mapKey -> new ArrayList<>()).add(value.toString());
			}
		}
		doingPage.getMetaMap().putAll(result);
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {}

}
