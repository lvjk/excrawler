package six.com.crawler.admin.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.service.JobService;
import six.com.crawler.admin.service.WorkerErrMsgService;
import six.com.crawler.admin.vo.RefreshJobSnapshotVo;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.entity.WorkerErrMsg;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月18日 下午3:59:17
 */
@Controller
public class JobSnapshotApi {

	@Autowired
	private JobService jobService;

	@Autowired
	private WorkerErrMsgService workerErrMsgService;

	@MessageMapping("/refreshJobSnapshot")
	@SendTo("/topic/job/jobSnapshot")
	public ResponseMsg<List<JobSnapshot>> refreshJobSnapshot(RefreshJobSnapshotVo refreshJobSnapshotVo) {
		String[] jobNames = StringUtils.split(refreshJobSnapshotVo.getJobNames(), ",");
		String[] workSpaceNames = StringUtils.split(refreshJobSnapshotVo.getWorkSpaceNames(), ",");
		ResponseMsg<List<JobSnapshot>> msg = jobService.getJobSnapshots(jobNames, workSpaceNames);
		return msg;
	}

	@RequestMapping(value = "/crawler/job/jobSnapshot/queryErrMsg", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<PageQuery<WorkerErrMsg>> queryErrMsg(@RequestParam("jobName") String jobName,
			@RequestParam("jobSnapshotId") String jobSnapshotId,
			@RequestParam("pageIndex") int pageIndex) {
		return workerErrMsgService.query(jobName, jobSnapshotId,pageIndex);
	}

	public WorkerErrMsgService getWorkerErrMsgService() {
		return workerErrMsgService;
	}

	public void setWorkerErrMsgService(WorkerErrMsgService workerErrMsgService) {
		this.workerErrMsgService = workerErrMsgService;
	}

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}
}
