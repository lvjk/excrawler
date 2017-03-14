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

	private int initPool() {
		int poolSize = 0;
		redisManager.lock(siteHttpProxyPoolKey);
		try {
			Map<String, HttpProxy> map = redisManager.hgetAll(HttpProxyPool.REDIS_HTTP_PROXY_POOL, HttpProxy.class);
			if (null != map) {
				for (HttpProxy httpProxy : map.values()) {
					redisManager.lpush(siteHttpProxyPoolKey, httpProxy);
				}
				poolSize = map.size();
			}

		} finally {
			redisManager.unlock(siteHttpProxyPoolKey);
		}
		return poolSize;

	}

	/**
	 * 存在如果同时执行2个一个sitecode任务 先停止一个会导致后续拿不到代理
	 * 
	 * @return
	 */
	public HttpProxy getHttpProxy() {
		HttpProxy httpProxy = null;
		if (httpProxyType == HttpProxyType.ENABLE_ONE || httpProxyType == HttpProxyType.ENABLE_MANY) {
			while (true) {
				int index = getProxyIndex();
				httpProxy = redisManager.lindex(siteHttpProxyPoolKey, index, HttpProxy.class);
				// 如果获取httpProxy==null 那么初始化站点http代理池
				if (null == httpProxy) {
					poolSize = initPool();
					// 如果初始化代理池size>0 那么继续获取，否则抛异常
					if (poolSize > 0) {
						continue;
					} else {
						throw new RuntimeException("there isn't httpProxys in the HttpProxyPool["
								+ HttpProxyPool.REDIS_HTTP_PROXY_POOL + "]");
					}
				}
				long nowTime = System.currentTimeMillis();
				long alreadyRestTime = nowTime - httpProxy.getLastUseTime();
				LOG.info(
						siteHttpProxyPoolKey + "'s index["+index+"] [" + httpProxy.toString() + "] alreadyRestTime:" + alreadyRestTime);
				if (alreadyRestTime >= restTime) {
					httpProxy.setLastUseTime(nowTime);
					redisManager.lset(siteHttpProxyPoolKey, index, httpProxy);
					break;
				}
			
				if (index != -1) {} else {
					throw new RuntimeException("get getProxyIndex:-1");
				}
			}
		}
		return httpProxy;
	}

	private int getProxyIndex() {
		int index =0;
		boolean flag = false;
		try {
			redisManager.lock(siteHttpProxyPoolKey);
			do {
				// 因为 incr 当key不存在时 先设置值=0，然后再自增，所以都是从1开始
				Long tempIndex = redisManager.incr(siteHttpProxyIndexKey);
				index = tempIndex.intValue() - 1;
				if (index >= poolSize&&0!=poolSize) {
					redisManager.del(siteHttpProxyIndexKey);
					flag = true;
				} else {
					break;
				}
			} while (flag);
		} finally {
			redisManager.unlock(siteHttpProxyPoolKey);
		}
		return index;
	}

	public void destroy() {
		redisManager.lock(siteHttpProxyPoolKey);
		try {
			redisManager.del(siteHttpProxyIndexKey);
			redisManager.del(siteHttpProxyPoolKey);
		} finally {
			redisManager.unlock(siteHttpProxyPoolKey);
		}
	}
}
