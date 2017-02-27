package six.com.crawler.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class UrlUtils {

	private static final Set<String> notNeedDoUrl = new HashSet<>();

	private static final String needDoUrl1 = "/";

	private static final String needDoUrl2 = "../";

	private static final String needDoUrl3 = "./";
	
	private static final char[] unencodedChars = new char[] { '-', '_', '.', '~', '!', '*', '\'', '(', ')', ';', ':',
			'@', '&', '=', '+', '$', ',', '/', '?', '#', '[', ']', '%' };
	
	private static final String PRX_HTTP = "http://";
	private static final String PRX_HTTPS = "https://";

	static {
		notNeedDoUrl.add("https://");
		notNeedDoUrl.add("http://");
	}

	/**
	 * 拼接Url
	 * 
	 * @param mainUrl
	 *            网站主url
	 * @param srcHtmlUrl
	 *            页面url
	 * @param findUrl
	 *            页面抽取出来的url
	 * @return
	 */
	public static String paserUrl(String baseUrl, String srcUrl, String findUrl) {
		ObjectCheckUtils.checkNotNull(findUrl, "findUrl");
		// 如果是http:// 或者https://开头的 直接返回 findUrl
		for (String startHead : notNeedDoUrl) {
			if (findUrl.startsWith(startHead)) {
				return findUrl;
			}
		}
		ObjectCheckUtils.checkNotNull(srcUrl, "srcUrl");
		if (null == baseUrl) {
			baseUrl = getMainUrl(srcUrl);
		}
		String newUrl = findUrl;
		int index = 0;
		int end = 0;
		String head = srcUrl;
		int count = 0;
		// 如果以 /开头 那么 在findUrl 前面加上 mainUrl
		if (findUrl.startsWith(needDoUrl1)) {
			newUrl = baseUrl + findUrl;
			// 如果以 ../ 或者 ../../等开头的处理
		} else if (findUrl.startsWith(needDoUrl2)) {
			while (true) {
				index = findUrl.indexOf(needDoUrl2);
				if (index != -1) {
					index = index + needDoUrl2.length();
					findUrl = findUrl.substring(index, findUrl.length());
					count++;
				} else {
					break;
				}
			}
			// 去掉url 最后 比如 /index.html /index ..
			end = head.lastIndexOf("/");
			head = head.substring(0, end);
			while (count-- > 0) {
				end = head.lastIndexOf("/");
				head = head.substring(0, end);
			}
			newUrl = head.concat("/").concat(findUrl);
		} else if (findUrl.startsWith(needDoUrl3)) {
			end = head.lastIndexOf("/");
			head = head.substring(0, end);
			findUrl = findUrl.replace(needDoUrl3, "/");
			newUrl = head.concat(findUrl);
		}else{
			end = head.lastIndexOf("/");
			head = head.substring(0, end);
			newUrl = head.concat("/").concat(findUrl);
		}
		newUrl = encodeUrl(newUrl, "utf-8");
		return StringUtils.replaceChars(newUrl, '\\', '/');
	}

	public static String encodeIllegalCharacterInUrl(String url) {
		return url.replace(" ", "%20");
	}

	public static String getMainUrl(String url) {
		String host = url;
		int i = StringUtils.ordinalIndexOf(url, "/", 3);
		if (i > 0) {
			host = StringUtils.substring(url, 0, i);
		}
		return host;
	}

	private static Pattern patternForProtocal = Pattern.compile("[\\w]+://");

	public static String removeProtocol(String url) {
		return patternForProtocal.matcher(url).replaceAll("");
	}

	/**
	 * 通过ulr 获取domain 。如果获取不到则返回host作为domain
	 * 
	 * @param url
	 * @param host
	 * @return
	 */
	public static String getDomain(String url) {
		String domain = removeProtocol(url);
		int i = StringUtils.indexOf(domain, "/", 1);
		if (i > 0) {
			domain = StringUtils.substring(domain, 0, i);
		}
		if (null == domain) {
			return getHost(url);
		} else {
			return domain;
		}

	}

	/**
	 * calculate absolute link from relative links found in html
	 * 
	 * @param baseUrl
	 *            base url
	 * @param path
	 * @return
	 */
	public static String calculateLink(String baseUrl, String path, String encoding) {
		try {
			URL pathUrl;
			URL baseURL = new URL(baseUrl);
			if (path != null && path.startsWith("?")) {
				// create the base URL minus query.
				URL base = new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(), baseURL.getPath());
				pathUrl = new URL(base.toString() + path);
			} else {
				// fix bug:if the parameter of path is null,must convert it to
				// ""
				pathUrl = new URL(baseURL, null == path ? "" : path);
			}
			String str = encodeUrl(pathUrl.toString(), encoding);
			return StringUtils.replaceChars(str, '\\', '/');
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encodeUrl(String url, String encoding) {
		if (encoding == null)
			return url;
		url = url.trim();
		StringBuilder newUrlSb = new StringBuilder();
		for (char ch : url.toCharArray()) {
			if (isEncodedChar(ch)) {
				try {
					newUrlSb.append(URLEncoder.encode(String.valueOf(ch), encoding));
				} catch (UnsupportedEncodingException e) {
					newUrlSb.append(ch);
				}
			} else {
				newUrlSb.append(ch);
			}

		}
		return newUrlSb.toString();
	}

	private static boolean isEncodedChar(char ch) {
		if (ch > 127)
			return true;
		if ('A' <= ch && ch <= 'Z')
			return false;
		if ('a' <= ch && ch <= 'z')
			return false;
		if ('0' <= ch && ch <= '9')
			return false;
		for (char unencodedChar : unencodedChars) {
			if (unencodedChar == ch)
				return false;
		}
		return true;
	}

	public static String encode(String str) {
		try {
			return URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String encodeUrl(String url) {
		if (url == null)
			return null;
		return url.replaceAll(" ", "%20");
	}

	/**
	 * 从url中获取host
	 * 
	 * @param url
	 * @return host
	 */
	public static String getHost(String url) {
		int start = 0;
		String host = null;
		if ((start = url.indexOf(PRX_HTTP)) != -1) {
			start += PRX_HTTP.length();
		} else if ((start = url.indexOf(PRX_HTTPS)) != -1) {
			start += PRX_HTTPS.length();
		} else {
			return host;
		}
		url = url.substring(start, url.length());
		int end = 0;
		if ((end = url.indexOf("/")) == -1) {
			end = url.length();
		}
		host = url.substring(0, end);
		return host;
	}

	public static void main(String[] args) {
		String mainUrl = "http://www.cqgtfw.gov.cn";
		String srcUrl = "http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/index.htm";
		String findUrl = "./201609/t20160919_381769.htm";
		String result = "http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/201609/t20160919_381769.htm";
		System.out.println(result.equals(paserUrl(mainUrl, srcUrl, findUrl)));

		mainUrl = "http://download.csdn.net";
		srcUrl = "http://download.csdn.net/download/zh921112/8504337";
		findUrl = "http://download.csdn.net/test/8504337";
		result = "http://download.csdn.net/test/8504337";
		System.out.println(result.equals(paserUrl(mainUrl, srcUrl, findUrl)));

		mainUrl = "http://download.csdn.net";
		srcUrl = "http://download.csdn.net/download/zh921112";
		findUrl = "../test/8504337";
		result = "http://download.csdn.net/test/8504337";
		System.out.println(result.equals(paserUrl(mainUrl, srcUrl, findUrl)));

		mainUrl = "http://download.csdn.net";
		srcUrl = "http://download.csdn.net/download/zh921112/8504337";
		findUrl = "../../test/8504337";
		result = "http://download.csdn.net/test/8504337";
		System.out.println(result.equals(paserUrl(mainUrl, srcUrl, findUrl)));

		mainUrl = "http://download.csdn.net";
		srcUrl = "http://download.csdn.net/download/zh921112/8504337";
		findUrl = "download.csdn.net/download/test/8504337";
		result = "download.csdn.net/download/test/8504337";
		System.out.println(result.equals(paserUrl(mainUrl, srcUrl, findUrl)));

		String host = getMainUrl(srcUrl);
		System.out.println(host);

	}
}
