package six.com.crawler.node;

import java.io.Serializable;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月16日 上午5:54:52 类说明 爬虫节点信息
 */
public class Node implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5609665168838994265L;
	private String clusterName;// 集群名字
	private String name;// 节点名字
	private NodeType type;// 节点类型
	private String host;// 节点host
	private int trafficPort;// 节点间通信port
	private int port;// 节点服务端口
	private int cpu;// cpu使用情况
	private float mem;// 内存使用情况
	private int runningWorkerMaxSize;// 节点最大运行任务数
	private int totalJobSize;// 节点总任务数
	private int totalScheduleJobSize;// 节点调度总任务数
	private int totalNoScheduleJobSize;// 节点未调度总任务数
	private int runningWorkerSize;// 节点运行worker总数

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getTrafficPort() {
		return trafficPort;
	}

	public void setTrafficPort(int trafficPort) {
		this.trafficPort = trafficPort;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public float getMem() {
		return mem;
	}

	public void setMem(float mem) {
		this.mem = mem;
	}

	public int getRunningWorkerMaxSize() {
		return runningWorkerMaxSize;
	}

	public void setRunningWorkerMaxSize(int runningWorkerMaxSize) {
		this.runningWorkerMaxSize = runningWorkerMaxSize;
	}

	public synchronized int getFreeWorkerSize() {
		return runningWorkerMaxSize - runningWorkerSize;
	}

	public int getTotalJobSize() {
		return totalJobSize;
	}

	public void setTotalJobSize(int totalJobSize) {
		this.totalJobSize = totalJobSize;
	}

	public int getTotalScheduleJobSize() {
		return totalScheduleJobSize;
	}

	public void setTotalScheduleJobSize(int totalScheduleJobSize) {
		this.totalScheduleJobSize = totalScheduleJobSize;
	}

	public int getTotalNoScheduleJobSize() {
		return totalNoScheduleJobSize;
	}

	public void setTotalNoScheduleJobSize(int totalNoScheduleJobSize) {
		this.totalNoScheduleJobSize = totalNoScheduleJobSize;
	}

	public synchronized int getRunningWorkerSize() {
		return runningWorkerSize;
	}

	/**
	 * 节点运行任务数设值，并返回操作后的值
	 * 
	 * @return
	 */
	public synchronized void setRunningWorkerSize(int runningWorkerSize) {
		this.runningWorkerSize = runningWorkerSize;
	}

	/**
	 * 节点运行任务数减1，并返回操作后的值
	 * 
	 * @return
	 */
	public synchronized int incrAndGetRunningWorkerSize() {
		return this.runningWorkerSize++;
	}

	/**
	 * 节点运行任务数加1，并返回操作后的值
	 * 
	 * @return
	 */
	public synchronized int decrAndGetRunningWorkerSize() {
		return this.runningWorkerSize--;
	}

	public String toString() {
		return name + "[" + host + ":" + port + "]";
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object o) {
		if (null != o) {
			if (o instanceof Node) {
				Node targetNode = (Node) o;
				if (toString().equals(targetNode.toString())) {
					return true;
				}
			}
		}
		return false;
	}
}