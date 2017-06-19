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
 * 
 *       运行job的worker接口定义
 * 
 */
public interface Worker<T extends WorkSpaceData> extends WorkerLifecycle {

	/**
	 * 绑定环境配置
	 * 
	 * @param configure
	 */
	void bindConfigure(SpiderConfigure configure);

	/**
	 * 绑定worker管理者
	 * 
	 * @param manager
	 */
	void bindManager(WorkerSchedulerManager manager);

	/**
	 * 绑定worker job
	 * 
	 * @param job
	 */
	void bindJob(Job job);
	
	/**
	 * 绑定worker jobSnapshotId
	 * 
	 * @param workerSnapshot
	 */
	void bindJobSnapshotId(String jobSnapshotId);

	/**
	 * 绑定worker 运行快照
	 * 
	 * @param workerSnapshot
	 */
	void bindWorkerSnapshot(WorkerSnapshot workerSnapshot);

	/**
	 * 获取环境配置
	 * 
	 * @return
	 */
	SpiderConfigure getConfigure();

	/**
	 * 获取 manager
	 * 
	 * @return
	 */
	WorkerSchedulerManager getManager();

	/**
	 * 获取job运行快照
	 * 
	 * @return
	 */
	JobSnapshot getJobSnapshot();
	
	String getJobSnapshotId();

	/**
	 * 获取work Job
	 * 
	 * @return
	 */
	Job getJob();

	/**
	 * 获取工作空间
	 * 
	 * @return
	 */
	WorkSpace<T> getWorkSpace();

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

}
