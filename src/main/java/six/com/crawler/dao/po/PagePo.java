package six.com.crawler.dao.po;

import six.com.crawler.entity.BasePo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月17日 上午9:11:12
 * 
 *       网站抓取Page 对应数据 po
 * 
 */
public class PagePo extends BasePo{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6739289067989865883L;
	/** 所属哪个任务产生的pagevo **/
	private String jobName;
	/** 所属哪个任务运行批次产生的pagevo **/
	private String jobSnapshotId;
	/** 站点code **/
	private String siteCode;
	/** 页面唯一key **/
	private String pageKey;
	/** 页面url **/
	private String pageUrl;
	/** 页面源码 **/
	private String pageSrc;
	/** 页面序列化byte **/
	private byte[] data;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobSnapshotId() {
		return jobSnapshotId;
	}

	public void setJobSnapshotId(String jobSnapshotId) {
		this.jobSnapshotId = jobSnapshotId;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}

	public String getPageKey() {
		return pageKey;
	}

	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getPageSrc() {
		return pageSrc;
	}

	public void setPageSrc(String pageSrc) {
		this.pageSrc = pageSrc;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
