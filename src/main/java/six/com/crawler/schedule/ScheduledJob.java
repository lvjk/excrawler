package six.com.crawler.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月27日 上午11:52:43
 */
public class ScheduledJob implements org.quartz.Job {

	final static Logger LOG = LoggerFactory.getLogger(ScheduledJob.class);

	public static final String JOB_NAME_KEY = "jobName";

	public static final String SCHEDULER_MANAGER_KEY = "scheduleManager";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		AbstractSchedulerManager scheduleManager = (AbstractSchedulerManager) context.getJobDetail().getJobDataMap()
				.get(SCHEDULER_MANAGER_KEY);
		String jobName = (String) context.getJobDetail().getJobDataMap().get(JOB_NAME_KEY);
		LOG.info("quartz scheduled job[" + jobName + "]");
		scheduleManager.execute(jobName);
	}

}
