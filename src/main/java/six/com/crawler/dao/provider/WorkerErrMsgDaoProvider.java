package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;


import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.BaseDao;
import six.com.crawler.dao.TableNames;
import six.com.crawler.dao.WorkerErrMsgDao;
import six.com.crawler.entity.WorkerErrMsg;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月16日 下午3:17:51 
*/
public class WorkerErrMsgDaoProvider extends BaseProvider{
	
	String queryColumns="id,"
			+ "jobSnapshotId,"
			+ "jobName,"
			+ "workerName,"
			+ "DATE_FORMAT(startTime,'%Y-%m-%d %H:%i:%s') startTime,"
			+ "type,"
			+ "msg";
	private String saveColumns="jobSnapshotId,jobName,workerName,startTime,type,msg";
	
	public String query(Map<String, Object> map) {
		SQL sql=new SQL();
		sql.SELECT(queryColumns);
		sql.FROM(TableNames.JOB_WORKER_SNAPSHOT_ERRMSG_TABLE_NAME);
		Object ob=map.get(WorkerErrMsgDao.QUERY_PARAM_JOBSNAPSHOTID);
		if(null!=ob){
			sql.WHERE(WorkerErrMsgDao.QUERY_PARAM_JOBSNAPSHOTID+"=#{"+WorkerErrMsgDao.QUERY_PARAM_JOBSNAPSHOTID+"}");
		}
		ob=map.get(WorkerErrMsgDao.QUERY_PARAM_JOBNAME);
		if(null!=ob){
			sql.WHERE(WorkerErrMsgDao.QUERY_PARAM_JOBNAME+"=#{"+WorkerErrMsgDao.QUERY_PARAM_JOBNAME+"}");
		}
		ob=map.get(WorkerErrMsgDao.QUERY_PARAM_WORKERNAME);
		if(null!=ob){
			sql.WHERE(WorkerErrMsgDao.QUERY_PARAM_WORKERNAME+"=#{"+WorkerErrMsgDao.QUERY_PARAM_WORKERNAME+"}");
		}
		return sql.toString();
	}
	
	public String queryByJob(Map<String, Object> map){
		SQL sql=new SQL();
		sql.SELECT(queryColumns);
		sql.FROM(TableNames.JOB_WORKER_SNAPSHOT_ERRMSG_TABLE_NAME);
		Object ob=map.get(WorkerErrMsgDao.QUERY_PARAM_JOBSNAPSHOTID);
		if(null!=ob){
			sql.WHERE(WorkerErrMsgDao.QUERY_PARAM_JOBSNAPSHOTID+"=#{"+WorkerErrMsgDao.QUERY_PARAM_JOBSNAPSHOTID+"}");
		}
		ob=map.get(WorkerErrMsgDao.QUERY_PARAM_JOBNAME);
		if(null!=ob){
			sql.WHERE(WorkerErrMsgDao.QUERY_PARAM_JOBNAME+"=#{"+WorkerErrMsgDao.QUERY_PARAM_JOBNAME+"}");
		}
		return sql.toString();
	}
	
	public String pageQuery(Map<String, Object> queryParams) {
		String sql="select b.totalSize,a.* "
				+ " from("
				+ "		select "
				+queryColumns
				+ "       from "+TableNames.JOB_WORKER_SNAPSHOT_ERRMSG_TABLE_NAME
				+ "      where `jobName` =#{jobName} and `jobSnapshotId` =#{jobSnapshotId}"
				+ "      order by `startTime` desc) a,"
				+ "   (select FOUND_ROWS() totalSize)b limit #{start},#{end}";
		return sql;
	}
	
	public String save(WorkerErrMsg workerErrMsg) {
		String values="#{jobSnapshotId},"
				+ "#{jobName},"
				+ "#{workerName},"
				+ "#{startTime},"
				+ "#{msg}";
		SQL sql=new SQL();
		sql.INSERT_INTO(TableNames.JOB_WORKER_SNAPSHOT_ERRMSG_TABLE_NAME);
		sql.VALUES(saveColumns, values);
		return sql.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		List<WorkerErrMsg> workerErrMsgs = (List<WorkerErrMsg>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String values="(#{list["+INDEX_FLAG+"].jobSnapshotId},"
				+ "#{list["+INDEX_FLAG+"].jobName},"
				+ "#{list["+INDEX_FLAG+"].workerName},"
				+ "#{list["+INDEX_FLAG+"].startTime},"
				+ "#{list["+INDEX_FLAG+"].type},"
				+ "#{list["+INDEX_FLAG+"].msg})";
		StringBuilder sbd = new StringBuilder();  
		sbd.append("insert into ").append(TableNames.JOB_WORKER_SNAPSHOT_ERRMSG_TABLE_NAME);  
		sbd.append("(").append(saveColumns).append(") ");  
		sbd.append("values");  
		sbd.append(setBatchSaveSql(values,workerErrMsgs));
		return sbd.toString();
	
	}
}
