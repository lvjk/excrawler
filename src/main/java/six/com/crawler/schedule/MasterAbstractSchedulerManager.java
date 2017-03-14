package six.com.crawler.schedule;

import java.util.List;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:45:58
 */
public abstract class MasterAbstractSchedulerManager extends AbstractSchedulerManager {

	/**
	 * 判断注册中心是否有此job的worker
	 * 
	 * @param job
	 * @return
	 */
	public abstract boolean isRunning(Job job);
	/**
	 * 向调度器 调度job
	 * 
	 * @param job
	 */
	public abstract void scheduled(Job job);

	/**
	 * 取消调度
	 * 
	 * @param job
	 */
	public abstract void cancelScheduled(String jobName);

	/**
	 * 通过job获取此job工作节点
	 * 
	 * @param job
	 * @return
	 */
	public abstract List<Node> getWorkerNode(String jobName);
	
	/**
	 * 任务结束 此方法是在 调用stop和finish 被调用
	 * @param job
	 */
	public abstract void end(Job job);
}
