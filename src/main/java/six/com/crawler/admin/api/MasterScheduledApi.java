package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.common.service.MasterScheduledService;

/**
 * @author six
 * @date 2016年1月14日 上午10:10:58 爬虫 调度 api
 */
@Controller
public class MasterScheduledApi extends BaseApi {

	@Autowired
	private MasterScheduledService scheduledService;

	public MasterScheduledService getScheduledService() {
		return scheduledService;
	}

	public void setScheduledService(MasterScheduledService scheduledService) {
		this.scheduledService = scheduledService;
	}

	@RequestMapping(value = "/crawler/master/scheduled/execute/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> execute(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.execute(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/master/scheduled/suspend/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> suspend(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.suspend(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/master/scheduled/goon/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> goon(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.goOn(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/master/scheduled/stop/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> stop(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.stop(jobName);
		msg.setMsg(result);
		return msg;
	}
	
	@RequestMapping(value = "/crawler/master/scheduled/end/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> end(@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.end(jobName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/master/scheduled/scheduled/{jobName}/{isScheduled}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> scheduled(@PathVariable("jobName") String jobName,
			@PathVariable("isScheduled") int isScheduled) {
		ResponseMsg<String> responseMsg = new ResponseMsg<>();
		String msg = scheduledService.scheduled(jobName);
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@RequestMapping(value = "/crawler/master/scheduled/getWorkerInfo/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<WorkerSnapshot>> getWorkerInfo(@PathVariable("jobName") String jobName) {
		ResponseMsg<List<WorkerSnapshot>> responseMsg = new ResponseMsg<>();
		List<WorkerSnapshot> result = scheduledService.getWorkerInfo(jobName);
		responseMsg.setData(result);
		return responseMsg;
	}

}
