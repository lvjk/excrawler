package six.com.crawler.work.extract;

import java.io.Serializable;

import six.com.crawler.entity.BasePo;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午9:14:46 类说明 页面数据类型
 */

public class ExtractItem extends BasePo implements Serializable {

	private static final long serialVersionUID = 5530674966621099301L;

	private String jobName;// job名字

	private int serialNub;// 抽取顺序

	private String pathName;// 名字

	private int primary;// 是否是主键 0:不是 1是

	private ExtractItemType type;// 类型

	/**
	 * 0：不输出
	 * 1:输出至保存 
	 * 2:输出到生成新page meta中 
	 * 3:url类型生成新page并输出到对了中
	 */
	private int outputType;

	private String outputKey;// 结果 key
	
	private String httpMethod;//httpMethod

	private int mustHaveResult;// 是否必须有值 1 ：必须有值

	private String describe;// 描述

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public int getSerialNub() {
		return serialNub;
	}

	public void setSerialNub(int serialNub) {
		this.serialNub = serialNub;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public int getPrimary() {
		return primary;
	}

	public void setPrimary(int primary) {
		this.primary = primary;
	}

	public int getType() {
		return type.value();
	}

	public void setType(int type) {
		this.type = ExtractItemType.valueOf(type);
	}

	public int getOutputType() {
		return outputType;
	}

	public void setOutputType(int outputType) {
		this.outputType = outputType;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public int getMustHaveResult() {
		return mustHaveResult;
	}

	public void setMustHaveResult(int mustHaveResult) {
		this.mustHaveResult = mustHaveResult;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
