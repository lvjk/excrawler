package six.com.crawler.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月27日 上午11:52:43
 */
public class ScheduledJob implements org.quartz.Job {

	final static Logger LOG = LoggerFactory.getLogger(ScheduledJob.class);
			
	public static final String JOB_KEY = "job";

	public static final String SCHEDULER_MANAGER_KEY = "scheduleManager";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		AbstractSchedulerManager scheduleManager = (AbstractSchedulerManager) context.getJobDetail().getJobDataMap()
				.get(SCHEDULER_MANAGER_KEY);
		Job job = (Job) context.getJobDetail().getJobDataMap().get(JOB_KEY);
		LOG.info("定时触发job["+job.getName()+"],提交到待执行队列里");
		scheduleManager.execute(job);
	}

}
