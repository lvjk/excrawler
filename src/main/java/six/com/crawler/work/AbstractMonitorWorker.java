package six.com.crawler.work;

import java.io.Serializable;

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
		triggerJobName = jobSnapshot.getDispatchType().getName();
		triggerJobSnapshotId = jobSnapshot.getDispatchType().getCurrentTimeMillis();
		fillWorkSpace();
	}
	
	private class MonitorData implements WorkSpaceData,Serializable{

		private static final long serialVersionUID = -6303001982409677119L;

		@Override
		public void setIndex(Index index) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Index getIndex() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getKey() {
			// TODO Auto-generated method stub
			return "monitor";
		}
		
	}

	private void fillWorkSpace() {
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
			fillWorkSpace();
		}
	}

	protected String getTriggerJobName() {
		return triggerJobName;
	}

	protected String getTriggerJobSnapshotId() {
		return triggerJobSnapshotId;
	}
}
