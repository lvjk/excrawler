package six.com.crawler.admin.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import six.com.crawler.admin.service.JobService;
import six.com.crawler.admin.vo.RefreshJobSnapshotVo;
import six.com.crawler.entity.JobSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月18日 下午3:59:17
 */
@Controller
public class JobSnapshotApi {

	@Autowired
	private JobService jobService;

	public JobService getJobService() {
		return jobService;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	@MessageMapping("/refreshJobSnapshot")
	@SendTo("/topic/job/jobSnapshot")
	public ResponseMsg<List<JobSnapshot>> refreshJobSnapshot(RefreshJobSnapshotVo refreshJobSnapshotVo) {
		String[] jobNames = StringUtils.split(refreshJobSnapshotVo.getJobNames(), ",");
		String[] workSpaceNames = StringUtils.split(refreshJobSnapshotVo.getWorkSpaceNames(), ",");
		ResponseMsg<List<JobSnapshot>> msg = jobService.getJobSnapshots(jobNames, workSpaceNames);
		return msg;
	}
}
