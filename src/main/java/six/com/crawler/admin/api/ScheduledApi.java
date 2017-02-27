package six.com.crawler.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.service.ScheduledService;

/**
 *@author six    
 *@date 2016年1月14日 上午10:10:58  
 * 爬虫 调度 api
 */
@Controller
public class ScheduledApi extends BaseApi {
	
	@Autowired
	private ScheduledService scheduledService;
	
	public ScheduledService getScheduledService() {
		return scheduledService;
	}

	public void setScheduledService(ScheduledService scheduledService) {
		this.scheduledService = scheduledService;
	}
	
	@RequestMapping(value = "/crawler/scheduled/execute/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> execute(@PathVariable("jobHostNode") String jobHostNode,
			@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.execute(jobHostNode, jobName);
		msg.setData(result);
		return msg;
	}
	
	@RequestMapping(value = "/crawler/scheduled/assistExecute/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> assistExecute(
			@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.assistExecute(jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/scheduled/suspend/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> suspend(@PathVariable("jobHostNode") String jobHostNode,
			@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.suspend(jobHostNode, jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/scheduled/goon/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> goon(@PathVariable("jobHostNode") String jobHostNode,
			@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.goOn(jobHostNode, jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/scheduled/stop/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> stop(@PathVariable("jobHostNode") String jobHostNode,
			@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = scheduledService.stop(jobHostNode, jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/scheduled/scheduled/{jobName}/{isScheduled}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> scheduled(@PathVariable("jobName") String jobName,
			@PathVariable("isScheduled") int isScheduled) {
		ResponseMsg<String> responseMsg = new ResponseMsg<>();
		String msg = scheduledService.scheduled(jobName);
		responseMsg.setMsg(msg);
		return responseMsg;
	}

}
