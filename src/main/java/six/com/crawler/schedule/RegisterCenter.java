package six.com.crawler.schedule;

import java.util.List;

import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.work.Worker;

/**
 * @author six
 * @date 2016年8月23日 上午10:32:46 运行Job 注册中心
 */
public interface RegisterCenter {
	
	/**
	 * 通过nodeName  将注册中心的信息复位
	 * 
	 * @param correctNodeName
	 */
	public void reset(String nodeName);
	
	/**
	 * 通过nodeName获取节点
	 * @return
	 */
	public Node getNode(String nodeName);
	
	/**
	 * 获取所有节点
	 * 
	 * @return
	 */
	public List<Node> getNodes();

	/**
	 * 注册节点
	 * 
	 * @param node
	 * @param hearbeat
	 *            节点信息的有效期 秒
	 */
	public void registerNode(Node node, int hearbeat);
	
	
	/**
	 * 通过 nodeName删除注册的 Node
	 * @param nodeName
	 */
	public void delNode(String nodeName);
	
	
	/**
	 * 通过jobName获取 JobSnapshot
	 * @param node
	 * @return
	 */
	public JobSnapshot getJobSnapshot(String nodeName,String jobName);
	
	/**
	 * 获取所有 JobSnapshot
	 * @return
	 */
	public List<JobSnapshot> getJobSnapshots(String nodeName);
	/**
	 * 注册job 如果注册前已经存在的话那么会覆盖 把整个job信息注册进去
	 * 
	 * @param job
	 */
	public void registerJobSnapshot(JobSnapshot jobSnapshot);
	
	
	/**
	 * 更新 JobSnapshot 至注册中心
	 * @param jobSnapshot
	 */
	public void updateJobSnapshot(JobSnapshot jobSnapshot);
	
	/**
	 * 通过 nodeName jobName删除 删除注册的JobSnapshot
	 * @param nodeName
	 * @param jobName
	 */
	public void delJobSnapshot(String nodeName,String jobName);
	
	
	/**
	 * 通过jobName 获取 所有 WorkerSnapshot集合
	 * 
	 * @param job
	 * @return
	 */
	public List<WorkerSnapshot> getWorkerSnapshots(String nodeName,String jobName);
	
	/**
	 * 通过jobName 获取 所有 jobWorker 集合
	 * 
	 * @param job
	 * @return
	 */
	public List<Worker> getWorkers(String jobName);
	
	/**
	 * 通过workerName 获取 jobWorker 实例
	 * 
	 * @param workerName
	 * @return
	 */
	public Worker getWorker(String jobName, String workerName);

	/**
	 * 获取当前本地worker
	 * 
	 * @return
	 */
	public List<Worker> getLocalWorkers();
	
	/**
	 * 注册 Worker
	 * 
	 * @param job
	 */
	public void registerWorker(Worker worker);
	
	/**
	 * 更新 WorkerSnapshot信息
	 * 
	 * @param info
	 */
	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot);

	/**
	 * 检查注册运行job 的workers 是否全部wait
	 * 
	 * @param crawlerWorker
	 * @param job
	 * @return
	 */
	public boolean workerIsAllWaited(String nodeName,String jobName);

	/**
	 * 删除运行job和worker
	 * 
	 * @param job
	 */
	public void delWorker(String nodeName,String jobName,String workerName);

	/**
	 * 
	 * 
	 * @return
	 */
	public int  getSerNumOfWorkerByJob(String nodeName,String jobName);

}
