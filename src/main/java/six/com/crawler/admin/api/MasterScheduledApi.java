package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.service.MasterScheduledService;
import six.com.crawler.entity.WorkerSnapshot;

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
		return scheduledService.execute(jobName);
	}

	@RequestMapping(value = "/crawler/master/scheduled/suspend/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> suspend(@PathVariable("jobName") String jobName) {
		return scheduledService.suspend(jobName);
	}

	@RequestMapping(value = "/crawler/master/scheduled/goon/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> goon(@PathVariable("jobName") String jobName) {
		return scheduledService.goOn(jobName);
	}

	@RequestMapping(value = "/crawler/master/scheduled/stop/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> stop(@PathVariable("jobName") String jobName) {
		return scheduledService.stop(jobName);
	}

	@RequestMapping(value = "/crawler/master/scheduled/getWorkerInfo/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<WorkerSnapshot>> getWorkerInfo(@PathVariable("jobName") String jobName) {
		return scheduledService.getWorkerInfo(jobName);
	}

}
