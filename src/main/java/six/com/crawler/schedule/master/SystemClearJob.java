package six.com.crawler.schedule.master;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月19日 下午2:20:01
 */
public class SystemClearJob implements org.quartz.Job {

	final static Logger log = LoggerFactory.getLogger(SystemClearJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("start to execute SystemClearJob");
		AbstractSchedulerManager scheduleManager = (AbstractSchedulerManager) context.getJobDetail().getJobDataMap()
				.get(MasterSchedulerManager.SCHEDULER_MANAGER_KEY);
		int clearBeforeDays = (int) context.getJobDetail().getJobDataMap()
				.get(MasterSchedulerManager.CLEAR_BEFORE_DAYS_KEY);
		scheduleManager.getWorkerErrMsgDao().delBeforeDate(clearBeforeDays);
		scheduleManager.getWorkerSnapshotDao().delBeforeDate(clearBeforeDays);
		scheduleManager.getJobSnapshotDao().delBeforeDate(clearBeforeDays);
		scheduleManager.getPageDao().delBeforeDate(clearBeforeDays);
		log.info("end to execute SystemClearJob");
	}

}
