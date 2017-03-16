package six.com.crawler.common.service.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.ResponeMsgManager;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.common.exception.RedisException;
import six.com.crawler.common.service.JobService;
import six.com.crawler.common.service.MasterScheduledService;
import six.com.crawler.schedule.MasterAbstractSchedulerManager;
import six.com.crawler.schedule.RegisterCenter;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午10:36:10
 */
@Service
public class MasterScheduledServiceImpl implements MasterScheduledService {

	final static Logger LOG = LoggerFactory.getLogger(MasterScheduledServiceImpl.class);

	static final String JOB_SERVICE_OPERATION_PRE = "master.scheduled.operation.";

	@Autowired
	private MasterAbstractSchedulerManager scheduleManager;

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
				if (scheduleManager.isRunning(job)) {
					msg = "这个任务[" + jobName + "]正在运行";
				} else {
					scheduleManager.execute(job);
					msg = "提交任务[" + jobName + "]到待执行队列";
				}
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
				if (!scheduleManager.isRunning(job)) {
					msg = "the job[" + jobName + "] is not running and don't suspend";
				} else {
					scheduleManager.suspend(job);
					msg = "the job[" + jobName + "] have been requested to execute suspend";
				}
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
				if (!scheduleManager.isRunning(job)) {
					msg = "the job[" + jobName + "] is not running and don't goOn";
				} else {
					scheduleManager.goOn(job);
					msg = "the job[" + jobName + "] have been requested to execute goOn";
				}
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
				if (!scheduleManager.isRunning(job)) {
					msg = "the job[" + jobName + "] is not running and don't stop";
				} else {
					scheduleManager.stop(job);
					msg = "the job[" + jobName + "] have been requested to execute stop";
				}
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

	public String end(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.get(jobName);
			if (null != job) {
				scheduleManager.end(job);
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

	public List<WorkerSnapshot> getWorkerInfo(String jobName) {
		Job job = jobService.get(jobName);
		List<WorkerSnapshot> result = null;
		if (null != job) {
			result = registerCenter.getWorkerSnapshots(jobName);
		} else {
			result = Collections.emptyList();
		}
		return result;
	}

	public MasterAbstractSchedulerManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(MasterAbstractSchedulerManager scheduleManager) {
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
