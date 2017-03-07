package six.com.crawler.common.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.HttpProxyType;
import six.com.crawler.common.http.HttpProxyPool;
import six.com.crawler.common.service.HttpPorxyService;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.common.RedisManager;

/**
 * @author six
 * @date 2016年8月26日 下午2:25:19 代理池 和本地ip 管理
 */
@Service
public class HttpPorxyServiceImpl implements HttpPorxyService {

	final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceImpl.class);


	@Autowired
	private AbstractSchedulerManager schedulerManager;

	@Autowired
	private RedisManager redisManager;

	public HttpProxyPool buildHttpProxyPool(String siteCode,HttpProxyType httpProxyType,long restTime) {
		HttpProxyPool httpProxyPool = new HttpProxyPool(redisManager,siteCode,httpProxyType,restTime);
		return httpProxyPool;
	}


	@Override
	public List<HttpProxy> getHttpProxys() {
		List<HttpProxy> result = redisManager.lrange(HttpProxyPool.REDIS_HTTP_PROXY_POOL, 0, -1, HttpProxy.class);
		return result;
	}

	@Override
	public String addHttpProxy(HttpProxy httpProxy) {
		if (schedulerManager.getHttpClient().isValidHttpProxy(httpProxy)) {
			redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
			try {
				if (httpProxy.getType() == 2) {
					redisManager.del(HttpProxyPool.REDIS_HTTP_PROXY_POOL + "_2");
					redisManager.set(HttpProxyPool.REDIS_HTTP_PROXY_POOL + "_2", httpProxy);
				} else {
					redisManager.lrem(HttpProxyPool.REDIS_HTTP_PROXY_POOL, 0, httpProxy);
					redisManager.lpush(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy);
				}
				return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] add succeed";
			} finally {
				redisManager.unlock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
			}
		} else {
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] is invalid";
		}
	}

	@Override
	public String testHttpProxy(HttpProxy httpProxy) {
		if (schedulerManager.getHttpClient().isValidHttpProxy(httpProxy)) {
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] is valid";
		} else {
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] is invalid";
		}
	}

	@Override
	public String delHttpProxy(HttpProxy httpProxy) {
		redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		try {
			redisManager.lrem(HttpProxyPool.REDIS_HTTP_PROXY_POOL, 0, httpProxy);
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] del succeed";
		} finally {
			redisManager.unlock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		}
	}

	public AbstractSchedulerManager getSchedulerManager() {
		return schedulerManager;
	}

	public void setSchedulerManager(AbstractSchedulerManager schedulerManager) {
		this.schedulerManager = schedulerManager;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}
}
