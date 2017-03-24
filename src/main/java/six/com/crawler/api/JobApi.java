package six.com.crawler.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.service.JobService;

/**
 * @author six
 * @date 2016年5月31日 下午2:56:54 爬虫 Job 任务 api
 */
@Controller
public class JobApi extends BaseApi {

	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/crawler/job/query/{pageIndex}/{pageSize}/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<PageQuery<Job>> queryJobs(@PathVariable("pageIndex") int pageIndex,
			@PathVariable("pageSize") int pageSize, @PathVariable("jobName") String jobName) {
		ResponseMsg<PageQuery<Job>> responseMsg = createResponseMsg();
		jobService.queryJobs(responseMsg, jobName, pageIndex, pageSize);
		return responseMsg;
	}

	@RequestMapping(value = "/crawler/job/save", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Boolean> addJob(Job job) {
		return null;
	}

	@RequestMapping(value = "/crawler/job/queryjobinfo/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Map<String, Object>> queryJobInfo(@PathVariable("jobName") String jobName) {
		ResponseMsg<Map<String, Object>> msg = createResponseMsg();
		Map<String, Object> result = jobService.queryJobInfo(jobName);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/getHistoryJobSnapshot/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<JobSnapshot>> getHistoryJobSnapshot(@PathVariable("jobName") String jobName) {
		ResponseMsg<List<JobSnapshot>> msg = createResponseMsg();
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
	@MessageMapping("/jobSnapshot")
	@SendTo("/topic/job/jobSnapshot")
	public ResponseMsg<List<JobSnapshot>> jobSnapshot(List<Map<String, String>> list) {
		ResponseMsg<List<JobSnapshot>> msg = createResponseMsg();
		List<JobSnapshot> result = jobService.getJobSnapshotFromRegisterCenter(list);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/getLoclaAll", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Job>> getLoclaAllJobs() {
		return null;
	}
	
	@RequestMapping(value = "/crawler/job/updateIsScheduled", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateIsScheduled(
			@RequestParam("version")int version,
			@RequestParam("name")String name,
			@RequestParam("isScheduled")int isScheduled) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
		jobService.updateIsScheduled(responseMsg,version,name, isScheduled);
		return responseMsg;
	}
	
	@RequestMapping(value = "/crawler/job/updateCronTrigger", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateCronTrigger(
			@RequestParam("version")int version,
			@RequestParam("name")String name,
			@RequestParam("cronTrigger")String cronTrigger) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
		jobService.updateCronTrigger(responseMsg,version,name, cronTrigger);
		return responseMsg;
	}
	
	@RequestMapping(value = "/crawler/job/updateNextJobName", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateNextJobName(
			@RequestParam("version")int version,
			@RequestParam("name")String name,
			@RequestParam("nextJobName")String nextJobName) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
		jobService.updateNextJobName(responseMsg,version,name,nextJobName);
		return responseMsg;
	}
	
	@RequestMapping(value = "/crawler/job/updateJobSnapshotStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateJobSnapshotStatus(
			@RequestParam("version")int version,
			@RequestParam("id")String id,
			@RequestParam("status")int status) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
		jobService.updateJobSnapshotStatus(responseMsg, version, id, status);
		return responseMsg;
	}
	
	@RequestMapping(value = "/crawler/job/upload/profile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = uploadFile(jobService, multipartFile);
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@RequestMapping(value = "/crawler/job/download/profile/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("jobName") String jobName) {
		return downloadFile(jobService, jobName);
	}

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}
}
