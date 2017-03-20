package six.com.crawler.schedule;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import okhttp3.Request;
import six.com.crawler.entity.Node;
import six.com.crawler.http.HttpClient;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.http.HttpResult;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午3:49:06
 */
public abstract class RemotingClient {

	final static Logger log = LoggerFactory.getLogger(RemotingClient.class);
			
	@Autowired
	private HttpClient httpClient;

	public String doExecute(Node targetNode, String path) {
		if (null == targetNode) {
			throw new RuntimeException("call targetNode is null");
		}
		if (StringUtils.isBlank(path)) {
			throw new RuntimeException("call targetNode's path is blank");
		}
		String url = "http://" + targetNode.getHost() + ":" + targetNode.getPort() + path;
		String json =null;
		try{
			Request request = httpClient.buildRequest(url, null, HttpMethod.GET, null, null, null);
			HttpResult httpResult = httpClient.executeRequest(request);
			json = httpClient.getHtml(httpResult, ContentType.OTHER);
		}catch (Exception e) {
			log.error("call "+url+" err",e);
		}
		return json;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
}
