package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月4日 下午5:18:21
 */
public class Cq315housePresell1Worker extends AbstractCrawlWorker {

	final static Logger LOG = LoggerFactory.getLogger(Cq315housePresell1Worker.class);
	private String referer = "http://www.cq315house.com/315web/HtmlPage/PresaleCertDetail.htm#";
	private String pageSizeTemplate = "<<pageSize>>";
	private String pageIndexTemplate = "<<pageIndex>>";
	String jsonTemplateUrl = "http://www.cq315house.com/315web/webservice/GetMyData913.ashx"
			+ "?projectname=&kfs=&projectaddr=&pagesize=" + pageSizeTemplate + "&pageindex=" + pageIndexTemplate
			+ "&presalecert=";
	RedisWorkQueue presell2Queue;
	private int pageIndex = 1;// 83没有数据
	private int pageSize = 100;
	Map<String, String> fieldMap;

	private Page buildPage(int pageIndex, int pageSize) {
		String jsonUrl = StringUtils.replace(jsonTemplateUrl, pageIndexTemplate, String.valueOf(pageIndex));
		jsonUrl = StringUtils.replace(jsonUrl, pageSizeTemplate, String.valueOf(pageSize));
		Page page = new Page(getSite().getCode(), 1, jsonUrl, jsonUrl);
		page.setReferer(referer);
		page.setMethod(HttpMethod.GET);
		page.setType(PageType.JSON.value());
		return page;
	}

	protected void insideInit() {
		presell2Queue = new RedisWorkQueue(getManager().getRedisManager(), "cq315house_presell_2");
		fieldMap = new HashMap<>();
		fieldMap.put("PARENTPROJID", "projectId");
		fieldMap.put("F_PROJECT_NAME", "projectName");
		fieldMap.put("F_SITE", "district");
		fieldMap.put("F_ADDR", "address");
		fieldMap.put("F_ENTERPRISE_NAME", "companyName");
		fieldMap.put("F_PRESALE_CERT", "presellPermit");
		fieldMap.put("F_BLOCK", "forSellBuilding");
		Page firstPage = buildPage(pageIndex, pageSize);// 初始化第一页
		getWorkQueue().clear();
		getWorkQueue().push(firstPage);
	}

	protected void beforeDown(Page doingPage) {

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExtract(Page doingPage) {
		String jsonData = doingPage.getPageSrc();
		if (StringUtils.isBlank(jsonData) && pageIndex == 1) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			throw new RuntimeException("get jsonData is blank");
		}
		if (("[]".equals(jsonData) || StringUtils.isBlank(jsonData)) && pageIndex > 1) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.FINISHED);
		} else {
			String proxyJsonKey = "json";
			jsonData = "{'" + proxyJsonKey + "':" + jsonData + "}";
			Map<String, Object> jsonMap = JsonUtils.toObject(jsonData, Map.class);
			List<Map<String, Object>> projectList = (List<Map<String, Object>>) jsonMap.get(proxyJsonKey);
			Map<String, List<String>> result = new HashMap<>();
			for (Map<String, Object> projectMap : projectList) {
				for (String jsonKey : fieldMap.keySet()) {
					Object value = projectMap.get(jsonKey);
					if (null == value) {
						throw new RuntimeException("jsonMap don't contain this key[" + jsonKey + "] ");
					}
					String field = fieldMap.get(jsonKey);
					result.computeIfAbsent(field, mapKey -> new ArrayList<>()).add(value.toString());
				}
			}
			doingPage.getMetaMap().putAll(result);
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

	@Override
	public void onComplete(Page doingPage, ResultContext resultContext) {
		List<String> projectIds = doingPage.getMetaMap().get("projectId");
		for (int i = 0; i < projectIds.size(); i++) {
			String projectId = projectIds.get(i);
			String pre1Id = resultContext.getOutResults().get(i).get(Constants.DEFAULT_RESULT_ID);
			String preReferer = "ProjectDetailPre.htm?projectId=" + projectId;
			preReferer = UrlUtils.paserUrl(doingPage.getBaseUrl(), referer, preReferer);
			String presell2Url = "../webservice/GetMyData112.ashx?type=1&projectId=" + projectId;
			presell2Url = UrlUtils.paserUrl(doingPage.getBaseUrl(), preReferer, presell2Url);
			Page presell2Page = new Page(doingPage.getSiteCode(), 1, presell2Url, presell2Url);
			presell2Page.setReferer(preReferer);
			presell2Page.getMetaMap().put("presellId_1", Arrays.asList(pre1Id));
			presell2Queue.push(presell2Page);
		}
		pageIndex++;
		Page newPage = buildPage(pageIndex, pageSize);
		getWorkQueue().push(newPage);
	}
}
