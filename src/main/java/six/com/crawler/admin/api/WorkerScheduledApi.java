package six.com.crawler.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.api.annotation.OnlyVisitByMaster;
import six.com.crawler.common.service.WorkerScheduledService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午3:38:27
 */

@Controller
public class WorkerScheduledApi extends BaseApi {

	@Autowired
	private WorkerScheduledService workerScheduledService;

	@OnlyVisitByMaster
	@RequestMapping(value = "/crawler/worker/scheduled/execute/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Boolean> execute(@PathVariable("jobName") String jobName) {
		ResponseMsg<Boolean> msg = createResponseMsg();
		boolean result = workerScheduledService.execute(jobName);
		msg.setData(result);
		return msg;
	}

	@OnlyVisitByMaster
	@RequestMapping(value = "/crawler/worker/scheduled/suspend/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Boolean> suspend(@PathVariable("jobName") String jobName) {
		ResponseMsg<Boolean> msg = createResponseMsg();
		Boolean result = workerScheduledService.suspend(jobName);
		msg.setData(result);
		return msg;
	}

	@OnlyVisitByMaster
	@RequestMapping(value = "/crawler/worker/scheduled/goOn/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Boolean> goOn(@PathVariable("jobName") String jobName) {
		ResponseMsg<Boolean> msg = createResponseMsg();
		Boolean result = workerScheduledService.goOn(jobName);
		msg.setData(result);
		return msg;
	}

	@OnlyVisitByMaster
	@RequestMapping(value = "/crawler/worker/scheduled/stop/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Boolean> stop(@PathVariable("jobName") String jobName) {
		ResponseMsg<Boolean> msg = createResponseMsg();
		Boolean result = workerScheduledService.stop(jobName);
		msg.setData(result);
		return msg;
	}
	
	@OnlyVisitByMaster
	@RequestMapping(value = "/crawler/worker/scheduled/stopAll", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Boolean> stopAll() {
		ResponseMsg<Boolean> msg = createResponseMsg();
		Boolean result = workerScheduledService.stopAll();
		msg.setData(result);
		return msg;
	}
}
