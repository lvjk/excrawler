package six.com.crawler.work;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.work.exception.WorkerException;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月6日 上午10:56:46
 * 
 *       监控任务worker
 * 
 */
public abstract class AbstractMonitorWorker extends AbstractWorker<WorkSpaceData> {

	/**
	 * 被监控的任务名称
	 */
	private String triggerJobName;

	/**
	 * 被监控的任务运行id
	 */
	private String triggerJobSnapshotId;
	
	public AbstractMonitorWorker() {
		super(WorkSpaceData.class);
	}

	@Override
	protected void initWorker(JobSnapshot jobSnapshot) {
		triggerJobName = jobSnapshot.getTriggerType().getName();
		triggerJobSnapshotId = jobSnapshot.getTriggerType().getCurrentTimeMillis();
		getWorkSpace().push(new MonitorData());
	}
	


	/**
	 * 实现监控逻辑,需要循环监控的话，返回true,否则返回false监控任务线程将会结束
	 * 
	 * @return
	 * @throws WorkerException
	 */
	protected abstract boolean doMonitor() throws WorkerException;

	@Override
	protected void insideWork(WorkSpaceData workerData) throws WorkerException {
		if (doMonitor()) {
			getWorkSpace().errRetryPush(workerData);
		}
	}

	protected String getTriggerJobName() {
		return triggerJobName;
	}

	protected String getTriggerJobSnapshotId() {
		return triggerJobSnapshotId;
	}
}
