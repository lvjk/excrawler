package six.com.crawler.work;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotStatus;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.schedule.SchedulerCommand;
import six.com.crawler.schedule.SchedulerCommandGroup;
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
public class CommonMonitorWorker extends AbstractMonitorWorker {
	
	final static Logger log = LoggerFactory.getLogger(CommonMonitorWorker.class);

	/**
	 * 实现监控逻辑,需要循环监控的话，返回true,否则返回false监控任务线程将会结束
	 * 
	 * @return
	 * @throws WorkerException
	 */
	protected boolean doMonitor() throws WorkerException {
		JobSnapshot jobSnapshot = getManager().getScheduleCache().getJobSnapshot(getTriggerJobName());
		// 当null == jobSnapshot时表明被监控的任务结束了
		if (null == jobSnapshot) {
			jobSnapshot = getManager().getJobSnapshotDao().query(getTriggerJobSnapshotId(), getTriggerJobName());
			if (null == jobSnapshot) {
				throw new WorkerMonitorException("Job info exception!");
			}
			
			log.info("job "+jobSnapshot.getName()+" status is :"+jobSnapshot.getStatus());
			if (jobSnapshot.getStatus() == JobSnapshotStatus.STOP.value()) {
				// 非正常结束
				List<WorkerErrMsg> msgs = getManager().getWorkerErrMsgDao().queryByJob(getTriggerJobSnapshotId(),
						getTriggerJobName());
				
				if (msgs != null) {
					for (int i = 0; i < msgs.size(); i++) {
						log.info("Error message info is:"+msgs.get(i).toString());
						if (msgs.get(i).getType().equals("worker_init")) {
							// 重新调度任务
							SchedulerCommandGroup commandGroup = new SchedulerCommandGroup();

							SchedulerCommand command1 = new SchedulerCommand();
							command1.setCommand(SchedulerCommand.FINISH);
							command1.setJobName(getJob().getName());

							SchedulerCommand command2 = new SchedulerCommand();
							command2.setCommand(SchedulerCommand.EXECUTE);
							command2.setJobName(getTriggerJobName());

							commandGroup.setSchedulerCommands(new SchedulerCommand[] { command1, command2 });

							getManager().getMasterSchedulerManager(result -> {
							}).submitCommand(commandGroup);
						}
					}
				}
			}
			// 返回true
			return false;
		} else {
			// 否则返回true，继续监控
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
