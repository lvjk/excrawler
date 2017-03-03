package six.com.crawler.work;


import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author six
 * @date 2016年1月15日 下午6:20:00
 */
public interface Worker extends WorkerLifecycle {
	
	
	void bindManager(AbstractSchedulerManager manager);
	
	void bindJob(Job job);
	/**
	 * 初始化
	 */
	public void init();

	/**
	 * 获取 manager
	 * 
	 * @return
	 */
	AbstractSchedulerManager getManager();

	/**
	 * 获取 worker name
	 * 
	 * @return
	 */
	String getName();
	

	/**
	 * 获取worker 快照
	 * 
	 * @return
	 */
	WorkerSnapshot getWorkerSnapshot();

	
	/**
	 * 获取工作频率
	 * @return
	 */
	long getWorkFrequency();
	/**
	 * 获取最后一次活动时间
	 * 
	 * @return
	 */
	long getLastActivityTime();

	/**
	 * 获取work Job
	 * 
	 * @return
	 */
	Job getJob();
}
