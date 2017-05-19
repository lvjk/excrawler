package six.com.crawler.work.downer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.HttpProxyDao;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.HttpProxyType;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月6日 上午9:17:33
 */
public class HttpProxyPool {

	final static Logger log = LoggerFactory.getLogger(HttpProxyPool.class);

	public static final String REDIS_HTTP_PROXY_POOL = "http_proxy_pool";
	public static final String REDIS_HTTP_PROXY_INDEX = "http_proxy_index";
	private HttpProxyType httpProxyType;
	private String siteHttpProxyPoolKey;// 站点http代理池 key
	private String siteHttpProxyIndexKey;// 站点http代理获取索引 key
	private RedisManager redisManager;
	private HttpProxyDao httpProxyDao;
	private DistributedLock distributedLock;
	private int poolSize;
	private long restTime;

	public HttpProxyPool(HttpProxyDao httpProxyDao, RedisManager redisManager, DistributedLock distributedLock,
			String siteCode, HttpProxyType httpProxyType, long restTime) {
		this.httpProxyDao = httpProxyDao;
		this.siteHttpProxyPoolKey = REDIS_HTTP_PROXY_POOL + "_" + siteCode;
		this.siteHttpProxyIndexKey = REDIS_HTTP_PROXY_INDEX + "_" + siteCode;
		this.redisManager = redisManager;
		this.distributedLock = distributedLock;
		this.httpProxyType = httpProxyType;
		this.restTime = restTime;
	}

	/**
	 * 获取一个可用的代理
	 * 
	 * @return
	 */
	public HttpProxy getHttpProxy() {
		HttpProxy httpProxy = null;
		int index = 0;
		Long tempIndex = null;
		while (true) {
			tempIndex = redisManager.incr(siteHttpProxyIndexKey);
			index = tempIndex.intValue() - 1;
			if (index >= poolSize && 0 != poolSize) {
				index = index % poolSize;
			}
			httpProxy = redisManager.lindex(siteHttpProxyPoolKey, index, HttpProxy.class);
			// 如果获取httpProxy==null 那么初始化站点http代理池
			if (null == httpProxy) {
				distributedLock.lock();
				poolSize=redisManager.llen(siteHttpProxyPoolKey);
				if(poolSize<=0){
					int num = 0;
					if (httpProxyType == HttpProxyType.ENABLE_ONE) {
						num = 1;
					} else {
						num = -1;
					}
					try {
						poolSize = initPool(num);
					} catch (Exception e) {
						throw new RuntimeException("init httpProxy pool");
					} finally {
						distributedLock.unLock();
					}
					if (poolSize <= 0) {
						throw new RuntimeException("there is not httpProxys");
					}
				}
			} else {
				long nowTime = System.currentTimeMillis();
				long alreadyRestTime = nowTime - httpProxy.getLastUseTime();
				if (alreadyRestTime >= restTime) {
					httpProxy.setLastUseTime(nowTime);
					redisManager.lset(siteHttpProxyPoolKey, index, httpProxy);
					log.info(siteHttpProxyPoolKey + "'s index[" + index + "] [" + httpProxy.toString()
							+ "] alreadyRestTime:" + alreadyRestTime);
					break;
				}
			}
		}
		return httpProxy;
	}

	private int initPool(int num) {
		int poolSize = 0;
		List<HttpProxy> list = httpProxyDao.getAll();
		if (null != list) {
			int count = 0;
			for (HttpProxy httpProxy : list) {
				redisManager.lpush(siteHttpProxyPoolKey, httpProxy);
				count++;
				if (count == num) {
					break;
				}
			}
			poolSize = list.size();
		}
		return poolSize;
	}

	public void destroy() {
		distributedLock.lock();
		try {
			redisManager.del(siteHttpProxyIndexKey);
			redisManager.del(siteHttpProxyPoolKey);
		} finally {
			distributedLock.unLock();
		}
	}
}
