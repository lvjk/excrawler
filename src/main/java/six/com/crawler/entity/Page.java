package six.com.crawler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.MD5Utils;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.downer.PostContentType;
import six.com.crawler.work.space.Index;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午8:13:12 类说明
 */
public class Page implements WorkSpaceData, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1639855924033150969L;

	private String siteCode;// 页面所属站点

	private String baseUrl;// base url

	private String ancestorUrl;// 从哪个page 获取到此页面link

	private String pageKey;// 页面唯一key

	private String originalUrl;// 原始link 避免跳转

	private String finalUrl;// 最终link 避免跳转

	private String firstUrl;// 页面第一页link

	private String referer;// 请求从哪个网页过来的

	private int pageNum;// 分页时 第几页

	private HttpMethod method = HttpMethod.GET;// 默认为get

	private PageType type;// 页面类型

	private String charset;// 编码

	private int retryProcess;// 尝试处理次数

	private int fztRetryProcess;// 502尝试重试次数

	private Map<String, Object> parameters;// 请求页面所需参数

	private PostContentType postContentType;// post type

	private int depth = 1;// 页面深度

	private transient String pageSrc;// 页面源码

	private transient Document doc;// page Document

	private String waitJsLoadElement;// 等待js加载 元素

	private Map<String, List<String>> metaMap = new HashMap<>();

	private int noNeedDown;// 不需要下载:1 需要下载默认:0

	private Index index;// 工作空间index

	public PostContentType getPostContentType() {
		return postContentType;
	}

	public void setPostContentType(PostContentType postContentType) {
		this.postContentType = postContentType;
	}

	public Page() {
	}

	public Page(String siteCode, int pageNum, String firstUrl, String originalUrl) {
		if (StringUtils.isBlank(siteCode)) {
			throw new IllegalArgumentException("Page site must be != NullOrEmpty");
		}
		if (pageNum <= 0) {
			throw new IllegalArgumentException("Page num must be >= 1");
		}
		if (StringUtils.isBlank(originalUrl)) {
			throw new IllegalArgumentException("Page originalUrl must be != NullOrEmpty");
		}
		this.siteCode = siteCode;
		this.pageNum = pageNum;
		this.firstUrl = firstUrl;
		this.finalUrl = originalUrl;
		this.originalUrl = originalUrl;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public String getFirstUrl() {
		return firstUrl;
	}

	public HttpMethod getMethod() {
		if (null == method) {
			method = HttpMethod.GET;
		}
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAncestorUrl() {
		return ancestorUrl;
	}

	public void setAncestorUrl(String ancestorUrl) {
		this.ancestorUrl = ancestorUrl;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getPageSrc() {
		return pageSrc;
	}

	public void setPageSrc(String pageSrc) {
		this.pageSrc = pageSrc;
		doc = Jsoup.parse(pageSrc);
	}

	public Document getDoc() {
		if (null == doc && StringUtils.isNotBlank(pageSrc)) {
			doc = Jsoup.parse(pageSrc);
		}
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public int getRetryProcess() {
		return retryProcess;
	}

	public void setRetryProcess(int retryProcess) {
		this.retryProcess = retryProcess;
	}

	public int getFztRetryProcess() {
		return fztRetryProcess;
	}

	public void setFztRetryProcess(int fztRetryProcess) {
		this.fztRetryProcess = fztRetryProcess;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}

	public int getPageNum() {
		return pageNum;
	}

	public PageType getType() {
		return type;
	}

	public void setType(int type) {
		this.type = PageType.valueOf(type);
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public void setFirstUrl(String firstUrl) {
		this.firstUrl = firstUrl;
	}

	/**
	 * 获取下载后最后跳转后的url
	 * 
	 * @return finalUrl
	 */
	public String getFinalUrl() {
		if (null == finalUrl) {
			return originalUrl;
		} else {
			return finalUrl;
		}
	}

	/**
	 * 设置 下载后最后跳转后的url
	 * 
	 * @param finalUrl
	 */
	public void setFinalUrl(String finalUrl) {
		this.finalUrl = finalUrl;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getWaitJsLoadElement() {
		return waitJsLoadElement;
	}

	public void setWaitJsLoadElement(String waitJsLoadElement) {
		this.waitJsLoadElement = waitJsLoadElement;
	}

	public int getNoNeedDown() {
		return noNeedDown;
	}

	public void setNoNeedDown(int noNeedDown) {
		this.noNeedDown = noNeedDown;
	}

	public String getPageKey() {
		if (null == pageKey) {
			String temp = originalUrl;
			if (null != parameters && !parameters.isEmpty()) {
				String parametersJson = JsonUtils.toJson(parameters);
				temp += parametersJson;
			}
			pageKey = MD5Utils.MD5(temp);
		}
		return pageKey;
	}

	public void addMeta(String key, String meta) {
		metaMap.computeIfAbsent(key, mapKey -> new ArrayList<>()).add(meta);
	}

	public List<String> getMeta(String key) {
		return metaMap.get(key);
	}

	public Map<String, List<String>> getMetaMap() {
		return metaMap;
	}

	public String toString() {
		StringBuilder strBuf = new StringBuilder();
		if (getMethod().value.equals(HttpMethod.POST.value)) {
			Map<String, Object> params = getParameters();
			if (null != params && params.size() > 0) {
				for (String key : params.keySet()) {
					strBuf.append(key + "=" + params.get(key) + "&");
				}
				strBuf.delete(strBuf.length() - 1, strBuf.length());
			}
			strBuf.append("?post@");
			strBuf.append(getFinalUrl());
		} else {
			strBuf.append("get@" + getFinalUrl());
		}
//		if (!getMetaMap().isEmpty()) {
//			String metaJson = JsonUtils.toJson(getMetaMap());
//			strBuf.append("@meta:" + metaJson);
//		}
		return strBuf.toString();
	}

	@Override
	public String getKey() {
		return getPageKey();
	}

	@Override
	public void setIndex(Index index) {
		this.index = index;
	}

	@Override
	public Index getIndex() {
		return index;
	}

	public static void main(String[] args) {
		String url = "http://www.tmsf.com/newhouse/property_33_12493_info.htm";
		String pageKey = MD5Utils.MD5(url);
		System.out.println(pageKey);
	}
}
