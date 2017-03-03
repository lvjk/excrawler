package six.com.crawler.common.service.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.service.HttpPorxyService;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author six
 * @date 2016年8月26日 下午2:25:19 代理池 和本地ip 管理
 */
@Service
public class HttpPorxyServiceImpl implements HttpPorxyService {

	final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceImpl.class);

	private static final String REDIS_HTTP_PROXY_INDEX = "http_proxy_index";
	private static final String REDIS_HTTP_PROXY_POOL = "http_proxy_pool";

	@Autowired
	private AbstractSchedulerManager schedulerManager;

	private static List<InetAddress> localAddressList = new ArrayList<InetAddress>();

	static {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface networkinterface = en.nextElement();
				if ("lo".equalsIgnoreCase(networkinterface.getName())) {
					// 排除 回坏地址
					continue;
				}
				Enumeration<InetAddress> addresses = networkinterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress localIP = addresses.nextElement();
					String localIpStr = localIP.getHostAddress();
					Pattern p = Pattern.compile("\\d+\\.{1}\\d+\\.{1}\\d+\\.{1}\\d+");
					Matcher m = p.matcher(localIpStr);
					boolean b = m.matches();
					if (b && (!"127.0.0.1".equals(localIpStr))) {
						localAddressList.add(localIP);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("loadLocalIps error!", e);
		}
	}

	public HttpProxy getHttpProxy(String siteCode, int type, long restTime) {
		HttpProxy httpProxy = null;
		if (2 == type) {
			httpProxy = schedulerManager.getRedisManager().get(REDIS_HTTP_PROXY_POOL + "_2", HttpProxy.class);
		} else {
			// 站点http代理池 key
			String siteHttpProxyPoolKey = REDIS_HTTP_PROXY_POOL + "_" + siteCode;
			// 站点http代理获取索引 key
			String siteHttpProxyIndexKey = REDIS_HTTP_PROXY_INDEX + "_" + siteCode;
			while (true) {
				int index = getProxyIndex(siteHttpProxyPoolKey, siteHttpProxyIndexKey);
				if (index != -1) {
					httpProxy = schedulerManager.getRedisManager().lindex(REDIS_HTTP_PROXY_POOL, index,
							HttpProxy.class);
					long nowTime = System.currentTimeMillis();
					long alreadyRestTime = nowTime - httpProxy.getLastUseTime();
					LOG.info(
							siteHttpProxyPoolKey + "[" + httpProxy.toString() + "] alreadyRestTime:" + alreadyRestTime);
					if (alreadyRestTime >= restTime) {
						httpProxy.setLastUseTime(nowTime);
						schedulerManager.getRedisManager().lset(REDIS_HTTP_PROXY_POOL, index, httpProxy);
						break;
					}
				} else {
					throw new RuntimeException("get getProxyIndex:-1");
				}
			}
		}
		return httpProxy;
	}

	private int getProxyIndex(String siteHttpProxyPoolKey, String siteHttpProxyIndexKey) {
		int index = -1;
		boolean flag = false;
		schedulerManager.getRedisManager().lock(siteHttpProxyPoolKey);
		int size = schedulerManager.getRedisManager().llen(siteHttpProxyPoolKey);
		if (0 == size) {
			List<HttpProxy> list = schedulerManager.getRedisManager().lrange(REDIS_HTTP_PROXY_POOL, 0, -1,
					HttpProxy.class);
			for (HttpProxy httpProxy : list) {
				schedulerManager.getRedisManager().lpush(siteHttpProxyPoolKey, httpProxy);
			}
			int expire=1 * 60 * 60;
			schedulerManager.getRedisManager().expire(siteHttpProxyIndexKey, expire);
			size = list.size();
		}
		if (size > 0) {
			try {
				do {
					// 因为 incr 当key不存在时 先设置值=0，然后再自增，所以都是从1开始
					Long tempIndex = schedulerManager.getRedisManager().incr(siteHttpProxyIndexKey);
					// 设置10分钟过期时间
					int expire=1 * 60 * 10;
					schedulerManager.getRedisManager().expire(siteHttpProxyIndexKey, expire);
					index = tempIndex.intValue() - 1;
					if (index >= size) {
						schedulerManager.getRedisManager().del(siteHttpProxyIndexKey);
						flag = true;
					} else {
						break;
					}
				} while (flag);
			} finally {
				schedulerManager.getRedisManager().unlock(siteHttpProxyPoolKey);
			}
		}
		return index;
	}

	public static InetAddress getLocalAddress() {
		return null;
	}

	@Override
	public List<HttpProxy> getHttpProxys() {
		List<HttpProxy> result = schedulerManager.getRedisManager().lrange(REDIS_HTTP_PROXY_POOL, 0, -1,
				HttpProxy.class);
		return result;
	}

	@Override
	public String addHttpProxy(HttpProxy httpProxy) {
		if (schedulerManager.getHttpClient().isValidHttpProxy(httpProxy)) {
			schedulerManager.getRedisManager().lock(REDIS_HTTP_PROXY_POOL);
			try {
				if (httpProxy.getType() == 2) {
					schedulerManager.getRedisManager().del(REDIS_HTTP_PROXY_POOL + "_2");
					schedulerManager.getRedisManager().set(REDIS_HTTP_PROXY_POOL + "_2", httpProxy);
				} else {
					schedulerManager.getRedisManager().lrem(REDIS_HTTP_PROXY_POOL, 0, httpProxy);
					schedulerManager.getRedisManager().lpush(REDIS_HTTP_PROXY_POOL, httpProxy);
				}
				return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] add succeed";
			} finally {
				schedulerManager.getRedisManager().unlock(REDIS_HTTP_PROXY_POOL);
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
		schedulerManager.getRedisManager().lock(REDIS_HTTP_PROXY_POOL);
		try {
			schedulerManager.getRedisManager().lrem(REDIS_HTTP_PROXY_POOL, 0, httpProxy);
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] del succeed";
		} finally {
			schedulerManager.getRedisManager().unlock(REDIS_HTTP_PROXY_POOL);
		}
	}

	public AbstractSchedulerManager getSchedulerManager() {
		return schedulerManager;
	}

	public void setSchedulerManager(AbstractSchedulerManager schedulerManager) {
		this.schedulerManager = schedulerManager;
	}
}
