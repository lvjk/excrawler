package six.com.crawler.common.service;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 下午12:25:31
 */
public interface WorkerScheduledService {

	/**
	 * 执行job
	 * 
	 * @param jobName
	 * @return
	 */
	public String execute(String jobName);

	/**
	 * 暂停执行job
	 * 
	 * @param jobName
	 * @return
	 */
	public String suspend(String jobName);

	/**
	 * 继续执行job
	 * 
	 * @param jobName
	 * @return
	 */
	public String goOn(String jobName);

	/**
	 * 终止执行job
	 * 
	 * @param jobName
	 * @return
	 */
	public String stop(String jobName);

}
