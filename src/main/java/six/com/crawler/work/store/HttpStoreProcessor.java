package six.com.crawler.work.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Request;
import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.exception.AbstractHttpException;
import six.com.crawler.common.http.HttpClient;
import six.com.crawler.common.http.HttpConstant;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.downer.PostContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午11:26:50
 */
public class HttpStoreProcessor extends StoreAbstarct {

	protected final static Logger LOG = LoggerFactory.getLogger(HttpStoreProcessor.class);

	private HttpClient httpClient;
	List<ResultContext> tempCache;
	int batchSize;
	String sendHttpUlr;
	HttpMethod method;

	public HttpStoreProcessor(AbstractWorker worker, List<String> resultKeys) {
		super(worker, resultKeys);
		httpClient = worker.getManager().getHttpClient();
		sendHttpUlr = worker.getJob().getParameter(JobConTextConstants.SEND_HTTP_URL, String.class);
		String everySendSizeStr = worker.getJob().getParameter(JobConTextConstants.BATCH_SIZE, String.class);
		if (null != everySendSizeStr) {
			batchSize = Integer.valueOf(everySendSizeStr);
		} else {
			batchSize = 20;
		}
		tempCache = new ArrayList<>(batchSize);
		String httpMethod = worker.getJob().getParameter(JobConTextConstants.SEND_HTTP_METHOD, String.class);
		method = "post".equalsIgnoreCase(httpMethod) ? HttpMethod.POST : HttpMethod.GET;
	}

	@Override
	protected int insideStore(ResultContext resultContext) throws StoreException {
		tempCache.add(resultContext);
		if (tempCache.size() >= batchSize) {
			Map<String, String> headMap = HttpConstant.headMap;
			PostContentType postContentType = PostContentType.JSON;
			Map<String, Object> parameters = new HashMap<>();
			String json = JsonUtils.toJson(tempCache);
			parameters.put("content", json);
			Request request = httpClient.buildRequest(sendHttpUlr, null, method, headMap, postContentType, parameters,null);
			HttpResult result;
			try {
				result = httpClient.executeRequest(request);
				LOG.info("seed http[" + sendHttpUlr + "] status: " + result.getCode());
				LOG.info("json data: " + json);
				tempCache.clear();
			} catch (AbstractHttpException e) {
				LOG.error("seed http err", e);
				throw new StoreException(e);
			}

		}
		return 1;
	}

}
