package six.com.crawler.admin.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import okhttp3.Request;
import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.admin.service.ExtractPathService;
import six.com.crawler.admin.vo.TestExtractPathVo;
import six.com.crawler.dao.ExtractPathDao;
import six.com.crawler.entity.Page;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.work.downer.HttpClient;
import six.com.crawler.work.downer.HttpMethod;
import six.com.crawler.work.downer.HttpResult;
import six.com.crawler.work.extract.ExtractPath;
import six.com.crawler.work.extract.PathType;

/**
 * @author six
 * @date 2016年8月18日 上午10:19:11 解析规则服务器
 */
@Component
public class ExtractPathServiceImpl implements ExtractPathService {

	final static Logger LOG = LoggerFactory.getLogger(ExtractPathServiceImpl.class);
	@Autowired
	private ExtractPathDao pathdao;

	@Autowired
	private HttpClient httpClient;

	Map<String, Map<String, List<ExtractPath>>> maps = new HashMap<String, Map<String, List<ExtractPath>>>();

	public List<ExtractPath> getPaserPath(String siteCode, PathType pathType, Page page) {
		Map<String, List<ExtractPath>> map = maps.get(siteCode);
		List<ExtractPath> result = null;
		String key = pathType.toString() + page.getDepth();
		if (null != map) {
			result = map.get(key);
		}
		return result;
	}

	/**
	 * 通过siteCode pathType ranking 获取path
	 * 
	 * @param siteCode
	 *            网站code
	 * @param pathType
	 *            path类型
	 * @param ranking
	 *            排名
	 * @return
	 */
	public List<ExtractPath> query(String pathName, String siteCode) {
		List<ExtractPath> result = pathdao.queryBySiteAndName(siteCode,pathName);
		return result;
	}

	public void fuzzyQuery(ResponseMsg<List<ExtractPath>> responseMsg,String pathName, String siteCode) {
		List<ExtractPath> result = pathdao.fuzzyQuery(pathName, siteCode);
		responseMsg.setData(result);
		responseMsg.setIsOk(1);
	}

	/**
	 * 更新解析path
	 * 
	 * @param siteCode
	 * @param path
	 */
	public void updatePaserPath(ExtractPath path) {
	}

	/**
	 * 添加指定站点path
	 * 
	 * @param siteCode
	 * @param path
	 */
	public void addPaserPath(ExtractPath path) {
		pathdao.batchSave(Arrays.asList(path));
	}

	/**
	 * 批量添加指定站点paths
	 * 
	 * @param siteCode
	 * @param paths
	 */
	public void addPaserPaths(List<ExtractPath> paths) {
		if (null != paths) {
			for (ExtractPath path : paths) {
				addPaserPath(path);
			}
		}
	}

	@Override
	public List<ExtractPath> query(String siteCode) {
		List<ExtractPath> result = pathdao.queryBySite(siteCode);
		return result;
	}

	@Override
	public List<String> testExtract(TestExtractPathVo extractPath) {
		String testUrl = extractPath.getTestUrl();
		String testHtml = extractPath.getTestHtml();
		List<String> extractResult = null;
		if (null == extractPath || StringUtils.isBlank(extractPath.getName())
				|| StringUtils.isBlank(extractPath.getSiteCode()) || StringUtils.isBlank(extractPath.getPath())) {
			extractResult = Collections.emptyList();
		} else {
			if (StringUtils.isBlank(testHtml) && StringUtils.isNotBlank(testUrl)) {
				Request request = httpClient.buildRequest(testUrl, null, HttpMethod.GET, null, null, null);
				HttpResult httpResult = httpClient.executeRequest(request);
				testHtml = httpClient.getHtml(httpResult, ContentType.HTML);
			}
			if (StringUtils.isBlank(testHtml)) {
				extractResult = Collections.emptyList();
			} else {
				Document document = Jsoup.parse(testHtml);
				extractResult = JsoupUtils.extract(document, extractPath);
			}
		}
		return extractResult;
	}

	@Override
	public void saveExtractPath(List<ExtractPath> path) {
		pathdao.batchSave(path);
	}

	@Override
	public void updateExtractPath(ExtractPath path) {

	}

	public void delExtractPathBySiteCide(String siteCode) {
		pathdao.delBySiteCode(siteCode);
	}

	public void delExtractPathByName(String name) {
		pathdao.delByName(name);
	}

	public ExtractPathDao getPathdao() {
		return pathdao;
	}

	public void setPathdao(ExtractPathDao pathdao) {
		this.pathdao = pathdao;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
