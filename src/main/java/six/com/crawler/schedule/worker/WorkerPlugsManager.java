package six.com.crawler.schedule.worker;

import six.com.crawler.work.Worker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月8日 上午9:42:58
 */
public interface WorkerPlugsManager {

	/**
	 * 保存worker 插件 class
	 * 
	 * @param workerClassName
	 * @param classByte
	 * @return
	 */
	public boolean saveClass(Class<?> clz);

	/**
	 * 获取worker 插件 class
	 * 
	 * @param workerClassName
	 * @return
	 */
	public Worker<?> newWorker(String className);
}
