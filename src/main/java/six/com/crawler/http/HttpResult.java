package six.com.crawler.http;

import java.io.UnsupportedEncodingException;

import okhttp3.Headers;

/**
 * @author six
 * @date 2016年8月17日 下午5:45:43
 */
public class HttpResult {

	private String charset;//
	private Headers headers;
	private String html;
	private byte[] data;

	private String redirectedUrl;
	private int code;
	
	public Headers getHeaders() {
		return headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	private String referer;

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getRedirectedUrl() {
		return redirectedUrl;
	}

	public void setRedirectedUrl(String redirectedUrl) {
		this.redirectedUrl = redirectedUrl;
	}
	

	public String getDataAsString(String charset) {
		if (null == data) {
			return null;
		} else {
			try {
				if ("utf-8".equalsIgnoreCase(charset) || "utf8".equalsIgnoreCase(charset)) {
					// check for UTF-8 BOM
					if (data.length >= 3) {
						if (data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF)
							return new String(data, 3, data.length - 3, charset);
					}
				}
				return new String(data, charset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
