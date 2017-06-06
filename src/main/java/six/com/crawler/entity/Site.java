package six.com.crawler.entity;

import java.io.Serializable;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月4日 下午10:28:07 类说明 站点
 */
public class Site extends BasePo implements Serializable {

	private static final long serialVersionUID = 3748779904088935189L;

	// 默认站点最小访问频率5 秒
	public static final int DEFAULT_MIN_VISIT_FREQUENRY = 3000;
	/**
	 * 站点代码
	 */
	private String code = "";
	/**
	 * 站点主页
	 */
	private String mainUrl = "";

	/**
	 * 站点访问频率
	 */
	private long visitFrequency = DEFAULT_MIN_VISIT_FREQUENRY;

	/**
	 * 站点描述
	 */
	private String describe = "";

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

	public long getVisitFrequency() {
		return visitFrequency;
	}

	public void setVisitFrequency(long visitFrequency) {
		this.visitFrequency = visitFrequency;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
