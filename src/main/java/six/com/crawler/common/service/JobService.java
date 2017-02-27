package six.com.crawler.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.common.entity.DoneInfo;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.QueueInfo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午3:25:23
 */
public interface JobService {

	
	/**
	 * 统计节点任务数据信息
	 * @return
	 */
	public Node totalNodeJobInfo(String nodeName);
	/**
	 * 通过参数查询 job jobName String 任务名字 nodeName String 节点名字 isTrigger int 0 or 1
	 * 是否开启调度
	 * 
	 * @param parameterMap
	 * @return
	 */
	public Job queryByName(String jobName);

	/**
	 * 查询job相关的所有信息
	 * 
	 * @param jobName
	 * @return
	 */
	public Map<String, Object> queryJobInfo(String jobName);
	
	/**
	 * 通过参数查询 job jobName String 任务名字 nodeName String 节点名字 isTrigger int 0 or 1
	 * 是否开启调度
	 * 
	 * @param parameterMap
	 * @return
	 */
	public List<Job> fuzzyQuery(String jobName);

	/**
	 * 通过参数parameters 查询
	 * 
	 * @param parameterMap
	 * @return
	 */
	public List<Job> query(Map<String, Object> parameters);

	
	/**
	 * 导出 JobSnapshot 报告 
	 * @param jobName
	 */
	public void reportJobSnapshot(String nodeName,String jobName);
	
	
	
	/**
	 * 通过任务名字查询最后一次 JobSnapshot
	 * 
	 * @param jobNameListStr
	 *            jobName1;jobName2;jobName3;
	 * @return
	 */
	public JobSnapshot queryLastJobSnapshotFromHistory(String jobName);
	/**
	 * 通过任务名字查询历史 JobSnapshot
	 * 
	 * @param jobNameListStr
	 *            jobName1;jobName2;jobName3;
	 * @return
	 */
	public List<JobSnapshot> queryJobSnapshotsFromHistory(String jobName);
	
	
	public JobSnapshot getJobSnapshotFromRegisterCenter(String nodeName, String jobName,String queueName);
	/**
	 * 通过job name获取 注册中心 JobSnapshot
	 * @param jobName
	 * @return
	 */
	public JobSnapshot getJobSnapshotFromRegisterCenter(String nodeName,String jobName) ;
	
	/**
	 * 注册 JobSnapshot 快照至注册中心
	 * @param jobActivityInfo
	 */
	public void registerJobSnapshotToRegisterCenter(JobSnapshot jobSnapshot);
	/**
	 * 更新 JobSnapshot 快照至注册中心
	 * @param jobActivityInfo
	 */
	public void updateJobSnapshotToRegisterCenter(JobSnapshot jobSnapshot);
	
	
	/**
	 * 删除注册中心的 JobSnapshot
	 * @param nodeName
	 * @param jobName
	 */
	public void delJobSnapshotFromRegisterCenter(String nodeName,String jobName);
	
	
	/**
	 * 通过任务名字查询任务正在运行活动信息
	 * 
	 * @param jobNameListStr
	 *            jobName1;jobName2;jobName3;
	 * @return
	 */
	public List<WorkerSnapshot> getWorkSnapshotsFromRegisterCenter(String nodeName,String jobName);
	
	/**
	 * 更新job的 工作快照 快照至注册中心
	 * @param jobActivityInfo
	 */
	public void updateWorkSnapshotToRegisterCenter(WorkerSnapshot workerSnapshot,boolean isSaveErrMsg);
	


	/**
	 * 默认查询当前节点job
	 * 
	 * @return
	 */
	public List<Job> defaultQuery();
	
	
	/**
	 * 获取所有任务队列信息
	 * @return
	 */
	public List<QueueInfo> getJobQueueInfos();
	
	
	/**
	 * 获取所有任务处理过信息
	 * @return
	 */
	public List<DoneInfo> getQueueDones();
	
	/**
	 * 移除指定任务处理过信息
	 * @return
	 */
	public String cleanQueueDones(String queueName);
	
	
	/**
	 * 通过 queueName 清除指定任务队列
	 * @param queueName
	 * @return
	 */
	public String cleanQueue(String queueName);
	
	/**
	 * 通过 queueName 修復指定任务队列
	 * @param queueName
	 * @return
	 */
	public String repairQueue(String queueName);

	/**
	 * 查询job参数
	 * 
	 * @return
	 */
	public List<JobParam> queryJobParams(String jobName);

	/**
	 * 更新job 状态为 state
	 * 
	 * @param job
	 * @param state
	 */
	public void update(Job job);



	/**
	 * 通过job name 查询 解析项
	 * 
	 * @param parameterMap
	 * @return
	 */
	public List<ExtractItem> queryPaserItem(String jobName);
	
	
	
	public String uploadJobProfile(MultipartFile jobProfile);
	

	
}
