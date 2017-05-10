package six.com.crawler.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.exception.AbstractHttpException;
import six.com.crawler.http.exception.HttpIoException;
import six.com.crawler.http.exception.HttpReadDataMoreThanMaxException;
import six.com.crawler.utils.AutoCharsetDetectorUtils;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.utils.JsonUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.downer.CookiesStore;
import six.com.crawler.work.downer.MX509TrustManager;
import six.com.crawler.work.downer.PostContentType;
import six.com.crawler.work.downer.exception.HttpFiveZeroTwoException;
import six.com.crawler.work.downer.exception.UnknownHttpStatusDownException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午11:30:10
 */
@Component
public class HttpClient implements InitializingBean {

	private final static Logger LOG = LoggerFactory.getLogger(HttpClient.class);

	@Autowired
	private SpiderConfigure configure;

	private OkHttpClient okClient;

	private CookiesStore cookiesStore;

	private CloseableHttpClient httpClient;

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public boolean isValidHttpProxy(HttpProxy httpProxy) {
		if (null != httpProxy && null != httpProxy.getHost() && 0 != httpProxy.getPort()) {
			String httpProxyTestUrl = configure.getConfig("httpProxy.test.url", "http://www.baidu.com");
			Map<String, String> headMap = new HashMap<String, String>();
			headMap.putAll(HttpConstant.headMap);
			Request request = buildRequest(httpProxyTestUrl, null, HttpMethod.GET, headMap, null, null, httpProxy);
			try {
				HttpResult httpResult = executeRequest(request);
				if (httpResult.getCode() == 200) {
					return true;
				}
			} catch (AbstractHttpException e) {
				LOG.error("checkHttpProxy err:" + httpProxy.toString(), e);
			}
		}
		return false;
	}

	public Request buildRequest(String url, String referer, HttpMethod method, Map<String, String> headMap,
			PostContentType postContentType, Map<String, Object> parameters) {
		return buildRequest(url, referer, method, headMap, postContentType, parameters, null);
	}

	public static String urlEncoder(String str) {
		try {
			return URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public Request buildRequest(String url, String referer, HttpMethod method, Map<String, String> headMap,
			PostContentType postContentType, Map<String, Object> parameters, HttpProxy httpProxy) {
		if (StringUtils.isBlank(url)) {
			throw new RuntimeException("the url must not be blank");
		}
		Builder build = null;
		String contentType = null;
		if (HttpMethod.POST == method) {
			RequestBody body = null;
			if (PostContentType.JSON == postContentType) {
				String json = JsonUtils.toJson(parameters);
				json = urlEncoder(json);
				body = RequestBody.create(JSON, json);
			} else {
				FormBody.Builder bodyBuild = new FormBody.Builder();
				for (String key : parameters.keySet()) {
					String value = parameters.get(key).toString();
					value = urlEncoder(value);
					bodyBuild.addEncoded(key, value);
				}
				body = bodyBuild.build();
				contentType = HttpConstant.CONTENT_TYPE_FORM_VALUE;
			}
			build = new Builder().url(url).post(body);
		} else {
			build = new Builder().url(url).get();
		}
		if (StringUtils.isNotBlank(contentType)) {
			build.addHeader(HttpConstant.CONTENT_TYPE, contentType);
		}
		String host = UrlUtils.getHost(url);
		build.addHeader(HttpConstant.HOST, host);

		String origin = UrlUtils.getMainUrl(url);
		build.addHeader(HttpConstant.ORIGIN, origin);

		String domain = UrlUtils.getDomain(url);
		okhttp3.Cookie cookie = cookiesStore.getCookieByDamainAndKey(domain, HttpConstant.COOKIE_XSRF);
		if (null != cookie) {
			build.addHeader(HttpConstant.X_Xsrftoken, cookie.value());
		}
		if (null != headMap) {
			for (String name : headMap.keySet()) {
				build.addHeader(name, headMap.get(name));
			}
		}
		if (null != referer) {
			build.addHeader(HttpConstant.REFERER, referer);
		}
		String headName = HttpConstant.CONNECTION;
		String headValue = HttpConstant.KEEP_ALIVE;
		if (null != httpProxy) {
			SocketAddress sa = new InetSocketAddress(httpProxy.getHost(), httpProxy.getPort());
			Proxy proxy = new Proxy(Type.HTTP, sa);
			build.proxy(proxy);
			if (StringUtils.isNotBlank(httpProxy.getUserName())) {
				String nameAndPass = httpProxy.getUserName() + ":" + httpProxy.getPassWord();
				String encoding = new String(Base64.getEncoder().encode(nameAndPass.getBytes()));
				build.addHeader("Proxy-Authorization", "Basic " + encoding);
			}
			headName = HttpConstant.PROXY_CONNECTION;
			headValue = HttpConstant.KEEP_ALIVE;
		}
		build.addHeader(headName, headValue);
		return build.build();
	}

	public HttpResult executeRequest(Request request) throws AbstractHttpException, HttpFiveZeroTwoException {
		if (null == request) {
			throw new RuntimeException("url must not be blank.");
		}
		String url = request.url().toString();
		HttpResult result = null;
		long beforeRequestTime = System.currentTimeMillis();
		try (Response response = okClient.newCall(request).execute();) {
			result = new HttpResult();
			int httpCode = response.code();
			result.setCode(httpCode);
			result.setHeaders(response.headers());
			if (null != response.body().contentType() && null != response.body().contentType().charset()) {
				String charset = response.body().contentType().charset().name();
				result.setCharset(charset);
			}
			byte[] data = readLimited(response);
			result.setData(data);
			if (httpCode >= 300 && httpCode < 400) {
				String redirectUrl = response.header("Location");
				if (StringUtils.isBlank(redirectUrl)) {
					throw new RuntimeException("httpCode >= 300 && httpCode < 400 , but redirectUrl is blank:" + url);
				}
				result.setRedirectedUrl(redirectUrl);
				result.setReferer(url);
			} else if (httpCode == 502) {
				throw new HttpFiveZeroTwoException("httpCode[" + httpCode + "]:" + url);
			} else if (httpCode != 200) {
				throw new UnknownHttpStatusDownException("httpCode[" + httpCode + "]:" + url);
			}
		} catch (IOException e) {
			// request execute 异常处理
			long afterRequestTime = System.currentTimeMillis();
			throw new HttpIoException("execute request[" + request.url() + "] by proxy[" + request.proxy()
					+ "] err, request time is [" + (afterRequestTime - beforeRequestTime) + "]", e);
		}
		return result;
	}

	/**
	 * okhttp 已经内置支持gzip和deflate 解压了
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private static byte[] readLimited(Response response) throws AbstractHttpException {
		final long length = response.body().contentLength();
		byte[] bytes = null;
		// 判断是否大于HTML_MAX_CONTENT_LENGTH 如果是抛出 HttpReadDataMoreThanMaxException
		// 异常
		if (length > (long) HttpConstant.HTML_MAX_CONTENT_LENGTH) {
			throw new HttpReadDataMoreThanMaxException(
					"read bytes length[" + length + "]more than max:" + HttpConstant.HTML_MAX_CONTENT_LENGTH);
		}
		try {
			bytes = response.body().bytes();
		} catch (IOException e) {
			throw new HttpIoException("", e);
		}
		// 判断是否大于HTML_MAX_CONTENT_LENGTH 如果是抛出 HttpReadDataMoreThanMaxException
		// 异常
		if (bytes.length > HttpConstant.HTML_MAX_CONTENT_LENGTH) {
			throw new HttpReadDataMoreThanMaxException(
					"read bytes length[" + bytes.length + "]more than max:" + HttpConstant.HTML_MAX_CONTENT_LENGTH);
		}
		bytes = HttpDecodingUtils.decodeing(response.header("Content-Encoding"), bytes);
		return bytes;
	}

	public String getHtml(HttpResult result, ContentType type) {
		String charset = result.getCharset();
		if (null == result.getCharset()) {
			charset = AutoCharsetDetectorUtils.instance().getCharset(result.getData(), type);
		} else {
			charset = AutoCharsetDetectorUtils.instance().replacement(charset);
		}
		result.setCharset(charset);
		return getDataAsString(charset, result.getData());
	}

	private static String getDataAsString(String charset, byte[] data) {
		try {
			if ("utf-8".equalsIgnoreCase(charset) || "utf8".equalsIgnoreCase(charset)) {
				// check for UTF-8 BOM
				if (data.length >= 3) {
					if (data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) {
						return new String(data, 3, data.length - 3, charset);
					}
				}
			}
			return new String(data, charset);
		} catch (UnsupportedEncodingException t) {
			throw new RuntimeException(t);
		}
	}

	public HttpUriRequest buildApacheRequest(String url, String referer, HttpMethod method, Map<String, String> headMap,
			PostContentType postContentType, Map<String, Object> paramsMap) {
		return buildApacheRequest(url, referer, method, headMap, postContentType, paramsMap, null);
	}

	public HttpUriRequest buildApacheRequest(String url, String referer, HttpMethod method, Map<String, String> headMap,
			PostContentType postContentType, Map<String, Object> paramsMap, HttpProxy httpProxy) {
		if (StringUtils.isBlank(url)) {
			throw new RuntimeException("the url must not be blank");
		}
		RequestBuilder requestBuilder = null;
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		String contentType = null;
		if (HttpMethod.POST == method) {
			requestBuilder = RequestBuilder.create(HttpMethod.POST.get());
			HttpEntity entity = null;
			if (PostContentType.JSON == postContentType) {
				String json = JsonUtils.toJson(paramsMap);
				json = urlEncoder(json);
				entity = new StringEntity(json, "utf-8");// 解决中文乱码问题
			} else {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				for (String paramKey : paramsMap.keySet()) {
					Object paramValue = paramsMap.get(paramKey);
					String value = paramValue.toString();
					value = urlEncoder(value);
					params.add(new BasicNameValuePair(paramKey, value));
				}
				entity = new UrlEncodedFormEntity(params, Charset.forName("utf-8"));
				contentType = HttpConstant.CONTENT_TYPE_FORM_VALUE;
			}
			requestBuilder.setEntity(entity);
		} else {
			requestBuilder = RequestBuilder.create(HttpMethod.GET.get());
		}
		requestBuilder.setUri(url);
		if (StringUtils.isNotBlank(contentType)) {
			requestBuilder.addHeader(HttpConstant.CONTENT_TYPE, contentType);
		}
		String host = UrlUtils.getHost(url);
		requestBuilder.addHeader(HttpConstant.HOST, host);

		String origin = UrlUtils.getMainUrl(url);
		requestBuilder.addHeader(HttpConstant.ORIGIN, origin);

		String domain = UrlUtils.getDomain(url);
		okhttp3.Cookie cookie = cookiesStore.getCookieByDamainAndKey(domain, HttpConstant.COOKIE_XSRF);
		if (null != cookie) {
			requestBuilder.addHeader(HttpConstant.X_Xsrftoken, cookie.value());
		}
		if (null != headMap) {
			for (String name : headMap.keySet()) {
				requestBuilder.addHeader(name, headMap.get(name));
			}
		}
		if (null != referer) {
			requestBuilder.addHeader(HttpConstant.REFERER, referer);
		}
		String headName = HttpConstant.CONNECTION;
		String headValue = HttpConstant.KEEP_ALIVE;
		if (null != httpProxy) {
			InetSocketAddress sa = new InetSocketAddress(httpProxy.getHost(), httpProxy.getPort());
			HttpHost proxy = new HttpHost(sa.getAddress());
			configBuilder.setProxy(proxy);
			headName = HttpConstant.PROXY_CONNECTION;
			headValue = HttpConstant.KEEP_ALIVE;
		}
		requestBuilder.setConfig(configBuilder.build());
		requestBuilder.addHeader(headName, headValue);
		return requestBuilder.build();
	}

	public HttpResult executeHttpUriRequest(HttpUriRequest httpRequest) throws AbstractHttpException {
		if (null == httpRequest) {
			throw new RuntimeException("httpRequest must not be null.");
		}
		String url = httpRequest.getURI().toString();
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		HttpResult result = null;
		try {
			response = httpClient.execute(httpRequest);
			result = new HttpResult();
			int httpCode = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			result.setCode(httpCode);
			byte[] data = EntityUtils.toByteArray(entity);
			result.setData(data);
			if (httpCode >= 300 && httpCode < 400) {
				Header locationHeader = response.getFirstHeader("Location");
				String redirectUrl = locationHeader != null ? locationHeader.getValue() : null;
				if (StringUtils.isBlank(redirectUrl)) {
					throw new RuntimeException("httpCode >= 300 && httpCode < 400 , but redirectUrl is blank:" + url);
				}
				result.setRedirectedUrl(redirectUrl);
				result.setReferer(url);
			} else if (httpCode != 200) {
				throw new RuntimeException("httpCode[" + httpCode + "]:" + url);
			}
		} catch (ClientProtocolException e) {
			throw new HttpIoException("execute request[" + url + "] err", e);
		} catch (IOException e) {
			throw new HttpIoException("execute request[" + url + "] err", e);
		} finally {
			try {
				EntityUtils.consume(entity);
				if (null != response) {
					response.close();
				}
			} catch (IOException e) {
				LOG.error("close response err:" + url, e);
			}
		}
		return result;
	}

	@PreDestroy
	public void destroy() {
		cookiesStore.close();
		try {
			httpClient.close();
		} catch (IOException e) {
			LOG.error("httpClient close err", e);
		}
	}

	public CookiesStore getCookiesStore() {
		return cookiesStore;
	}

	public void setCookiesStore(CookiesStore cookiesStore) {
		this.cookiesStore = cookiesStore;
	}

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public OkHttpClient getOkClient() {
		return okClient;
	}

	public void setOkClient(OkHttpClient okClient) {
		this.okClient = okClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
		okHttpClientBuilder.sslSocketFactory(MX509TrustManager.getSSLSocketFactory(),
				MX509TrustManager.myX509TrustManager);
		okHttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
		okHttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);
		okHttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
		okHttpClientBuilder.followRedirects(true);
		okHttpClientBuilder.followSslRedirects(true);
		String cookirDir = configure.getSpiderHome() + File.separator + "cookies";
		cookiesStore = new CookiesStore(cookirDir);
		okHttpClientBuilder.cookieJar(cookiesStore);
		okClient = okHttpClientBuilder.build();
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		httpClientBuilder.setDefaultCookieStore(cookiesStore);
		httpClient = HttpClients.createDefault();
	}
}
