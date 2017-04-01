package six.com.crawler.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.entity.PageQuery;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.admin.service.WorkerErrMsgService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月31日 下午4:52:37
 */

@Controller
public class WorkerErrMsgApi extends BaseApi {

	@Autowired
	private WorkerErrMsgService workerErrMsgService;

	public WorkerErrMsgService getWorkerErrMsgService() {
		return workerErrMsgService;
	}

	public void setWorkerErrMsgService(WorkerErrMsgService workerErrMsgService) {
		this.workerErrMsgService = workerErrMsgService;
	}

	@RequestMapping(value = "/crawler/workerErrMsg/query/{pageIndex}/{pageSize}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<PageQuery<WorkerErrMsg>> querySites(@PathVariable("jobName") String jobName,
														   @PathVariable("jobSnapshotId") String jobSnapshotId,
														   @PathVariable("pageIndex") int pageIndex,
			                                               @PathVariable("pageSize") int pageSize) {
		return workerErrMsgService.query(jobName, jobSnapshotId, pageIndex, pageSize);
	}
}
