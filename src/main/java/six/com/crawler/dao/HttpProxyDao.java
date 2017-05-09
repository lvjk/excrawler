package six.com.crawler.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.node.ClusterManager;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 下午4:10:46
 */
@Repository
public class HttpProxyDao implements InitializingBean {

	final static Logger LOG = LoggerFactory.getLogger(HttpProxyDao.class);

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private ClusterManager clusterManager;

	private DistributedLock distributedLock;

	public List<HttpProxy> getHttpProxys() {
		List<HttpProxy> result = new ArrayList<HttpProxy>();
		Map<String, HttpProxy> map = redisManager.hgetAll(HttpProxyPool.REDIS_HTTP_PROXY_POOL, HttpProxy.class);
		if (null != map) {
			result.addAll(map.values());
		}
		return result;
	}

	public boolean save(HttpProxy httpProxy) {
		distributedLock.lock();
		try {
			redisManager.hset(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString(), httpProxy);
			return true;
		} finally {
			distributedLock.unLock();
		}
	}

	public boolean del(HttpProxy httpProxy) {
		distributedLock.lock();
		try {
			redisManager.hdel(HttpProxyPool.REDIS_HTTP_PROXY_POOL, httpProxy.toString());
			return true;
		} finally {
			distributedLock.unLock();
		}
	}

	public void delAllHttpProxy() {
		distributedLock.lock();
		try {
			redisManager.del(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
		} finally {
			distributedLock.unLock();
		}
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		distributedLock = clusterManager.getWriteLock(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
	}

}
