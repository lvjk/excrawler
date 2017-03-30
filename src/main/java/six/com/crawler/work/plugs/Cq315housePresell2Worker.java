package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月10日 上午9:38:59
 */
public class Cq315housePresell2Worker extends AbstractCrawlWorker {

	RedisWorkSpace<Page> suiteStateQueue;
	Map<String, String> fieldMap;

	@Override
	public void insideInit() {
		suiteStateQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(),"cq315house_house_state", Page.class);
		fieldMap = new HashMap<>();
		fieldMap.put("presellId_1", "presellId_1");//
		fieldMap.put("PARENTPROJNAME", "projectName");//
		fieldMap.put("F_ENTERPRISE_NAME", "companyName");
		fieldMap.put("F_ADDR", "address");
		fieldMap.put("F_PRESALE_CERT", "presellPermit");
		fieldMap.put("F_PROJECT_NAME", "buildingName");
		fieldMap.put("F_BLOCK", "loudong");
		fieldMap.put("KSZZNUM", "forSellhouse");
		fieldMap.put("KSFZZNUM", "forSellNonhouse");
		fieldMap.put("PRESALECOUNT", "forSellNum");
		fieldMap.put("PRESALEAREA", "forSellArea");
		fieldMap.put("F_REGISTER_DATE", "banjieTime");
		fieldMap.put("BUILDID", "buildId");
	}

	protected void beforeDown(Page doingPage) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String jsonData = doingPage.getPageSrc();
		String proxyJsonKey = "json";
		jsonData = "{'" + proxyJsonKey + "':" + jsonData + "}";
		Map<String, Object> jsonMap = JsonUtils.toObject(jsonData, Map.class);
		List<Map<String, Object>> projectPresaleList = (List<Map<String, Object>>) jsonMap.get(proxyJsonKey);
		Map<String, List<String>> result = new HashMap<>();
		List<String> presellId_1s = doingPage.getMetaMap().get("presellId_1");
		String presellId_1 = presellId_1s.get(0);
		doingPage.getMetaMap().remove("presellId_1");
		for (Map<String, Object> projectPresaleMap : projectPresaleList) {
			projectPresaleMap.put("presellId_1", presellId_1);
			for (String jsonKey : fieldMap.keySet()) {
				Object value = projectPresaleMap.get(jsonKey);
				if (null == value) {
					throw new RuntimeException("jsonMap don't contain this key[" + jsonKey + "] ");
				}
				result.computeIfAbsent(fieldMap.get(jsonKey), mapKey -> new ArrayList<>()).add(value.toString());
			}
		}
		doingPage.getMetaMap().putAll(result);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {
	}

	@Override
	public void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> buildIdStrs = resultContext.getExtractResult("buildId");
		List<String> blockStrs = resultContext.getExtractResult("loudong");
		String separatorChars = ",";
		for (int i = 0; i < buildIdStrs.size(); i++) {
			String presellId = resultContext.getOutResults().get(i).get(Extracter.DEFAULT_RESULT_ID);
			String buildidStr = buildIdStrs.get(i);
			String blockStr = blockStrs.get(i);
			String[] buildids = StringUtils.split(buildidStr, separatorChars);
			String[] blocks = StringUtils.split(blockStr, separatorChars);
			for (int j = 0; j < buildids.length; j++) {
				String buildid = buildids[j];
				String block = blocks[j];
				String buildingUrl = "ShowRoomsNew.aspx?block=" + block + "&buildingid=" + buildid;
				buildingUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getReferer(), buildingUrl);
				Page buildingPage = new Page(doingPage.getSiteCode(), 1, buildingUrl, buildingUrl);
				buildingPage.setReferer(doingPage.getFinalUrl());
				buildingPage.getMetaMap().put("presellId", Arrays.asList(presellId));
				buildingPage.getMetaMap().put("buildingUnit", Arrays.asList(block));
				suiteStateQueue.push(buildingPage);
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
