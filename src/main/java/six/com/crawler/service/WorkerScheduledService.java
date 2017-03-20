package six.com.crawler.service;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午3:41:04
 */
public interface WorkerScheduledService {

	/**
	 * 执行任务
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean execute(String jobName);

	/**
	 * 暂停任务
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean suspend(String jobName);

	/**
	 * 继续任务
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean goOn(String jobName);

	/**
	 * 停止任务
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean stop(String jobName);

	/**
	 * 全部停止
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean stopAll();
}
