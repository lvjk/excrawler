package six.com.crawler.schedule;

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
	private static final long serialVersionUID = 6032721410844522956L;
	private String clusterName;//集群名字
	private String name;//节点名字
	private String host;//节点host
	private int port;//节点服务端口
	private int trafficPort;//节点间通信端口
	private int cpu;//cpu使用情况
	private int mem;//内存使用情况
	private NodeStatus status;//节点状态
	private String sshUser;//节点 用户名字
	private String sshPasswd; //节点密码
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
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
	public NodeStatus getStatus() {
		return status;
	}
	public void setStatus(NodeStatus status) {
		this.status = status;
	}
	public String getSshUser() {
		return sshUser;
	}
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	public String getShhPasswd() {
		return sshPasswd;
	}
	public void setShhPasswd(String sshPasswd) {
		this.sshPasswd = sshPasswd;
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
	public int getTrafficPort() {
		return trafficPort;
	}
	public void setTrafficPort(int trafficPort) {
		this.trafficPort = trafficPort;
	}
}