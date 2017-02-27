package six.com.crawler.common.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月19日 下午8:35:28 类说明 爬虫job
 */
public class Job implements Serializable {

	private static final long serialVersionUID = 781122651512874550L;

	private String name;// job 名字

	private String hostNode;// job所属哪个节点名字

	private int level;// 任务级别

	private long workFrequency = 1000;// 每次处理时间的阈值 默认1000毫秒

	private int isScheduled;// 0不调度 1调度 是否开启调度

	private int needNodes;// 执行此任务需要的节点数

	private String cronTrigger;// CronTrigger 时间

	private String workerClass;// worker class
	
	private String queueName;

	private String describe;// 任务描述
	
	private String user = "admin";// 任务 所属用户

	private List<JobParam> paramList;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getHostNode() {
		return hostNode;
	}

	public void setHostNode(String hostNode) {
		this.hostNode = hostNode;
	}

	@XmlAttribute
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@XmlAttribute
	public long getWorkFrequency() {
		return workFrequency;
	}

	public void setWorkFrequency(long workFrequency) {
		this.workFrequency = workFrequency;
	}

	@XmlAttribute
	public int getIsScheduled() {
		return isScheduled;
	}

	public void setIsScheduled(int isScheduled) {
		this.isScheduled = isScheduled;
	}

	@XmlAttribute
	public int getNeedNodes() {
		return needNodes;
	}

	public void setNeedNodes(int needNodes) {
		this.needNodes = needNodes;
	}

	@XmlAttribute
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	@XmlElement(name = "queueName")
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	@XmlElement(name = "cronTrigger")
	public String getCronTrigger() {
		return cronTrigger;
	}

	public void setCronTrigger(String cronTrigger) {
		this.cronTrigger = cronTrigger;
	}

	@XmlElement(name = "workerClass")
	public String getWorkerClass() {
		return workerClass;
	}

	public void setWorkerClass(String workerClass) {
		this.workerClass = workerClass;
	}

	@XmlElement(name = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@XmlElement(name = "param")
	public List<JobParam> getParamList() {
		return paramList;
	}

	public void setParamList(List<JobParam> paramList) {
		this.paramList = paramList;
	}

	public String getParam(String paramKey) {
		if (StringUtils.isBlank(paramKey)) {
			throw new NullPointerException("paramKey mustn't be blank");
		}
		String param = null;
		if (null != paramList) {
			for (int i = 0; i < paramList.size(); i++) {
				JobParam jobParam = paramList.get(i);
				if (paramKey.equals(jobParam.getName())) {
					param = jobParam.getValue();
					break;
				}
			}
		}
		return param;
	}

	public List<String> getParams(String paramKey) {
		if (StringUtils.isBlank(paramKey)) {
			throw new NullPointerException("paramKey mustn't be blank");
		}
		List<String> resultParams = new ArrayList<>();
		if (null != paramList) {
			for (int i = 0; i < paramList.size(); i++) {
				JobParam jobParam = paramList.get(i);
				if (paramKey.equals(jobParam.getName())) {
					resultParams.add(jobParam.getValue());
				}
			}
		}
		return resultParams;
	}

}
