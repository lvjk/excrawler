package six.com.crawler.admin.service.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.admin.service.MasterScheduledService;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午10:36:10
 */
@Service
public class MasterScheduledServiceImpl implements MasterScheduledService {

	final static Logger LOG = LoggerFactory.getLogger(MasterScheduledServiceImpl.class);

	@Autowired
	private AbstractSchedulerManager scheduleManager;


	@Override
	public String execute(String jobName) {
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (scheduleManager.isRunning(jobName)) {
				msg = "这个任务[" + jobName + "]正在运行";
			} else {
				scheduleManager.execute(jobName);
				msg = "提交任务[" + jobName + "]到待执行队列";
			}
		} else {
			msg = "这个任务[" + jobName + "]不存在";
		}
		return msg;
	}

	@Override
	public String suspend(String jobName) {
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (!scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is not running and don't suspend";
			} else {
				scheduleManager.suspend(jobName);
				msg = "the job[" + jobName + "] have been requested to execute suspend";
			}
		} else {
			msg = "这个任务[" + jobName + "]不存在";
		}
		return msg;
	}

	@Override
	public String goOn(String jobName) {
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (!scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is not running and don't goOn";
			} else {
				scheduleManager.goOn(jobName);
				msg = "the job[" + jobName + "] have been requested to execute goOn";
			}
		} else {
			msg = "这个任务[" + jobName + "]不存在";
		}
		return msg;

	}

	@Override
	public String stop(String jobName) {
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (!scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is not running and don't stop";
			} else {
				scheduleManager.stop(jobName);
				msg = "the job[" + jobName + "] have been requested to execute stop";
			}
		} else {
			msg = "这个任务[" + jobName + "]不存在";
		}
		return msg;
	}


	public List<WorkerSnapshot> getWorkerInfo(String jobName) {
		List<WorkerSnapshot> result = null;
		if (StringUtils.isNotBlank(jobName)) {
			result = scheduleManager.getWorkerSnapshots(jobName);
		} else {
			result = Collections.emptyList();
		}
		return result;
	}

	public AbstractSchedulerManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(AbstractSchedulerManager scheduleManager) {
		this.scheduleManager = scheduleManager;
	}
}
