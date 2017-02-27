package six.com.crawler.common.http;

import java.io.Serializable;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午9:23:11 类说明
 */
public enum HttpMethod implements Serializable{
	GET("get"), POST("post");

	public final String value;

	HttpMethod(String value) {
		this.value = value;
	}

	public String get() {
		return value;
	}
}
