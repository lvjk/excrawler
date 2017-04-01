package six.com.crawler.entity;

import java.io.Serializable;
import java.util.List;

import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月19日 下午12:10:57 job job运行信息
 */
public class WorkerSnapshot implements Serializable {

	private static final long serialVersionUID = 8013640891595004526L;
	private String jobSnapshotId;// jobSnapshotId id
	private String name;// worker 名
	private String localNode;// 本地节点
	private String jobName;// 任务名
	private volatile WorkerLifecycleState state = WorkerLifecycleState.READY;// 状态
	private String startTime = "";// 开始时间
	private String endTime = "";// 结束时间
	private int totalProcessCount;// 统计处理多少个数据
	private int totalResultCount;// 统计获取多少个数据
	private int totalProcessTime;// 统计处理时间
	private int maxProcessTime;// 最大任务处理时间
	private int minProcessTime = 999999999;// 最小任务处理时间 默认为很大的数字
	private int avgProcessTime;// 平均每次任务处理时间
	private int errCount;// 任务异常数量
	private long lastReport;// 上一次Report 时间
	private transient List<WorkerErrMsg> workerErrMsgs;// job运行记录 异常集合

	public String getJobSnapshotId() {
		return jobSnapshotId;
	}

	public void setJobSnapshotId(String jobSnapshotId) {
		this.jobSnapshotId = jobSnapshotId;
	}

	public synchronized WorkerLifecycleState getState() {
		return state;
	}

	public synchronized void setState(WorkerLifecycleState state) {
		this.state = state;
	}

	public void setWorkerErrMsgs(List<WorkerErrMsg> workerErrMsgs) {
		this.workerErrMsgs = workerErrMsgs;
	}

	public List<WorkerErrMsg> getWorkerErrMsgs() {
		return workerErrMsgs;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getTotalProcessCount() {
		return totalProcessCount;
	}

	public void setTotalProcessCount(int totalProcessCount) {
		this.totalProcessCount = totalProcessCount;
	}

	public int getTotalResultCount() {
		return totalResultCount;
	}

	public void totalResultCountIncrement() {
		this.totalResultCount++;
	}

	public void setTotalResultCount(int totalResultCount) {
		this.totalResultCount = totalResultCount;
	}

	public int getTotalProcessTime() {
		return totalProcessTime;
	}

	public void setTotalProcessTime(int totalProcessTime) {
		this.totalProcessTime = totalProcessTime;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public int getErrCount() {
		return errCount;
	}

	public void setErrCount(int errCount) {
		this.errCount = errCount;
	}

	public int getAvgProcessTime() {
		return avgProcessTime;
	}

	public void setAvgProcessTime(int avgProcessTime) {
		this.avgProcessTime = avgProcessTime;
	}

	public int getMaxProcessTime() {
		return maxProcessTime;
	}

	public void setMaxProcessTime(int maxProcessTime) {
		this.maxProcessTime = maxProcessTime;
	}

	public int getMinProcessTime() {
		return minProcessTime;
	}

	public void setMinProcessTime(int minProcessTime) {
		this.minProcessTime = minProcessTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalNode() {
		return localNode;
	}

	public void setLocalNode(String localNode) {
		this.localNode = localNode;
	}

	public long getLastReport() {
		return lastReport;
	}

	public void setLastReport(long lastReport) {
		this.lastReport =lastReport;
	}

}
