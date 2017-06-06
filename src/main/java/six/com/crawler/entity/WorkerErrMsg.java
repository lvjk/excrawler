package six.com.crawler.entity;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月10日 下午12:01:24
 * 
 */
public class WorkerErrMsg extends BasePo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4308723173856115866L;
	
	private String jobSnapshotId;// jobSnapshotId
	private String jobName;// jobName
	private String workerName;// WorkName
	private String startTime;// 异常发生时间
	private String type;// 异常类型
	private String msg;// 异常信息

	public String getJobSnapshotId() {
		return jobSnapshotId;
	}

	public void setJobSnapshotId(String jobSnapshotId) {
		this.jobSnapshotId = jobSnapshotId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}
	
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
