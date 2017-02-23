package six.com.crawler.work.downer;

import java.io.Serializable;

/**
 * @author six
 * @date 2016年7月26日 下午5:03:10
 */
public enum PostContentType implements Serializable{
	JSON("application/json"), FORM("application/form");

	private final String value;

	PostContentType(String value) {
		this.value = value;
	}

	public String get() {
		return value;
	}

	public static PostContentType paser(String type) {
		if ("json".equals(type)) {
			return JSON;
		} else {
			return FORM;
		}
	}
}
