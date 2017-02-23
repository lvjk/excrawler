package six.com.crawler.work.extract;

import java.io.Serializable;

import six.com.crawler.common.entity.PageType;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午9:14:46 类说明 页面数据类型
 */

public class ExtractItem implements Serializable {

	private static final long serialVersionUID = 5530674966621099301L;
	private String jobName;// job名字
	private String pathName;// 名字
	/**
	 * string字符类型2 1 url类型 2  text 文本类型3 phone 电话类型4 number 数字类型5 date 日期类型6
	 * meta 页面meta 信息7
	 */
	private ExtractItemType type;// 类型
	private String resultKey;// 结果 key
	private PageType pageType;// 页面类型
	private MustHaveResult mustHaveResult;// 是否必须有值
	private String describe;// 描述
	private int output;// 0 不输出 1输出的结果

	public int getOutput() {
		return output;
	}

	public void setOutput(int output) {
		this.output = output;
	}

	public int getType() {
		return type.value();
	}

	public void setType(int type) {
		this.type = ExtractItemType.valueOf(type);
	}

	public String getResultKey() {
		return resultKey;
	}

	public void setResultKey(String resultKey) {
		this.resultKey = resultKey;
	}

	public PageType getPageType() {
		return pageType;
	}

	public void setPageType(int pageType) {
		this.pageType = PageType.valueOf(pageType);
	}

	public MustHaveResult getMustHaveResult() {
		return mustHaveResult;
	}

	public void setMustHaveResult(int mustHaveResult) {
		this.mustHaveResult = MustHaveResult.getMustHaveResult(mustHaveResult);
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("jobName=").append(jobName).append(",");
		sb.append("pathName=").append(pathName).append(",");
		sb.append("type=").append(type).append(",");
		sb.append("pageType=").append(pageType).append(",");
		sb.append("mustHaveResult=").append(mustHaveResult).append(",");
		sb.append("describe=").append(describe);
		return sb.toString();
	}

}
