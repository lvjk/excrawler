package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.provider.JobSnapshotDaoProvider;
import six.com.crawler.entity.JobSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月24日 下午3:36:26
 */
public interface JobSnapshotDao extends BaseDao {

	String TABLE_NAME = "ex_crawler_platform_job_snapshot";

	/**
	 * 通过 jobSnapshotId 和 jobName 查询
	 * 
	 * @param jobSnapshotId
	 * @param jobName
	 * @return
	 */
	@SelectProvider(type = JobSnapshotDaoProvider.class, method = "query")
	public JobSnapshot query(@Param("id") String jobSnapshotId, @Param("name") String jobName);

	/**
	 * 通过任务名称查询 任务的运行记录 集合
	 * 
	 * @param jobName
	 * @return 任务的运行记录 集合
	 */
	@SelectProvider(type = JobSnapshotDaoProvider.class, method = "queryByJob")
	public List<JobSnapshot> queryByJob(String jobName);

	/**
	 * 通过任务名称查询 任务的最后一条运行记录
	 * 
	 * @param jobName
	 * @return 最后一条运行记录
	 */
	@SelectProvider(type = JobSnapshotDaoProvider.class, method = "queryLastEnd")
	public JobSnapshot queryLastEnd(@Param("jobName") String jobName,@Param("excludeId") String excludeId);
	
	
	/**
	 * 通过任务名称查询 任务的下载状态
	 * 
	 * @param jobName
	 * @return 任务的下载状态
	 */
	@SelectProvider(type = JobSnapshotDaoProvider.class, method = "queryCurrentJob")
	public JobSnapshot queryCurrentJob(@Param("jobName") String jobName);
	
	/**
	 * 保存任务运行记录
	 * 
	 * @param jobSnapshot
	 * @return 受影响数据的条数
	 */
	@InsertProvider(type = JobSnapshotDaoProvider.class, method = "save")
	public int save(JobSnapshot jobSnapshot);

	/**
	 * 更新任务运行记录
	 * 
	 * @param jobSnapshot
	 * @return 受影响数据的条数
	 */
	@InsertProvider(type = JobSnapshotDaoProvider.class, method = "update")
	public int update(JobSnapshot jobSnapshot);

	/**
	 * 更新任务运行记录的状态
	 * 
	 * @param id
	 * @param status
	 * @return 受影响数据的条数
	 */
	@InsertProvider(type = JobSnapshotDaoProvider.class, method = "updateStatus")
	public int updateStatus(@Param("version") int version, @Param("newVersion") int newVersion, @Param("id") String id,
			@Param("status") int status);
	
	/**
	 * 更新下载状态
	 * @param version
	 * @param newVersion
	 * @param id
	 * @param downloadState
	 * @return
	 */
	@InsertProvider(type = JobSnapshotDaoProvider.class, method = "updateDownloadStatus")
	public int updateDownloadStatus(@Param("version") int version, @Param("newVersion") int newVersion, @Param("id") String id,
			@Param("downloadState") int status);

}
