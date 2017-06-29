package six.com.crawler.schedule;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月23日 上午11:14:42
 */
public class SchedulerCommand implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3400476814151319930L;
	
	public static final String EXECUTE="execute";
	public static final String SUSPEND="suspend";
	public static final String GOON="goon";
	public static final String STOP="stop";
	public static final String FINISH="finish";
	
	private String id;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



	private String command;
	private String jobName;

	public String toString() {
		return command + " job[" + jobName + "]";
	}
}
