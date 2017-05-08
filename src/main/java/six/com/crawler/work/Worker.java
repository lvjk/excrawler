package six.com.crawler.work;

import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.worker.WorkerSchedulerManager;
import six.com.crawler.work.space.WorkSpace;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author six
 * @date 2016年1月15日 下午6:20:00
 */
public interface Worker<T extends WorkSpaceData> extends WorkerLifecycle {

	void bindConfigure(SpiderConfigure configure);
	
	void bindWorkerSnapshot(WorkerSnapshot workerSnapshot);

	void bindManager(WorkerSchedulerManager manager);

	void bindJobSnapshot(JobSnapshot jobSnapshot);

	void bindJob(Job job);
	
	/**
	 * 初始化
	 */
	public void init();

	/**
	 * 获取 worker name
	 * 
	 * @return
	 */
	String getName();
	
	
	SpiderConfigure getConfigure();
	
	/**
	 * 获取 manager
	 * 
	 * @return
	 */
	WorkerSchedulerManager getManager();

	/**
	 * 获取worker 快照
	 * 
	 * @return
	 */
	WorkerSnapshot getWorkerSnapshot();

	/**
	 * 获取工作频率
	 * 
	 * @return
	 */
	long getWorkFrequency();

	/**
	 * 获取最后一次活动时间
	 * 
	 * @return
	 */
	long getLastActivityTime();

	JobSnapshot getJobSnapshot();

	/**
	 * 获取work Job
	 * 
	 * @return
	 */
	Job getJob();
	
	WorkSpace<T> getWorkQueue();
}
