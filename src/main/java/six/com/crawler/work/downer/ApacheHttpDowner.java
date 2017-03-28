package six.com.crawler.work.downer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.exception.AbstractHttpException;
import six.com.crawler.http.HttpClient;
import six.com.crawler.http.HttpConstant;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.http.HttpResult;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.exception.DownerException;
import six.com.crawler.work.downer.exception.ManyRedirectDownException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月14日 上午11:17:35
 */
public class ApacheHttpDowner extends AbstractDowner {

	protected final static Logger LOG = LoggerFactory.getLogger(ApacheHttpDowner.class);

	private HttpClient httpClient;

	public ApacheHttpDowner(AbstractCrawlWorker worker) {
		super(worker);
		httpClient = worker.getManager().getHttpClient();
	}

	protected HttpResult insideDown(Page page) throws DownerException {
		HttpResult result = executeDown(page);
		String html = httpClient.getHtml(result, page.getType() == PageType.XML ? ContentType.XML : ContentType.HTML);
		String charset = result.getCharset();
		page.setPageSrc(html);
		page.setCharset(charset);
		return result;
	}

	private HttpResult executeDown(Page page) throws DownerException {
		String requestUrl = page.getOriginalUrl();
		String requesReferer = page.getReferer();
		HttpMethod httpMethod = page.getMethod();
		PostContentType postContentType = page.getPostContentType();
		Map<String, Object> parameters = page.getParameters();
		HttpResult result = null;
		int redirectTime = 0;
		HttpProxy httpProxy = null;
		do {
			httpProxy = getHttpProxy();
			HttpUriRequest httpUriRequest = httpClient.buildApacheRequest(requestUrl, requesReferer, httpMethod,
					HttpConstant.headMap, postContentType, parameters, httpProxy);
			LOG.info("execute request[" + requestUrl + "] by proxy["
					+ (null != httpProxy ? httpProxy.toString() : "noproxy") + "]");
			try {
				result = httpClient.executeHttpUriRequest(httpUriRequest);
			} catch (AbstractHttpException e) {
				// request execute 异常处理
				throw new ManyRedirectDownException(
						"execute request[" + httpUriRequest.getURI() + "] err", e);
			}
			if (StringUtils.isNotBlank(result.getRedirectedUrl())) {
				requestUrl = result.getRedirectedUrl();
				requesReferer = result.getReferer();
				httpMethod = HttpMethod.GET;
				postContentType = null;
				parameters = null;
				redirectTime++;
				if (redirectTime > HttpConstant.REDIRECT_TIMES) {
					// 抛重定向次数过多异常
					throw new ManyRedirectDownException(
							"execute request[" + httpUriRequest.getURI() + "] redirectTime is too many");
				}
			}
		} while (StringUtils.isNotBlank(result.getRedirectedUrl()));
		return result;
	}

	public byte[] downBytes(Page page) throws DownerException {
		HttpResult result = executeDown(page);
		return result.getData();
	}
	

	@Override
	protected void insideColose() {
		String domain = UrlUtils.getDomain(getHtmlCommonWorker().getSite().getMainUrl());
		httpClient.getCookiesStore().close(domain);
	}

}
