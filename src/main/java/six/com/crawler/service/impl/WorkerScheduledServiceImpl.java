package six.com.crawler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.entity.Job;
import six.com.crawler.schedule.WorkerSchedulerManager;
import six.com.crawler.service.JobService;
import six.com.crawler.service.WorkerScheduledService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午3:44:27
 */
@Service
public class WorkerScheduledServiceImpl implements WorkerScheduledService {

	@Autowired
	private WorkerSchedulerManager workerSchedulerManager;

	@Autowired
	private JobService jobService;

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	public WorkerSchedulerManager getWorkerSchedulerManager() {
		return workerSchedulerManager;
	}

	public void setWorkerSchedulerManager(WorkerSchedulerManager workerSchedulerManager) {
		this.workerSchedulerManager = workerSchedulerManager;
	}

	@Override
	public boolean execute(String jobName) {
		Job job = jobService.get(jobName);
		workerSchedulerManager.execute(job);
		return true;
	}

	@Override
	public boolean suspend(String jobName) {
		Job job = jobService.get(jobName);
		workerSchedulerManager.suspend(job);
		return true;
	}

	@Override
	public boolean goOn(String jobName) {
		Job job = jobService.get(jobName);
		workerSchedulerManager.goOn(job);
		return true;
	}

	@Override
	public boolean stop(String jobName) {
		Job job = jobService.get(jobName);
		workerSchedulerManager.stop(job);
		return true;
	}

	@Override
	public boolean stopAll() {
		workerSchedulerManager.stopAll();
		return true;
	}

}
