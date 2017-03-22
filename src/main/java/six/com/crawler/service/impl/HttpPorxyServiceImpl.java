package six.com.crawler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.http.HttpClient;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.service.HttpPorxyService;
import six.com.crawler.common.RedisManager;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.HttpProxyType;

/**
 * @author six
 * @date 2016年8月26日 下午2:25:19 代理池 和本地ip 管理
 */
@Service
public class HttpPorxyServiceImpl implements HttpPorxyService {

	final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceImpl.class);

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private RedisManager redisManager;

	public HttpProxyPool buildHttpProxyPool(String siteCode, HttpProxyType httpProxyType, long restTime) {
		HttpProxyPool httpProxyPool = new HttpProxyPool(redisManager, siteCode, httpProxyType, restTime);
		return httpProxyPool;
	}

	@Override
	public List<HttpProxy> getHttpProxys() {
		List<HttpProxy> result = new ArrayList<HttpProxy>();
		Map<String, HttpProxy> map = redisManager.hgetAll(HttpProxyPool.REDIS_HTTP_PROXY_POOL, HttpProxy.class);
		if (null != map) {
			result.addAll(map.values());
		}
		return result;
	}

	@Override
	public String addHttpProxy(HttpProxy httpProxy) {
		if (getHttpClient().isValidHttpProxy(httpProxy)) {
			redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
			try {
				redisManager.hset(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString(), httpProxy);
				return "this httpProxy[" + httpProxy.toString() + "] add succeed";
			} finally {
				redisManager.unlock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
			}
		} else {
			return "this httpProxy[" + httpProxy.toString() + "] is invalid";
		}
	}

	@Override
	public String testHttpProxy(HttpProxy httpProxy) {
		HttpProxy getHttpProxy = redisManager.hget(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString(),
				HttpProxy.class);
		if (getHttpClient().isValidHttpProxy(getHttpProxy)) {
			return "this httpProxy[" + getHttpProxy.toString() + "] is valid";
		} else {
			return "this httpProxy[" + getHttpProxy.toString() + "] is invalid";
		}
	}

	@Override
	public String delHttpProxy(HttpProxy httpProxy) {
		redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		try {
			redisManager.hdel(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString());
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] del succeed";
		} finally {
			redisManager.unlock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		}
	}

	public void delAllHttpProxy() {
		redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		try {
			redisManager.del(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		} finally {
			redisManager.unlock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		}
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}
}
