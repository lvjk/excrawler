package six.com.crawler.common.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.HttpProxyType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月6日 上午9:17:33
 */
public class HttpProxyPool {
	final static Logger LOG = LoggerFactory.getLogger(HttpProxyPool.class);

	public static final String REDIS_HTTP_PROXY_INDEX = "http_proxy_index";
	public static final String REDIS_HTTP_PROXY_POOL = "http_proxy_pool";
	private HttpProxyType httpProxyType;
	private String siteHttpProxyPoolKey;// 站点http代理池 key
	private String siteHttpProxyIndexKey;// 站点http代理获取索引 key
	private RedisManager redisManager;
	private int poolSize;
	private long restTime;

	public HttpProxyPool(RedisManager redisManager, String siteCode, HttpProxyType httpProxyType, long restTime) {
		this.redisManager = redisManager;
		this.httpProxyType = httpProxyType;
		this.siteHttpProxyPoolKey = REDIS_HTTP_PROXY_POOL + "_" + siteCode;
		this.siteHttpProxyIndexKey = REDIS_HTTP_PROXY_INDEX + "_" + siteCode;
		this.restTime = restTime;
	}

	private void initPool() {
		if(0==poolSize){
			redisManager.lock(siteHttpProxyPoolKey);
			try {
				poolSize = redisManager.llen(siteHttpProxyPoolKey);
				if (0 == poolSize) {
					Map<String, HttpProxy> map = redisManager.hgetAll(HttpProxyPool.REDIS_HTTP_PROXY_POOL, HttpProxy.class);
					if (null != map) {
						for (HttpProxy httpProxy : map.values()) {
							redisManager.lpush(siteHttpProxyPoolKey, httpProxy);
						}
						poolSize = map.size();
					}
				}
			} finally {
				redisManager.unlock(siteHttpProxyPoolKey);
			}	
		}
	}

	public HttpProxy getHttpProxy() {
		initPool();
		HttpProxy httpProxy = null;
		if (httpProxyType == HttpProxyType.ENABLE_ONE || httpProxyType == HttpProxyType.ENABLE_MANY) {
			while (true) {
				int index = getProxyIndex();
				if (index != -1) {
					httpProxy = redisManager.lindex(siteHttpProxyPoolKey, index, HttpProxy.class);
					long nowTime = System.currentTimeMillis();
					long alreadyRestTime = nowTime - httpProxy.getLastUseTime();
					LOG.info(
							siteHttpProxyPoolKey + "[" + httpProxy.toString() + "] alreadyRestTime:" + alreadyRestTime);
					if (alreadyRestTime >= restTime) {
						httpProxy.setLastUseTime(nowTime);
						redisManager.lset(siteHttpProxyPoolKey, index, httpProxy);
						break;
					}
				} else {
					throw new RuntimeException("get getProxyIndex:-1");
				}
			}
		} else if (httpProxyType == HttpProxyType.ENABLE_ABU) {
			httpProxy = redisManager.get(REDIS_HTTP_PROXY_POOL + "_2", HttpProxy.class);
		}
		return httpProxy;
	}

	private int getProxyIndex() {
		int index = -1;
		boolean flag = false;
		if (poolSize > 0) {
			try {
				redisManager.lock(siteHttpProxyPoolKey);
				do {
					// 因为 incr 当key不存在时 先设置值=0，然后再自增，所以都是从1开始
					Long tempIndex = redisManager.incr(siteHttpProxyIndexKey);
					index = tempIndex.intValue() - 1;
					if (index >= poolSize) {
						redisManager.del(siteHttpProxyIndexKey);
						flag = true;
					} else {
						break;
					}
				} while (flag);
			} finally {
				redisManager.unlock(siteHttpProxyPoolKey);
			}
		}
		return index;
	}
	
	public void destroy(){
		redisManager.lock(siteHttpProxyPoolKey);
		try {		
			redisManager.del(siteHttpProxyIndexKey);
			redisManager.del(siteHttpProxyPoolKey);
		} finally {
			redisManager.unlock(siteHttpProxyPoolKey);
		}
	}
}
