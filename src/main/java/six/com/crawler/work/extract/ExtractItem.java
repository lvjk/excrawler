package six.com.crawler.work.extract;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午9:14:46 类说明 页面数据类型
 */

public class ExtractItem implements Serializable {

	private static final long serialVersionUID = 5530674966621099301L;
	
	private String jobName;// job名字
	
	private int serialNub;//抽取顺序
	
	private String pathName;// 名字
	
	private int primary;//是否是主键 0:不是 1是
	
	private ExtractItemType type;// 类型
	
	private int outputType;
	
	private String outputKey;// 结果 key
	
	private MustHaveResult mustHaveResult;// 是否必须有值
	
	private String describe;// 描述
	
	@XmlAttribute
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	@XmlAttribute
	public int getSerialNub() {
		return serialNub;
	}

	public void setSerialNub(int serialNub) {
		this.serialNub = serialNub;
	}
	
	@XmlAttribute
	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	@XmlAttribute
	public int getPrimary() {
		return primary;
	}

	public void setPrimary(int primary) {
		this.primary = primary;
	}
	
	@XmlAttribute
	public int getType() {
		return type.value();
	}

	public void setType(int type) {
		this.type = ExtractItemType.valueOf(type);
	}

	@XmlAttribute
	public int getOutputType() {
		return outputType;
	}

	public void setOutputType(int outputType) {
		this.outputType = outputType;
	}
	

	@XmlAttribute
	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public MustHaveResult getMustHaveResult() {
		return mustHaveResult;
	}

	@XmlAttribute
	public void setMustHaveResult(int mustHaveResult) {
		this.mustHaveResult = MustHaveResult.getMustHaveResult(mustHaveResult);
	}

	@XmlElement(name = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
