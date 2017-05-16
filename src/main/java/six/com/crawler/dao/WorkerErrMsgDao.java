package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.provider.WorkerErrMsgDaoProvider;
import six.com.crawler.entity.WorkerErrMsg;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月16日 下午3:17:08 
*/
public interface WorkerErrMsgDao extends BaseDao{

	String QUERY_PARAM_JOBSNAPSHOTID="jobSnapshotId";
	String QUERY_PARAM_JOBNAME="jobName";
	String QUERY_PARAM_WORKERNAME="workerName";
	
	@SelectProvider(type = WorkerErrMsgDaoProvider.class, method = "query")
	public List<WorkerErrMsg> query(@Param(QUERY_PARAM_JOBSNAPSHOTID)String jobSnapshotId,
			@Param(QUERY_PARAM_JOBNAME)String jobName,
			@Param(QUERY_PARAM_WORKERNAME)String workerName);
	
	/**
	 * 支持 name%模糊查询
	 * @param jobName
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@SelectProvider(type = WorkerErrMsgDaoProvider.class, method = "pageQuery")
	public List<WorkerErrMsg> pageQuery(@Param("name")String jobName, 
			@Param("jobSnapshotId")String jobSnapshotId,
			@Param("start")int pageIndex, 
			@Param("end")int pageSize);
	
	@InsertProvider(type = WorkerErrMsgDaoProvider.class, method = "save")
	public int save(WorkerErrMsg workerErrMsg);
	
	@InsertProvider(type = WorkerErrMsgDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM)List<WorkerErrMsg> list);
	

}
