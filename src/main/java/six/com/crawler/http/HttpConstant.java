package six.com.crawler.http;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午8:57:30 类说明
 */
public class HttpConstant {

	public final static Map<String, String> headMap = new HashMap<String, String>();
	public final static String REFERER = "Referer";
	public final static String HOST = "Host";
	public final static String CONTENT_TYPE = "Content-Type";
	public final static String CONTENT_TYPE_FORM_VALUE = "application/x-www-form-urlencoded";
	public final static String ORIGIN = "Origin";
	public final static String COOKIE = "Cookie";// ;Content-Type
	public final static String USERAGENT = "User-Agent";// ;Origin
	public final static String COOKIE_XSRF = "_xsrf";// ;
	public final static String X_Xsrftoken = "X-Xsrftoken";
	public final static String COOKIE_SPLIT = ";";
	public final static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
	public final static String LOCAL_ADDRESS = "http.route.local-address";
	public final static String PROXY_CONNECTION = "Proxy-Connection";
	public final static String CONNECTION = "Connection";
	public final static String KEEP_ALIVE = "keep-alive";
	public final static long HTML_MAX_CONTENT_LENGTH = 1024 * 1000 * 1024 * 10;

	public static int REDIRECT_TIMES = 10;

	
	static {
		headMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		headMap.put("Cache-Control", "max-age=0");
		headMap.put("User-Agent", userAgent);
		headMap.put("Upgrade-Insecure-Requests", "1");// User-Agent:
	}

}
