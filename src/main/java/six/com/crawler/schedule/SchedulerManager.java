package six.com.crawler.schedule;

import java.util.List;
import java.util.Observer;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Node;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:20
 */
public interface SchedulerManager extends Observer {

	/**
	 * 暂停运行的job
	 * 
	 * @param jobName
	 */
	public void suspendWorkerByJob(String jobHostNode,String jobName);

	/**
	 * 恢复暂停的 job
	 * 
	 * @param jobName
	 */
	public void goOnWorkerByJob(String jobHostNode,String jobName);

	/**
	 * 停止运行的job
	 * 
	 * @param jobName
	 */
	public void stopWorkerByJob(String jobHostNode,String jobName);
	
	
	/**
	 * 完成运行的job
	 * 
	 * @param jobName
	 */
	public void finishWorkerByJob(String jobHostNode,String jobName);

	/**
	 * 判断注册中心是否有此job的worker
	 * 
	 * @param job
	 * @return
	 */
	public boolean isRunning(String jobHostNode,String jobName);

	/**
	 * 获取运行的job数
	 * 
	 * @return
	 */
	public int getRunningWorkerCount();

	/**
	 * 向调度器 调度job
	 * 
	 * @param job
	 */
	public void scheduled(Job job);

	/**
	 * 取消调度
	 * 
	 * @param job
	 */
	public void cancelScheduled(String jobName);

	/**
	 * 提交job至等待队列
	 * 
	 * @param job
	 */
	public void submitWaitQueue(Job job);

	/**
	 * 获取空闲节点
	 * 
	 * @return
	 */
	public List<Node> getFreeNodes();

	/**
	 * 通知 nodes 节点执行job
	 * 
	 * @param nodes
	 * @param job
	 * @return 各节点执行 job的相应
	 */
	public String callNodeExecuteJob(Node node,String jobName);

	/**
	 * 通过email 给管理员发送信息
	 * 
	 * @param topic
	 * @param msg
	 */
	public void noticeAdminByEmail(String topic, String msg);

	/**
	 * 通过短信给管理员发送信息
	 * 
	 * @param topic
	 * @param msg
	 */
	public void noticeAdminByPhone(String topic, String msg);
}
