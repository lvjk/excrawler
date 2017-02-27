package six.com.crawler.common.entity;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月4日 下午10:28:07 类说明 站点 
 */
public class Site implements Serializable {

	private static final long serialVersionUID = 3748779904088935189L;
	/**
	 * 站点代码
	 */
	private String code;
	/**
	 * 站点主页
	 */
	private String mainUrl;
	/**
	 * 站点描述
	 */
	private String describe;

	@XmlAttribute
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement(name = "mainUrl")
	public String getMainUrl() {
		return mainUrl;
	}

	public void setMainUrl(String mainUrl) {
		this.mainUrl = mainUrl;
	}

	@XmlElement(name = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
