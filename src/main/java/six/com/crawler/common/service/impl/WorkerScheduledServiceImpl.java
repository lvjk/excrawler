package six.com.crawler.common.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.ResponeMsgManager;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.exception.RedisException;
import six.com.crawler.common.service.JobService;
import six.com.crawler.common.service.WorkerScheduledService;
import six.com.crawler.schedule.RegisterCenter;
import six.com.crawler.schedule.WorkerAbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 下午12:16:52
 */
@Service
public class WorkerScheduledServiceImpl implements WorkerScheduledService {

	final static Logger LOG = LoggerFactory.getLogger(MasterScheduledServiceImpl.class);

	static final String JOB_SERVICE_OPERATION_PRE = "worker.scheduled.operation.";

	@Autowired
	private WorkerAbstractSchedulerManager scheduleManager;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private RegisterCenter registerCenter;

	@Autowired
	private JobService jobService;

	/**
	 * 
	 * 警告:所有对job的操作 需要执行分布式锁。保证所有此Job 的操作 顺序执行
	 * 
	 */

	@Override
	public String execute(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.get(jobName);
			if (null != job) {
				scheduleManager.execute(job);
				msg = "提交任务[" + jobName + "]到待执行队列";
			} else {
				msg = "这个任务[" + jobName + "]不存在";
			}
		} catch (RedisException e) {
			LOG.error("JobService execute job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} catch (Exception e) {
			LOG.error("JobService execute job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} finally {
			redisManager.unlock(JOB_SERVICE_OPERATION_PRE + jobName);
		}
		return msg;
	}

	@Override
	public String suspend(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.get(jobName);
			if (null != job) {
				scheduleManager.suspend(job);
				msg = "the job[" + jobName + "] have been requested to execute suspend";
			} else {
				msg = "这个任务[" + jobName + "]不存在";
			}
		} catch (RedisException e) {
			LOG.error("JobService suspend job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} catch (Exception e) {
			LOG.error("JobService suspend job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} finally {
			redisManager.unlock(JOB_SERVICE_OPERATION_PRE + jobName);
		}
		return msg;
	}

	@Override
	public String goOn(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.get(jobName);
			if (null != job) {
				scheduleManager.goOn(job);
				msg = "the job[" + jobName + "] have been requested to execute goOn";
			} else {
				msg = "这个任务[" + jobName + "]不存在";
			}
		} catch (RedisException e) {
			LOG.error("JobService goOn job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} catch (Exception e) {
			LOG.error("JobService goOn job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} finally {
			redisManager.unlock(JOB_SERVICE_OPERATION_PRE + jobName);
		}
		return msg;

	}

	@Override
	public String stop(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.get(jobName);
			if (null != job) {
				scheduleManager.stop(job);
				msg = "the job[" + jobName + "] have been requested to execute stop";
			} else {
				msg = "这个任务[" + jobName + "]不存在";
			}
		} catch (RedisException e) {
			LOG.error("JobService stop job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} catch (Exception e) {
			LOG.error("JobService stop job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} finally {
			redisManager.unlock(JOB_SERVICE_OPERATION_PRE + jobName);
		}
		return msg;
	}

	public WorkerAbstractSchedulerManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(WorkerAbstractSchedulerManager scheduleManager) {
		this.scheduleManager = scheduleManager;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public RegisterCenter getRegisterCenter() {
		return registerCenter;
	}

	public void setRegisterCenter(RegisterCenter registerCenter) {
		this.registerCenter = registerCenter;
	}

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

}
