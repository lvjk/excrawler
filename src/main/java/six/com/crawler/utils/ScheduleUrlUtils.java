package six.com.crawler.utils;

import six.com.crawler.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:00:33 任务调度url 拼接工具
 */
public class ScheduleUrlUtils {

	/**
	 * 拼接调度工作节点执行任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getExecuteJob(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/worker/scheduled/execute/"
				+ jobName;
		return callUrl;
	}

	/**
	 * 拼接调度工作节点暂停任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getSuspendJob(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/worker/scheduled/suspend/" + jobName;
		return callUrl;
	}

	/**
	 * 拼接调度工作节点继续执行任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getGoonJob(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/worker/scheduled/goon/" + jobName;
		return callUrl;
	}

	/**
	 * 拼接调度工作节点停止任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getStopJob(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/worker/scheduled/stop/" + jobName;
		return callUrl;
	}
	
	/**
	 * 拼接调度工作节点停止任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getStopAll(Node node) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/worker/scheduled/stopAll";
		return callUrl;
	}

	/**
	 * 拼接调度工作节点完成任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getFinishJob(Node node, String jobName) {
		String callUrl = "http://" + node.getHost() + ":" + node.getPort() + "/crawler/worker/scheduled/finish/" + jobName;
		return callUrl;
	}
	
	/**
	 * 拼接调度工作节点完成任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getStartWorer(Node masterNode, String jobName,String workerName) {
		String callUrl = "http://" + masterNode.getHost() + ":" + masterNode.getPort() 
						+ "/crawler/master/scheduled/startWorker/" + jobName+"/"+workerName;
		return callUrl;
	}
	/**
	 * 拼接调度工作节点完成任务url
	 * 
	 * @param node
	 * @param jobName
	 * @return
	 */
	public static String getEndWorer(Node masterNode, String jobName,String workerName) {
		String callUrl = "http://" + masterNode.getHost() + ":" + masterNode.getPort() 
						+ "/crawler/master/scheduled/endWorker/" + jobName+"/"+workerName;
		return callUrl;
	}
}
