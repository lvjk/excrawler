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
	public void execute(DispatchType dispatchType, String jobName);

	/**
	 * 暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public void suspend(DispatchType dispatchType, String jobName);

	/**
	 * 继续任务
	 * 
	 * @param job
	 * @return
	 */
	public void goOn(DispatchType dispatchType, String jobName);

	/**
	 * 停止任务
	 * 
	 * @param job
	 * @return
	 */
	public void stop(DispatchType dispatchType, String jobName);

	/**
	 * 停止所有任务
	 * 
	 * @param job
	 * @return
	 */
	public void stopAll(DispatchType dispatchType);

	
	public boolean isNotRuning(String jobName);
	/**
	 * job下的worker是否全部Wait
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean isWait(String jobName);
	
	/**
	 * job下的worker是否全部stop
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean isStop(String jobName);

	/**
	 * job下的worker是否全部finish
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean isFinish(String jobName);

	public void shutdown();
}
