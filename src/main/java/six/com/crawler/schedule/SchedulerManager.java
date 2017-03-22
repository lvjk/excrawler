package six.com.crawler.schedule;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月25日 下午2:04:20
 */
public interface SchedulerManager {

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

	/**
	 * job worker 是否全部waited
	 * @param jobName
	 * @return
	 */
	public abstract boolean workerIsAllWaited(String jobName);
}
