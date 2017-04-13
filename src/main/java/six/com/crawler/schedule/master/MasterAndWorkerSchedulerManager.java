package six.com.crawler.schedule.master;

import six.com.crawler.entity.Job;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 下午5:46:14
 */
public class MasterAndWorkerSchedulerManager extends AbstractSchedulerManager {

	private AbstractSchedulerManager masterSchedulerManager;
	private AbstractSchedulerManager workerSchedulerManager;

	public MasterAndWorkerSchedulerManager(AbstractSchedulerManager masterSchedulerManager,
			AbstractSchedulerManager workerSchedulerManager) {
		this.masterSchedulerManager = masterSchedulerManager;
		this.workerSchedulerManager = workerSchedulerManager;
	}

	@Override
	public void repair() {
		masterSchedulerManager.repair();
	}

	@Override
	public void scheduled(Job job) {
		masterSchedulerManager.scheduled(job);
	}

	@Override
	public void cancelScheduled(String jobChainName) {
		masterSchedulerManager.cancelScheduled(jobChainName);
	}

	@Override
	public void execute(String jobName) {
		masterSchedulerManager.execute(jobName);
	}

	@Override
	public void suspend(String jobName) {
		masterSchedulerManager.suspend(jobName);
	}

	@Override
	public void goOn(String jobName) {
		masterSchedulerManager.goOn(jobName);
	}

	@Override
	public void stop(String jobName) {
		masterSchedulerManager.stop(jobName);
	}

	@Override
	public void stopAll() {
		masterSchedulerManager.stopAll();
	}

	@Override
	public void shutdown() {
		workerSchedulerManager.shutdown();
		masterSchedulerManager.shutdown();
	}

	@Override
	protected void init() {
	}

}
