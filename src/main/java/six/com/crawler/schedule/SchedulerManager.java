package six.com.crawler.schedule;

import six.com.crawler.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:20
 * 
 *       调度接口
 * 
 */
public interface SchedulerManager {

	/**
	 * 调度器修复,当节点启动时会自动被调用
	 */
	public void repair();

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
	public void cancelScheduled(String jobChainName);

	/**
	 * 执行任务
	 * 
	 * @param job
	 */
	public void execute(String jobName);

	/**
	 * 暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public void suspend(String jobName);

	/**
	 * 继续任务
	 * 
	 * @param job
	 * @return
	 */
	public void goOn(String jobName);

	/**
	 * 停止任务
	 * 
	 * @param job
	 * @return
	 */
	public void stop(String jobName);

	/**
	 * 停止所有任务
	 * 
	 * @param job
	 * @return
	 */
	public void stopAll();

	public void shutdown();

	/**
	 * job worker 是否全部waited
	 * 
	 * @param jobName
	 * @return
	 */
	public abstract boolean workerIsAllWaited(String jobName);
}
