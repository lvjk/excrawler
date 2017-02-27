package six.com.crawler.common.entity;

import java.io.Serializable;

/**
* @author sixliu E-mail:359852326@qq.com
* @version 创建时间：2016年1月16日 上午5:54:52
* 类说明 爬虫节点信息
*/
public class Node  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5609665168838994265L;
	private String name;//节点名字
	private String host;//节点host
	private int port;//节点服务端口
	private int cpu;//cpu使用情况
	private int mem;//内存使用情况
	private int runningJobMaxSize;//节点最大运行任务数
	private int totalJobSize;//节点总任务数
	private int totalScheduleJobSize;//节点调度总任务数
	private int totalNoScheduleJobSize;//节点未调度总任务数
	private int runningJobSize;//节点未调度总任务数
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
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
	public int getMem() {
		return mem;
	}
	public void setMem(int mem) {
		this.mem = mem;
	}
	public int getRunningJobMaxSize() {
		return runningJobMaxSize;
	}
	public void setRunningJobMaxSize(int runningJobMaxSize) {
		this.runningJobMaxSize = runningJobMaxSize;
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
	public int getRunningJobSize() {
		return runningJobSize;
	}
	public void setRunningJobSize(int runningJobSize) {
		this.runningJobSize = runningJobSize;
	}
	
	
}