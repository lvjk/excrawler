package six.com.crawler.schedule.cache;

import java.util.List;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月14日 下午4:27:40
 */
public interface ScheduleCache {

	/**
	 * 获取缓存中正在执行的job
	 * 
	 * @param jobSnapshot
	 */
	public Job getJob(String jobName);

	/**
	 * 缓存执行的job
	 * 
	 * @param job
	 */
	public void setJob(Job job);

	/**
	 * 删除缓存中的job
	 * 
	 * @param jobName
	 */
	public void delJob(String jobName);

	
	/**
	 * 通过jobname获取 job运行快照
	 * 
	 * @param jobName
	 * @return
	 */
	public JobSnapshot getJobSnapshot(String jobName);

	/**
	 * 获取所有运行任务快照
	 * 
	 * @return
	 */
	public List<JobSnapshot> getJobSnapshots();

	/**
	 * 更新job运行快照
	 * 
	 * @param jobSnapshot
	 */
	public void updateJobSnapshot(JobSnapshot jobSnapshot);

	/**
	 * 通过jobName删除指定的任务运行快照
	 * 
	 * @param jobName
	 */
	public void delJobSnapshot(String jobName);

	
	/**
	 * 通过任务名获取运行任务的所有 worker快照信息
	 * 
	 * @param jobName
	 * @return
	 */
	public List<WorkerSnapshot> getWorkerSnapshots(String jobName);

	/**
	 * 更新缓存中workerSnapshot
	 * 
	 * @param workerSnapshot
	 */
	public void setWorkerSnapshot(WorkerSnapshot workerSnapshot);

	/**
	 * 更新缓存中workerSnapshot
	 * 
	 * @param workerSnapshot
	 */
	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot);

	/**
	 * 通过任务名称删除运行任务所有worker的快照
	 * 
	 * @param jobName
	 */
	public void delWorkerSnapshots(String jobName);
	
	/**
	 * 清楚所有
	 */
	public void clear();
}
