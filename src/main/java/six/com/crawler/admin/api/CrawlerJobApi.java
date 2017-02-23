package six.com.crawler.admin.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.entity.DoneInfo;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.QueueInfo;
import six.com.crawler.common.service.JobService;

/**
 * @author six
 * @date 2016年5月31日 下午2:56:54 爬虫 Job 任务 api
 */
@Controller
public class CrawlerJobApi extends BaseApi {

	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/crawler/job/getDefault", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Job>> getDefaultJobs() {
		ResponseMsg<List<Job>> msg = new ResponseMsg<>();
		List<Job> result = jobService.defaultQuery();
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/query/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Job>> query(@PathVariable("jobName") String jobName) {
		ResponseMsg<List<Job>> msg = new ResponseMsg<>();
		List<Job> result = jobService.fuzzyQuery(jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/save", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Boolean> addJob(Job job) {
		return null;
	}

	@RequestMapping(value = "/crawler/job/queryjobinfo/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Map<String, Object>> queryJobInfo(@PathVariable("jobName") String jobName) {
		ResponseMsg<Map<String, Object>> msg = new ResponseMsg<>();
		Map<String, Object> result = jobService.queryJobInfo(jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/jobSnapshot/history/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<JobSnapshot>> queryHistoryJobActivityInfo(@PathVariable("jobName") String jobName) {
		ResponseMsg<List<JobSnapshot>> msg = new ResponseMsg<>();
		List<JobSnapshot> result = jobService.queryJobSnapshotsFromHistory(jobName);
		msg.setData(result);
		return msg;
	}

	/**
	 * 向前段推送 job的 活动信息数据
	 * 
	 * @param jobNameList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@MessageMapping("/jobSnapshot")
	@SendTo("/topic/job/jobSnapshot")
	public ResponseMsg<List<JobSnapshot>> jobSnapshot(List<Object> list) {
		ResponseMsg<List<JobSnapshot>> msg = new ResponseMsg<>();
		List<JobSnapshot> result = new ArrayList<>();
		if (list != null) {
			JobSnapshot jobSnapshot=null;
			for (Object ob : list) {
				if (ob instanceof Job) {
					Job job = (Job) ob;
					jobSnapshot = jobService.getJobSnapshotFromRegisterCenter(job.getHostNode(),
							job.getName(),job.getQueueName());
				} else {
					Map<String, String> map = (Map<String, String>) ob;
					jobSnapshot = jobService.getJobSnapshotFromRegisterCenter(
							map.get("hostNode"),
							map.get("name"),
							map.get("queueName"));
				}
				if(null!=jobSnapshot){
					result.add(jobSnapshot);
				}
			}
		}
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/execute/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> execute(@PathVariable("jobHostNode")String jobHostNode,@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.execute(jobHostNode,jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/suspend/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> suspend(@PathVariable("jobHostNode")String jobHostNode,@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.suspend(jobHostNode,jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/goon/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> goon(@PathVariable("jobHostNode")String jobHostNode,@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.goOn(jobHostNode,jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/stop/{jobHostNode}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> stop(@PathVariable("jobHostNode")String jobHostNode,@PathVariable("jobName") String jobName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.stop(jobHostNode,jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/scheduled/{jobName}/{isScheduled}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> scheduled(@PathVariable("jobName") String jobName,
			@PathVariable("isScheduled") int isScheduled) {
		ResponseMsg<String> responseMsg = new ResponseMsg<>();
		String msg = jobService.scheduled(jobName);
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@RequestMapping(value = "/crawler/job/getLoclaAll", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Job>> getLoclaAllJobs() {
		return null;
	}

	@RequestMapping(value = "/crawler/job/queues", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<QueueInfo>> getJobQueues() {
		ResponseMsg<List<QueueInfo>> msg = new ResponseMsg<>();
		List<QueueInfo> result = jobService.getJobQueueInfos();
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/queue/repair/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> repairQueue(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.repairQueue(queueName);
		msg.setMsg(result);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/queue/clean/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> cleanQueue(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.cleanQueue(queueName);
		msg.setMsg(result);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/queue/done", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<DoneInfo>> getQueueDones() {
		ResponseMsg<List<DoneInfo>> msg = new ResponseMsg<>();
		List<DoneInfo> result = jobService.getQueueDones();
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/queue/done/clean/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> cleanQueueDone(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = jobService.cleanQueueDones(queueName);
		msg.setMsg(result);
		msg.setData(result);
		return msg;
	}

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}
}
