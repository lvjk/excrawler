package six.com.crawler.common.entity;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;

import six.com.crawler.common.DateFormats;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月16日 上午9:32:02
 */
public class JobSnapshot implements Serializable {

	private static final long serialVersionUID = -5076089473208316846L;
	private String id;// id
	private String name;// 任务名
	private String localNode;// job所属哪个节点名字
	private JobSnapshotState state=JobSnapshotState.READY;// 任务状态
	private String tableName;//数据表名
	private String queueName;// 任务队列名
	private String startTime;// 开始时间
	private String endTime;
	private int isScheduled;//
	private int proxyQueueCount;// 代理任务队列数量
	private int realQueueCount;// 真实任务队列数量
	private int errQueueCount;// 错误任务队列数量
	private int totalProcessCount;// 统计处理多少个数据
	private int totalResultCount;// 统计获取多少个数据
	private int totalProcessTime;// 统计获取处理时间
	private int avgProcessTime;// 平均每次任务处理时间
	private int maxProcessTime;// 最大任务处理时间
	private int minProcessTime;// 最小任务处理时间
	private int errCount;// 异常次数
	private transient List<WorkerSnapshot> workerSnapshots;// job运行记录WorkerSnapshot

	public JobSnapshot() {
	}

	public JobSnapshot(String name) {
		this.name = name;
		this.id = name + "_" + DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	
	public int getState() {
		return state.value();
	}

	public JobSnapshotState getEnumState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = JobSnapshotState.valueOf(state);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
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

	public int getIsScheduled() {
		return isScheduled;
	}

	public void setIsScheduled(int isScheduled) {
		this.isScheduled = isScheduled;
	}

	public int getProxyQueueCount() {
		return proxyQueueCount;
	}

	public void setProxyQueueCount(int proxyQueueCount) {
		this.proxyQueueCount = proxyQueueCount;
	}

	public int getRealQueueCount() {
		return realQueueCount;
	}

	public void setRealQueueCount(int realQueueCount) {
		this.realQueueCount = realQueueCount;
	}

	public int getErrQueueCount() {
		return errQueueCount;
	}

	public void setErrQueueCount(int errQueueCount) {
		this.errQueueCount = errQueueCount;
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

	public void setTotalResultCount(int totalResultCount) {
		this.totalResultCount = totalResultCount;
	}

	public int getTotalProcessTime() {
		return totalProcessTime;
	}

	public void setTotalProcessTime(int totalProcessTime) {
		this.totalProcessTime = totalProcessTime;
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

	public List<WorkerSnapshot> getWorkerSnapshots() {
		return workerSnapshots;
	}

	public void setWorkerSnapshots(List<WorkerSnapshot> workerSnapshots) {
		this.workerSnapshots = workerSnapshots;
	}

	public int getErrCount() {
		return errCount;
	}

	public void setErrCount(int errCount) {
		this.errCount = errCount;
	}

	public String toString() {
		StringBuilder sbd = new StringBuilder();
		sbd.append("id:").append(id).append(",");
		sbd.append("name:").append(name).append(",");
		sbd.append("startTime:").append(startTime).append(",");
		sbd.append("endTime:").append(endTime).append(",");
		sbd.append("totalProcessCount:").append(totalProcessCount).append(",");
		sbd.append("totalResultCount:").append(totalResultCount).append(",");
		sbd.append("avgProcessTime:").append(avgProcessTime).append(",");
		sbd.append("maxProcessTime:").append(maxProcessTime).append(",");
		sbd.append("minProcessTime:").append(minProcessTime).append(",");
		sbd.append("errCount:").append(errCount).append(",");
		return sbd.toString();
	}


}
