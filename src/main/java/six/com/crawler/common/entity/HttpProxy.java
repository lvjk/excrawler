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
	private int type;//1.自己独立ip代理   2.阿布代理
	private String userName;// 代理账户
	private String password;// 代理密码
	private long lastUseTime;//上一次试用时间

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
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public long getLastUseTime() {
		return lastUseTime;
	}

	public void setLastUseTime(long lastUseTime) {
		this.lastUseTime = lastUseTime;
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
