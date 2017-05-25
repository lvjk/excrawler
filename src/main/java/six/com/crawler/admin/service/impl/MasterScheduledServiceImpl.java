package six.com.crawler.admin.service.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.admin.service.BaseService;
import six.com.crawler.admin.service.MasterScheduledService;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.DispatchType;
import six.com.crawler.schedule.master.AbstractMasterSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午10:36:10
 */
@Service
public class MasterScheduledServiceImpl extends BaseService implements MasterScheduledService {

	final static Logger LOG = LoggerFactory.getLogger(MasterScheduledServiceImpl.class);

	@Autowired
	private AbstractMasterSchedulerManager scheduleManager;

	@Override
	public ResponseMsg<String> execute(String jobName) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is running";
			} else {
				scheduleManager.execute(DispatchType.newDispatchTypeByManual(), jobName);
				msg = "already put job[" + jobName + "] to waiting queue";
			}
		} else {
			msg = "the job[" + jobName + "] is not existed";
		}
		responseMsg.isOk();
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@Override
	public ResponseMsg<String> suspend(String jobName) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (!scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is not running and don't suspend";
			} else {
				scheduleManager.suspend(DispatchType.newDispatchTypeByManual(), jobName);
				msg = "the job[" + jobName + "] have been requested to execute suspend";
			}
		} else {
			msg = "the job[" + jobName + "] is not existed";
		}
		responseMsg.isOk();
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@Override
	public ResponseMsg<String> goOn(String jobName) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (!scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is not running and don't goOn";
			} else {
				scheduleManager.goOn(DispatchType.newDispatchTypeByManual(), jobName);
				msg = "the job[" + jobName + "] have been requested to execute goOn";
			}
		} else {
			msg = "the job[" + jobName + "] is not existed";
		}
		responseMsg.isOk();
		responseMsg.setMsg(msg);
		return responseMsg;

	}

	@Override
	public ResponseMsg<String> stop(String jobName) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (StringUtils.isNotBlank(jobName)) {
			if (!scheduleManager.isRunning(jobName)) {
				msg = "the job[" + jobName + "] is not running and don't stop";
			} else {
				scheduleManager.stop(DispatchType.newDispatchTypeByManual(), jobName);
				msg = "the job[" + jobName + "] have been requested to execute stop";
			}
		} else {
			msg = "the job[" + jobName + "] is not existed";
		}
		responseMsg.isOk();
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	public ResponseMsg<List<WorkerSnapshot>> getWorkerInfo(String jobName) {
		ResponseMsg<List<WorkerSnapshot>> responseMsg = createResponseMsg();
		List<WorkerSnapshot> result = null;
		if (StringUtils.isNotBlank(jobName)) {
			result = scheduleManager.getScheduleCache().getWorkerSnapshots(jobName);
		} else {
			result = Collections.emptyList();
		}
		responseMsg.setData(result);
		responseMsg.isOk();
		return responseMsg;
	}

	public AbstractMasterSchedulerManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(AbstractMasterSchedulerManager scheduleManager) {
		this.scheduleManager = scheduleManager;
	}
}
