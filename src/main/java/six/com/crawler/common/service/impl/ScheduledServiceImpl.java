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
import six.com.crawler.common.service.ScheduledService;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午10:36:10
 */
@Service
public class ScheduledServiceImpl implements ScheduledService {

	final static Logger LOG = LoggerFactory.getLogger(ScheduledServiceImpl.class);

	static final String JOB_SERVICE_OPERATION_PRE = "scheduled.operation.";

	@Autowired
	private AbstractSchedulerManager commonScheduleManager;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private JobService jobService;

	/**
	 * 
	 * 警告:所有对job的操作 需要执行分布式锁。保证所有此Job 的操作 顺序执行
	 * 
	 */

	@Override
	public String execute(String jobHostNode, String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			if (commonScheduleManager.isRunning(jobHostNode, jobName)) {
				msg = "the job[" + jobName + "] is running";
			} else {
				Job job = jobService.queryByName(jobName);
				if (!job.getHostNode().equals(getCommonScheduleManager().getCurrentNode().getName())) {
					msg = "job.hostNode[" + job.getHostNode() + "] don't equals currentNode["
							+ getCommonScheduleManager().getCurrentNode().getName()
							+ "],please submit job to waitQueue of currentNode]";
				} else {
					commonScheduleManager.submitWaitQueue(job);
					msg = "submit job[" + jobName + "] succeed and will wait to be executed";
				}
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

	public String assistExecute(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.queryByName(jobName);
			commonScheduleManager.submitWaitQueue(job);
			msg = "submit job[" + jobName + "] succeed and will wait to be executed";
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
	public String suspend(String jobHostNode, String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			if (!commonScheduleManager.isRunning(jobHostNode, jobName)) {
				msg = "the job[" + jobName + "] is not running and don't suspend";
			} else {
				commonScheduleManager.suspendWorkerByJob(jobHostNode, jobName);
				msg = "the job[" + jobName + "] have been requested to execute suspend";
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
	public String goOn(String jobHostNode, String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			if (!commonScheduleManager.isRunning(jobHostNode, jobName)) {
				msg = "the job[" + jobName + "] is not running and don't goOn";
			} else {
				commonScheduleManager.goOnWorkerByJob(jobHostNode, jobName);
				msg = "the job[" + jobName + "] have been requested to execute goOn";
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
	public String stop(String jobHostNode, String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			if (!commonScheduleManager.isRunning(jobHostNode, jobName)) {
				msg = "the job[" + jobName + "] is not running and don't stop";
			} else {
				commonScheduleManager.stopWorkerByJob(jobHostNode, jobName);
				msg = "the job[" + jobName + "] have been requested to execute stop";
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

	@Override
	public String scheduled(String jobName) {
		String msg = null;
		try {
			redisManager.lock(JOB_SERVICE_OPERATION_PRE + jobName);
			Job job = jobService.queryByName(jobName);
			// 如果等于1 那么已经开启调度
			if (job.getIsScheduled() == 1) {
				job.setIsScheduled(0);
				commonScheduleManager.cancelScheduled(jobName);
				jobService.update(job);
				msg = "cancel schedule job[" + jobName + "] succeed";
			} else {
				job.setIsScheduled(1);
				commonScheduleManager.scheduled(job);
				msg = "schedule job[" + jobName + "] succeed";
			}
			jobService.update(job);
		} catch (RedisException e) {
			LOG.error("JobService scheduled job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} catch (Exception e) {
			LOG.error("JobService scheduled job[" + jobName + "] err", e);
			msg = ResponeMsgManager.SYSTEM_ERR_0001;
		} finally {
			redisManager.unlock(JOB_SERVICE_OPERATION_PRE + jobName);
		}
		return msg;
	}

	public AbstractSchedulerManager getCommonScheduleManager() {
		return commonScheduleManager;
	}

	public void setCommonScheduleManager(AbstractSchedulerManager commonScheduleManager) {
		this.commonScheduleManager = commonScheduleManager;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

}
