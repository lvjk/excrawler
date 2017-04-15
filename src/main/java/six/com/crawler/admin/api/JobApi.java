package six.com.crawler.admin.api;

import java.util.ArrayList;
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

import six.com.crawler.admin.service.JobService;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.PageQuery;

/**
 * @author six
 * @date 2016年5月31日 下午2:56:54 爬虫 Job 任务 api
 */
@Controller
public class JobApi extends BaseApi {

	@Autowired
	private JobService jobService;


	@RequestMapping(value = "/crawler/job/query", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<PageQuery<Job>> queryJobs(@RequestParam("pageIndex") int pageIndex,
			@RequestParam("pageSize") int pageSize, @RequestParam("jobName") String jobName) {
		return jobService.queryJobs(jobName, pageIndex, pageSize);
	}

	@RequestMapping(value = "/crawler/job/save", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Boolean> addJob(Job job) {
		return null;
	}

	@RequestMapping(value = "/crawler/job/queryJobParams/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<JobParam>> queryJobParams(@PathVariable("jobName") String jobName) {
		return jobService.queryJobParams(jobName);
	}

	@RequestMapping(value = "/crawler/job/getHistoryJobSnapshot/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<JobSnapshot>> getHistoryJobSnapshot(@PathVariable("jobName") String jobName) {
		return jobService.queryJobSnapshotsFromHistory(jobName);
	}

	/**
	 * 向前段推送 job的 活动信息数据
	 * 
	 * @param jobNameList
	 * @return
	 */
	@MessageMapping("/jobSnapshot")
	@SendTo("/topic/job/jobSnapshot")
	public ResponseMsg<List<JobSnapshot>> getJobSnapshots(List<Map<String, String>> list) {
		ResponseMsg<List<JobSnapshot>> msg = null;
		if (null != list) {
			List<JobSnapshot> jobSnapshots = new ArrayList<>(list.size());
			JobSnapshot jobSnapshot = null;
			String name = null;
			String workSpaceName = null;
			for (Map<String, String> map : list) {
				name = map.get("name");
				workSpaceName = map.get("workSpaceName");
				jobSnapshot = new JobSnapshot();
				jobSnapshot.setName(name);
				jobSnapshot.setWorkSpaceName(workSpaceName);
				jobSnapshots.add(jobSnapshot);
			}
			msg = jobService.getJobSnapshots(jobSnapshots);
		}
		return msg;
	}

	@RequestMapping(value = "/crawler/job/updateIsScheduled", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateIsScheduled(@RequestParam("version") int version,
			@RequestParam("name") String name, @RequestParam("isScheduled") int isScheduled) {
		return jobService.updateIsScheduled(version, name, isScheduled);
	}

	@RequestMapping(value = "/crawler/job/updateCronTrigger", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateCronTrigger(@RequestParam("version") int version,
			@RequestParam("name") String name, @RequestParam("cronTrigger") String cronTrigger) {
		return jobService.updateCronTrigger(version, name, cronTrigger);
	}

	@RequestMapping(value = "/crawler/job/updateJobSnapshotStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<Integer> updateJobSnapshotStatus(@RequestParam("version") int version,
			@RequestParam("id") String id, @RequestParam("status") int status) {
		return jobService.updateJobSnapshotStatus(version, id, status);
	}

	@RequestMapping(value = "/crawler/job/updateJobParam", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> updateJobParam(@RequestParam("version") int version,
			@RequestParam("jobParamId") String jobParamId, @RequestParam("name") String name,
			@RequestParam("value") String value) {
		return jobService.updateJobParam(version, jobParamId, name, value);
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
