package six.com.crawler.service;

import java.util.List;
import java.util.Map;

import six.com.crawler.api.ResponseMsg;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午3:25:23
 */
public interface JobService extends DownloadAndUploadService {

	
	
	List<Job> queryIsScheduled();
	/**
	 * 供控制层调用 模糊查询jobName+？
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
	public void queryJobs(ResponseMsg<PageQuery<Job>> responseMsg, String jobName, int pageIndex, int pageSize);

	/**
	 * 通过参数查询 job jobName String 任务名字 nodeName String 节点名字 isTrigger int 0 or 1
	 * 是否开启调度
	 * 
	 * @param parameterMap
	 * @return
	 */
	public Job get(String jobName);

	/**
	 * 查询job相关的所有信息
	 * 
	 * @param jobName
	 * @return
	 */
	public Map<String, Object> queryJobInfo(String jobName);

	/**
	 * 通过参数parameters 查询
	 * 
	 * @param parameterMap
	 * @return
	 */
	public List<Job> query(Map<String, Object> parameters);

	/**
	 * 通过任务名字查询最后一次 JobSnapshot
	 * 
	 * @param jobNameListStr
	 *            jobName1;jobName2;jobName3;
	 * @return
	 */
	public JobSnapshot queryLastJobSnapshotFromHistory(String excludeJobSnapshotId, String jobName);

	/**
	 * 通过任务名字查询历史 JobSnapshot
	 * 
	 * @param jobNameListStr
	 *            jobName1;jobName2;jobName3;
	 * @return
	 */
	public List<JobSnapshot> queryJobSnapshotsFromHistory(String jobName);

	public List<JobSnapshot> getJobSnapshots(List<JobSnapshot> list);

	public void saveJobSnapshot(JobSnapshot jobSnapshot);


	public void updateJobSnapshot(JobSnapshot jobSnapshot);
	
	/**
	 * 通过任务名字查询任务正在运行活动信息
	 * 
	 * @param jobNameListStr
	 *            jobName1;jobName2;jobName3;
	 * @return
	 */
	public List<WorkerSnapshot> getWorkSnapshotsFromRegisterCenter(String jobName);

	/**
	 * 更新job的 工作快照 快照至注册中心
	 * 
	 * @param jobActivityInfo
	 */
	public void updateWorkSnapshotToRegisterCenter(WorkerSnapshot workerSnapshot, boolean isSaveErrMsg);

	/**
	 * 查询job参数
	 * 
	 * @return
	 */
	public List<JobParam> queryJobParams(String jobName);

	/**
	 * 通过job name 查询 解析项
	 * 
	 * @param parameterMap
	 * @return
	 */
	public List<ExtractItem> queryExtractItems(String jobName);
	
	public void updateIsScheduled(ResponseMsg<Integer> responseMsg,int version, String name, int isScheduled);
	
	public void updateCronTrigger(ResponseMsg<Integer> responseMsg,int version,String name,String cronTrigger);
	
	public void updateNextJobName(ResponseMsg<Integer> responseMsg,int version,String name,String nextJobName);
	
	public void updateJobSnapshotStatus(ResponseMsg<Integer> responseMsg,int version, String id, int status);
	
}
