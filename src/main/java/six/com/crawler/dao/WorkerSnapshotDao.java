package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.provider.WorkerSnapshotDaoProvider;
import six.com.crawler.entity.WorkerSnapshot;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月16日 下午1:33:32 
*/
public interface WorkerSnapshotDao extends BaseDao{


	String TABLE_NAME="ex_crawler_platform_job_worker_snapshot";
	
	@SelectProvider(type = WorkerSnapshotDaoProvider.class, method = "query")
	public List<WorkerSnapshot> query(String jobSnapshotId);
	
	@InsertProvider(type = WorkerSnapshotDaoProvider.class, method = "save")
	public int save(WorkerSnapshot jobSnapshot);
	
	@InsertProvider(type = WorkerSnapshotDaoProvider.class, method = "batchSave")
	public void batchSave(@Param(BATCH_SAVE_PARAM)List<WorkerSnapshot> jobSnapshot);

}
