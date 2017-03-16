package six.com.crawler.admin.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.service.WorkerScheduledService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 下午12:24:19
 */
@Controller
public class WorkerScheduledApi extends BaseApi {

	@Autowired
	private WorkerScheduledService scheduledService;

	public WorkerScheduledService getScheduledService() {
		return scheduledService;
	}

	public void setScheduledService(WorkerScheduledService scheduledService) {
		this.scheduledService = scheduledService;
	}

	@RequestMapping(value = "/crawler/worker/scheduled/execute/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> execute(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg =createResponseMsg();
		String result = scheduledService.execute(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/worker/scheduled/suspend/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> suspend(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = scheduledService.suspend(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/worker/scheduled/goon/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> goon(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = scheduledService.goOn(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/worker/scheduled/stop/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> stop(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = scheduledService.stop(jobName);
		msg.setMsg(result);
		return msg;
	}
}
