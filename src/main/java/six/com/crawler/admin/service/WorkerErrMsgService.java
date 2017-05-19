package six.com.crawler.admin.service;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.entity.WorkerErrMsg;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午1:21:01
 */
public interface WorkerErrMsgService {

	/**
	 * 分页查询指定任务(jobName)运行批次(jobSnapshotId)的异常信息
	 * @param jobName       
	 * @param jobSnapshotId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public ResponseMsg<PageQuery<WorkerErrMsg>> query(String jobName,String jobSnapshotId,int pageIndex);
}
