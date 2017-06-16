package six.com.crawler.work;

import java.io.Serializable;
import java.util.List;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotStatus;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.schedule.TriggerType;
import six.com.crawler.work.exception.WorkerException;
import six.com.crawler.work.exception.WorkerMonitorException;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月6日 上午10:56:46
 * 
 *       监控任务worker
 * 
 */
public class CommonMonitorWorker extends AbstractMonitorWorker implements Serializable{

	private static final long serialVersionUID = -3421176209187012514L;

	/**
	 * 实现监控逻辑,需要循环监控的话，返回true,否则返回false监控任务线程将会结束
	 * 
	 * @return
	 * @throws WorkerException
	 */
	protected boolean doMonitor() throws WorkerException{
		JobSnapshot jobSnapshot=getManager().getScheduleCache().getJobSnapshot(getTriggerJobName());
		if(null==jobSnapshot){
			jobSnapshot=getManager().getJobSnapshotDao().query(getTriggerJobSnapshotId(), getTriggerJobName());
		}
		if(null==jobSnapshot){
			 throw new WorkerMonitorException("Job info exception!");
		}
		if(jobSnapshot.getStatus()==JobSnapshotStatus.FINISHED.value()){
			//任务结束
			getWorkSpace().clearDoing();
			getManager().getMasterSchedulerManager().finish(TriggerType.newDispatchTypeByMaster(),getJobSnapshot().getName());
			return false;
		}else{
			//非正常结束
			List<WorkerErrMsg> msgs = getManager().getWorkerErrMsgDao().queryByJob(getTriggerJobSnapshotId(), getTriggerJobName());
			
			if(jobSnapshot.getStatus()==JobSnapshotStatus.STOP.value()){
				if(msgs == null || msgs.size() == 0){
					getManager().getMasterSchedulerManager().stop(TriggerType.newDispatchTypeByMaster(),getJobSnapshot().getName());
					return false;
				}else{
					for (int i = 0; i < msgs.size(); i++) {
						if(msgs.get(i).getType().equals("worker_init")){
							//重新调度任务并返回false
							getManager().getMasterSchedulerManager().execute(TriggerType.newDispatchTypeByMaster(), getTriggerJobName());
							return false;
						}
					}
				}
			}
			
			return true;
		}
	};

	@Override
	protected void onError(Exception t, WorkSpaceData workerData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void insideDestroy() {
		// TODO Auto-generated method stub
		
	}
}
