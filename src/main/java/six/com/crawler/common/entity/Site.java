package six.com.crawler.common.entity;

import java.io.Serializable;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月4日 下午10:28:07 类说明 站点 vo
 */
public class Site implements Serializable {

	private static final long serialVersionUID = 3748779904088935189L;
	private String code;// 站点代码
	private String mainUrl;// url
	private int proxy_enable;// 是否启用代理 0 启用 1不启用
	private int localAddress_enable;// 是否使用本地ip
	private String describe;// 站点描述
	private int downerType;//站点的下载类型 1：http 2js 3 2个都要

	public int getDownerType() {
		return downerType;
	}

	public void setDownerType(int downerType) {
		this.downerType = downerType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMainUrl() {
		return mainUrl;
	}

	public void setMainUrl(String mainUrl) {
		this.mainUrl = mainUrl;
	}

	public boolean isProxy_enable() {
		return proxy_enable==1;
	}

	public void setProxy_enable(int proxy_enable) {
		this.proxy_enable = proxy_enable;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public boolean isLocalAddress_enable() {
		return localAddress_enable==1;
	}

	public void setLocalAddress_enable(int localAddress_enable) {
		this.localAddress_enable = localAddress_enable;
	}

}
