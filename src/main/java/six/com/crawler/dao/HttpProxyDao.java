package six.com.crawler.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.http.HttpProxyPool;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 下午4:10:46
 */
@Repository
public class HttpProxyDao {

	final static Logger LOG = LoggerFactory.getLogger(HttpProxyDao.class);

	@Autowired
	private RedisManager redisManager;

	public List<HttpProxy> getHttpProxys() {
		List<HttpProxy> result = new ArrayList<HttpProxy>();
		Map<String, HttpProxy> map = redisManager.hgetAll(HttpProxyPool.REDIS_HTTP_PROXY_POOL, HttpProxy.class);
		if (null != map) {
			result.addAll(map.values());
		}
		return result;
	}

	public boolean save(HttpProxy httpProxy) {
		redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		try {
			redisManager.hset(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString(), httpProxy);
			return true;
		} finally {
			redisManager.unlock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		}
	}


	public boolean del(HttpProxy httpProxy) {
		redisManager.lock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		try {
			redisManager.hdel(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString());
			return true;
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


	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

}
