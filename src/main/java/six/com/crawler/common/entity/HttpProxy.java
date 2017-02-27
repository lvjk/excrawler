package six.com.crawler.common.entity;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午4:43:41
 */
public class HttpProxy implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3569393980465582761L;

	private String host;// 代理主机ip
	private int port;// 代理主机端口

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object object) {
		HttpProxy target = (HttpProxy) object;
		return host.equals(target.getHost()) && port == target.getPort();
	}

	public String toString() {
		return host + ":" + port;
	}
}
