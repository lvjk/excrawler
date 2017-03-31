package six.com.crawler.admin.service;

import java.util.List;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午3:25:23
 */
public interface JobService extends DownloadAndUploadService {

	/**
	 * 供控制层调用 模糊查询jobName+？ 默认将执行任务排在前列
	 * 
	 * @param ResponseMsg
	 *            返回前段的ResponseMsg
	 * @param jobName
	 *            模糊查询的jobName
	 * @param pageIndex
	 *            分页索引默认从0开始
	 * @param pageSize
	 *            分页大小
	 * @return
	 */
	public ResponseMsg<PageQuery<Job>> queryJobs(String jobName, int pageIndex, int pageSize);

	/**
	 * 获取任务运行状态
	 * 
	 * @param list
	 * @return
	 */
	public ResponseMsg<List<JobSnapshot>> getJobSnapshots(List<JobSnapshot> list);

	/**
	 * 通过任务名查询任务运行的历史JobSnapshot
	 * 
	 * @param jobName
	 * @return
	 */
	public ResponseMsg<List<JobSnapshot>> queryJobSnapshotsFromHistory(String jobName);

	/**
	 * 根据任务名查询job参数
	 * 
	 * @param jobName
	 * @return
	 */
	public ResponseMsg<List<JobParam>> queryJobParams(String jobName);
	
	/**
	 * 通过任务名称 查询 任务解析项
	 * 
	 * @param jobName
	 * @return
	 */
	public ResponseMsg<List<ExtractItem>> queryExtractItems(String jobName);

	/**
	 * 更新任务参数
	 * 
	 * @param version
	 * @param jobParamId
	 * @param name
	 * @param value
	 * @return
	 */
	public ResponseMsg<String> updateJobParam(int version, String jobParamId, String name, String value);


	/**
	 * 更新任务定时调度开关
	 * @param version
	 * @param name
	 * @param isScheduled
	 * @return
	 */
	public ResponseMsg<Integer> updateIsScheduled(int version, String name, int isScheduled);

	/**
	 * 更新任务定时触发时间
	 * @param version
	 * @param name
	 * @param cronTrigger
	 * @return
	 */
	public ResponseMsg<Integer> updateCronTrigger(int version, String name, String cronTrigger);

	/**
	 * 更新任务的下一个执行任务
	 * @param version
	 * @param name
	 * @param nextJobName
	 * @return
	 */
	public ResponseMsg<Integer> updateNextJobName(int version, String name, String nextJobName);

	/**
	 * 更新任务运行记录的状态
	 * @param version
	 * @param id
	 * @param status
	 * @return
	 */
	public ResponseMsg<Integer> updateJobSnapshotStatus(int version, String id, int status);

}
