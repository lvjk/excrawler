package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月4日 下午5:18:21
 */
public class Cq315housePresale1Worker extends HtmlCommonWorker {

	final static Logger LOG = LoggerFactory.getLogger(Cq315housePresale1Worker.class);
	private String referer = "http://www.cq315house.com/315web/HtmlPage/PresaleCertDetail.htm#";
	private String pageSizeTemplate="<<pageSize>>";
	private String pageIndexTemplate="<<pageIndex>>";
	String jsonTemplateUrl = "http://www.cq315house.com/315web/webservice/GetMyData913.ashx"
			+ "?projectname=&kfs=&projectaddr=&pagesize="+pageSizeTemplate
			+ "&pageindex="+pageIndexTemplate
			+ "&presalecert=";
	RedisWorkQueue presale2Queue;
	private int pageIndex = 83;//83没有数据
	private int pageSize = 100;
	Map<String, String> fieldMap;

	public Cq315housePresale1Worker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	private Page buildPage(int pageIndex,int pageSize){
		String jsonUrl=StringUtils.replace(jsonTemplateUrl, pageIndexTemplate, String.valueOf(pageIndex));
		jsonUrl=StringUtils.replace(jsonUrl, pageSizeTemplate, String.valueOf(pageSize));
		Page page = new Page(getSite().getCode(), 1, jsonUrl, jsonUrl);
		page.setReferer(referer);
		page.setMethod(HttpMethod.GET);
		page.setType(PageType.JSON.value());
		return page;
	}
	protected void insideInit(){
		presale2Queue = new RedisWorkQueue(getManager().getRedisManager(), "cq315house_presale_2");
		fieldMap = new HashMap<>();
		fieldMap.put("F_PROJECT_NAME", "projectName");
		fieldMap.put("F_SITE", "area");
		fieldMap.put("F_ADDR", "address");
		fieldMap.put("F_ENTERPRISE_NAME", "company");
		fieldMap.put("F_PRESALE_CERT", "presalePermit");
		fieldMap.put("F_BLOCK", "forSaleUnit");
		Page firstPage = buildPage(pageIndex, pageSize);//初始化第一页
		getWorkQueue().clear();
		getWorkQueue().push(firstPage);
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String jsonData = doingPage.getPageSrc();
		if (StringUtils.isBlank(jsonData) && pageIndex == 1) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			throw new RuntimeException("get jsonData is blank");
		}
		if (("[]".equals(jsonData)||StringUtils.isBlank(jsonData)) && pageIndex > 1) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
		}else{
			String proxyJsonKey = "json";
			jsonData = "{'" + proxyJsonKey + "':" + jsonData + "}";
			Map<String, Object> jsonMap = JsonUtils.toObject(jsonData, Map.class);
			List<Map<String, Object>> projectList = (List<Map<String, Object>>) jsonMap.get(proxyJsonKey);
			Map<String, List<String>> result = new HashMap<>();
			String projectIdKey = "PARENTPROJID";
			for (Map<String, Object> projectMap : projectList) {
				Object projectId = projectMap.get(projectIdKey);
				if (null == projectId) {
					throw new RuntimeException("jsonMap don't contain this key[" + projectIdKey + "] ");
				}
				String preReferer = "ProjectDetailPre.htm?projectId=" + projectId;
				preReferer = UrlUtils.paserUrl(doingPage.getBaseUrl(), referer, preReferer);

				String preSale2Url = "../webservice/GetMyData112.ashx?type=1&projectId=" + projectId;
				preSale2Url = UrlUtils.paserUrl(doingPage.getBaseUrl(), preReferer, preSale2Url);
				Page preSale2Page = new Page(doingPage.getSiteCode(), 1, preSale2Url, preSale2Url);
				preSale2Page.setReferer(preReferer);
				preSale2Page.setType(PageType.DATA.value());
				presale2Queue.push(preSale2Page);
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
	protected void afterPaser(Page doingPage) throws Exception {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {
		
	}
	
	@Override
	public void onComplete(Page p) {
		pageIndex++;
		Page newPage = buildPage(pageIndex, pageSize);
		getWorkQueue().push(newPage);
	}
}
