package six.com.crawler.schedule;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:20
 * 
 *       调度接口 master SchedulerManager 和 worker SchedulerManager 接口
 * 
 *       master和worker 下面的操作:
 * 
 *       execute,suspend,goOn,stop
 * 
 *       都是双向的,master调用worker执行(execute)任务,worker执行任务后executeCallBack给master
 * 
 */
public interface SchedulerManager {

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

	boolean isRunning(String jobName);

	boolean isStart(String jobName);

	boolean isWait(String jobName);

	boolean isSuspend(String jobName);

	boolean isStop(String jobName);

	boolean isFinish(String jobName);

	/**
	 * 获取最后一个结束的任务
	 * 
	 * @param jobName
	 * @return
	 */
	JobSnapshot getLastEnd(String jobName, String excludeId);

	void updateJobSnapshot(JobSnapshot jobSnapshot);

	public void shutdown();
}
