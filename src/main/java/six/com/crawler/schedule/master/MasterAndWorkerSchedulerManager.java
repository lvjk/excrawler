package six.com.crawler.schedule.master;

import six.com.crawler.entity.Job;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.DispatchType;

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
	public void execute(DispatchType dispatchType,String jobName) {
		masterSchedulerManager.execute(dispatchType,jobName);
	}

	@Override
	public void suspend(DispatchType dispatchType,String jobName) {
		masterSchedulerManager.suspend(dispatchType,jobName);
	}

	@Override
	public void goOn(DispatchType dispatchType,String jobName) {
		masterSchedulerManager.goOn(dispatchType,jobName);
	}

	@Override
	public void stop(DispatchType dispatchType,String jobName) {
		masterSchedulerManager.stop(dispatchType,jobName);
	}

	@Override
	public void stopAll(DispatchType dispatchType) {
		masterSchedulerManager.stopAll(dispatchType);
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
