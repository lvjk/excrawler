package six.com.crawler.entity;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 下午3:59:52
 * 
 *       任务链可以包含多个任务或者一个任务，它是作为一个集群调度对象,具体调度后做什么，由job负责，任务间具备 先后关系
 *       任务链下的所有任务共享当前任务链状态
 * 
 */
public class JobChain extends BasePo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3775150562953071503L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 级别
	 */
	private int level;

	/**
	 * 是否开启定时
	 */
	private int isScheduled;

	/**
	 * cronTrigger 定时
	 */
	private String cronTrigger = "";

	/**
	 * 描述
	 */
	private String describe = "";

	/**
	 * 所属用户
	 */
	private String user = "admin";

	/**
	 * 版本号
	 */
	private int version;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getIsScheduled() {
		return isScheduled;
	}

	public void setIsScheduled(int isScheduled) {
		this.isScheduled = isScheduled;
	}

	public String getCronTrigger() {
		return cronTrigger;
	}

	public void setCronTrigger(String cronTrigger) {
		this.cronTrigger = cronTrigger;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
