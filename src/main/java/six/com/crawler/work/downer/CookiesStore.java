package six.com.crawler.work.downer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import six.com.crawler.common.utils.JavaSerializeUtils;
import six.com.crawler.common.utils.MD5Utils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.configure.SpiderConfigure;

/**
 * @author six
 * @date 2016年7月18日 上午10:54:27 cookie 管理
 */
public class CookiesStore implements CookieJar,CookieStore {

	protected final static Logger LOG = LoggerFactory.getLogger(CookiesStore.class);

	private Map<String, Map<String, Cookie>> cookiesMap = new ConcurrentHashMap<String, Map<String, Cookie>>();

	private String cookieDir;

	public CookiesStore(String cookieDir) {
		this.cookieDir = cookieDir;// configure.getSpiderHome()+ File.separator
									// + "cookies";
	}

	@SuppressWarnings("unchecked")
	private synchronized ConcurrentHashMap<String, Cookie> init(String domain) {
		ConcurrentHashMap<String, Cookie> cookieMap = new ConcurrentHashMap<String, Cookie>();
		File cookiesFile = getFile(domain);
		if (cookiesFile.exists()) {
			byte[] dts = null;
			try {
				dts = FileUtils.readFileToByteArray(cookiesFile);
			} catch (IOException e) {
				LOG.error("FileUtils readFileToByteArray err:" + cookiesFile, e);
			}
			if (null != dts && dts.length > 0) {
				try {
					List<ProxyCookie> list = JavaSerializeUtils.unSerialize(dts, List.class);
					if (null != list) {
						for (ProxyCookie pCk : list) {
							cookieMap.put(pCk.name, pCk.newCookie());
						}
					}
				} catch (Exception e) {
					LOG.error("JavaSerializeUtils unSerialize err:" + cookiesFile, e);
				}
			}

		}
		return cookieMap;
	}

	/**
	 * 通过domain 获取cookie 文件
	 * 
	 * @param domain
	 * @return
	 */
	private File getFile(String domain) {
		String fileName = MD5Utils.MD5(domain);
		String cookieStorePath = cookieDir + File.separator + fileName;
		File cookiesFile = new File(cookieStorePath);
		return cookiesFile;
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		String domain = UrlUtils.getDomain(url.toString());
		Map<String, Cookie> map = getCookieMap(domain);
		for (Cookie ck : cookies) {
			map.put(ck.name(), ck);
		}
	}

	/**
	 * 通过domain 保存cookies
	 * 
	 * @param domain
	 * @param cookies
	 */
	public void saveFromList(String domain, List<Cookie> cookies) {
		Map<String, Cookie> map = getCookieMap(domain);
		for (Cookie ck : cookies) {
			map.put(ck.name(), ck);
		}
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		String domain = UrlUtils.getDomain(url.toString());
		Map<String, Cookie> map = getCookieMap(domain);
		checkExpired(map);
		return new ArrayList<>(map.values());
	}

	/**
	 * 通过domain 加载cookie
	 * 
	 * @param domain
	 * @return
	 */
	public List<Cookie> loadForDomain(String domain) {
		Map<String, Cookie> map = getCookieMap(domain);
		checkExpired(map);
		return new ArrayList<>(map.values());
	}

	/**
	 * 通过domain 和 key 获取cookie
	 * 
	 * @param domain
	 * @param key
	 * @return
	 */
	public Cookie getCookieByDamainAndKey(String domain, String key) {
		Map<String, Cookie> map = getCookieMap(domain);
		return map.get(key);
	}

	/**
	 * 通过domain获取cookie 并检查过期cookie
	 * 
	 * @param url
	 * @return
	 */
	private Map<String, Cookie> getCookieMap(final String domain) {
		Map<String, Cookie> map = cookiesMap.computeIfAbsent(domain, key -> init(domain));
		return map;
	}

	/**
	 * 检查过期cookie并删除 锁的粒度最小为map
	 * 
	 * @param map
	 */
	private void checkExpired(final Map<String, Cookie> map) {
		synchronized (map) {
			try (Stream<Entry<String, Cookie>> stream = map.entrySet().stream();) {
				final long now = System.currentTimeMillis();
				stream.filter(entry -> entry.getValue().expiresAt() <= now).map(entry -> map.remove(entry.getKey()))
						.count();
			}
		}
	}

	/**
	 * 关闭指定 domain cookie 内存，并持久化
	 * 
	 * @param domain
	 */
	public void close(String domain) {
		Map<String, Cookie> cookieMap = getCookieMap(domain);
		ProxyCookie proxyCookie = null;
		List<ProxyCookie> list = new ArrayList<>();
		for (Cookie ck : cookieMap.values()) {
			proxyCookie = ProxyCookie.newProxyCookie(ck);
			list.add(proxyCookie);
		}
		byte[] dts = JavaSerializeUtils.serialize(list);
		if (null != dts) {
			try {
				FileUtils.writeByteArrayToFile(getFile(domain), dts);
			} catch (IOException e) {
				LOG.error("FileUtils writeByteArrayToFile err:" + domain, e);
			}
		}
		cookiesMap.remove(domain);
	}

	/**
	 * 关闭指定 cookie 内存，并持久化
	 * 
	 * @param domain
	 */
	public void close() {
		for (String domain : cookiesMap.keySet()) {
			Map<String, Cookie> cookieMap = cookiesMap.get(domain);
			ProxyCookie proxyCookie = null;
			List<ProxyCookie> list = new ArrayList<>();
			for (Cookie ck : cookieMap.values()) {
				proxyCookie = ProxyCookie.newProxyCookie(ck);
				list.add(proxyCookie);
			}
			byte[] dts = JavaSerializeUtils.serialize(list);
			if (null != dts) {
				try {
					FileUtils.writeByteArrayToFile(getFile(domain), dts);
				} catch (IOException e) {
					LOG.error("FileUtils writeByteArrayToFile err:" + domain, e);
				}
			}
		}
	}

	/**
	 * 用来处理okhttp cookie 序列化问题
	 * 
	 * @author wisers
	 *
	 */
	public static class ProxyCookie implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3466716754657620157L;
		private String name;
		private String value;
		private long expiresAt;
		private String domain;
		private String path;
		private boolean secure;
		private boolean httpOnly;
		private boolean persistent;
		private boolean hostOnly;

		public static ProxyCookie newProxyCookie(Cookie ck) {
			ProxyCookie proxyCookie = new ProxyCookie();
			proxyCookie.setDomain(ck.domain());
			proxyCookie.setName(ck.name());
			proxyCookie.setValue(ck.value());
			proxyCookie.setExpiresAt(ck.expiresAt());
			proxyCookie.setHostOnly(ck.hostOnly());
			proxyCookie.setHttpOnly(ck.httpOnly());
			proxyCookie.setPath(ck.path());
			proxyCookie.setPersistent(ck.persistent());
			return proxyCookie;
		}

		public Cookie newCookie() {
			Cookie.Builder cookieBuilder = new Cookie.Builder();
			cookieBuilder.domain(domain);
			cookieBuilder.name(name);
			cookieBuilder.value(value);
			cookieBuilder.path(path);
			cookieBuilder.expiresAt(expiresAt);
			cookieBuilder.hostOnlyDomain(domain);
			return cookieBuilder.build();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public long getExpiresAt() {
			return expiresAt;
		}

		public void setExpiresAt(long expiresAt) {
			this.expiresAt = expiresAt;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isSecure() {
			return secure;
		}

		public void setSecure(boolean secure) {
			this.secure = secure;
		}

		public boolean isHttpOnly() {
			return httpOnly;
		}

		public void setHttpOnly(boolean httpOnly) {
			this.httpOnly = httpOnly;
		}

		public boolean isPersistent() {
			return persistent;
		}

		public void setPersistent(boolean persistent) {
			this.persistent = persistent;
		}

		public boolean isHostOnly() {
			return hostOnly;
		}

		public void setHostOnly(boolean hostOnly) {
			this.hostOnly = hostOnly;
		}

	}

	@Autowired
	private SpiderConfigure configure;

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	@Override
	public void addCookie(org.apache.http.cookie.Cookie cookie) {
		
	}

	@Override
	public List<org.apache.http.cookie.Cookie> getCookies() {
		return null;
	}

	@Override
	public boolean clearExpired(Date date) {
		return false;
	}

	@Override
	public void clear() {
		
	}
}
