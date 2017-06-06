package six.com.crawler.work;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.work.exception.WorkerException;
import six.com.crawler.work.space.Index;
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
	private String targetJobName;

	/**
	 * 被监控的任务运行id
	 */
	private String targetJobSnapshotId;

	public AbstractMonitorWorker() {
		super(WorkSpaceData.class);
	}

	@Override
	protected void initWorker(JobSnapshot jobSnapshot) {
		targetJobName = jobSnapshot.getDispatchType().getName();
		targetJobSnapshotId = jobSnapshot.getDispatchType().getCurrentTimeMillis();
		fillWorkSpace();
	}

	private void fillWorkSpace() {
		getWorkSpace().push(new WorkSpaceData() {
			@Override
			public void setIndex(Index index) {
			}

			@Override
			public String getKey() {
				return null;
			}

			@Override
			public Index getIndex() {
				return null;
			}
		});
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
			fillWorkSpace();
		}
	}

	protected String getTargetJobName() {
		return targetJobName;
	}

	protected String getTargetJobSnapshotId() {
		return targetJobSnapshotId;
	}
}
