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
	public String execute(TriggerType dispatchType, String jobName);

	/**
	 * 暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public void suspend(TriggerType dispatchType, String jobName);

	/**
	 * 让job[jobName] worker 休息默认时间
	 * 
	 * @param dispatchType
	 * @param jobName
	 */
	public void rest(TriggerType dispatchType, String jobName);

	/**
	 * 继续任务
	 * 
	 * @param job
	 * @return
	 */
	public void goOn(TriggerType dispatchType, String jobName);

	/**
	 * 停止任务
	 * 
	 * @param job
	 * @return
	 */
	public void stop(TriggerType dispatchType, String jobName);

	/**
	 * 完成任务
	 * 
	 * @param dispatchType
	 * @param jobName
	 */
	public void finish(TriggerType dispatchType, String jobName);

	/**
	 * 停止所有任务
	 * 
	 * @param job
	 * @return
	 */
	public void stopAll(TriggerType dispatchType);

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

	/** job 下所有worker的状态判断方法 **/

	/**
	 * 判断job下worker是否都为非运行状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isNotRuning(String jobName);

	/**
	 * 判断job下worker是否都为运行状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isRunning(String jobName);

	/**
	 * 判断job下worker是否都为开始状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isStart(String jobName);

	/**
	 * 判断job下worker是否都为等待状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isWait(String jobName);

	/**
	 * 判断job下worker是否都为暂停状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isSuspend(String jobName);

	/**
	 * 判断job下worker是否都为停止状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isStop(String jobName);

	/**
	 * 判断job下worker是否都为完成状态
	 * 
	 * @param jobName
	 * @return
	 */
	boolean isFinish(String jobName);

	/** job 下所有worker的状态判断方法 **/

	/**
	 * 获取指定任务下最后一个结束的任务快照
	 * 
	 * @param jobName
	 * @param excludeId
	 * @return
	 */
	JobSnapshot getLastEnd(String jobName, String excludeId);

	/**
	 * 调度器shutdown
	 */
	void shutdown();
}
