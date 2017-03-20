package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.BaseDao;
import six.com.crawler.dao.WorkerSnapshotDao;
import six.com.crawler.entity.WorkerSnapshot;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月16日 下午1:35:08 
*/
public class WorkerSnapshotDaoProvider extends BaseProvider{
	
	private String saveColumns="jobSnapshotId,"
			+ "`name`,"
			+ "localNode,"
			+ "jobName,"
			+ "startTime,"
			+ "endTime,"
			+ "totalProcessCount,"
			+ "totalResultCount,"
			+ "totalProcessTime,"
			+ "avgProcessTime,"
			+ "maxProcessTime,"
			+ "minProcessTime,"
			+ "errCount";
	public String query(String jobSnapshotId) {
		SQL sql=new SQL();
		String columns="jobSnapshotId,"
				+ "`name`,"
				+ "localNode,"
				+ "jobName,"
				+ "UNIX_TIMESTAMP(startTime)*1000 startTime,"
				+ "UNIX_TIMESTAMP(endTime)*1000 endTime,"
				+ "totalProcessCount,"
				+ "totalResultCount,"
				+ "totalProcessTime,"
				+ "avgProcessTime,"
				+ "maxProcessTime,"
				+ "minProcessTime,"
				+ "errCount";
		sql.SELECT(columns);
		sql.FROM(WorkerSnapshotDao.TABLE_NAME);
		sql.WHERE("jobSnapshotId=#{jobSnapshotId}");
		return sql.toString();
	}
	public String save(WorkerSnapshot workerSnapshot) {
		String values="#{jobSnapshotId},"
				+ "#{name},"
				+ "#{localNode},"
				+ "#{jobName},"
				+ "#{startTime},"
				+ "#{endTime},"
				+ "#{totalProcessCount},"
				+ "#{totalResultCount},"
				+ "#{totalProcessTime},"
				+ "#{avgProcessTime},"
				+ "#{maxProcessTime},"
				+ "#{minProcessTime},"
				+ "#{errCount}";
		SQL sql=new SQL();
		sql.INSERT_INTO(WorkerSnapshotDao.TABLE_NAME);
		sql.VALUES(saveColumns, values);
		return sql.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		List<WorkerSnapshot> workerSnapshots = (List<WorkerSnapshot>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String values="(#{list["+INDEX_FLAG+"].jobSnapshotId},"
				+ "#{list["+INDEX_FLAG+"].name},"
				+ "#{list["+INDEX_FLAG+"].localNode},"
				+ "#{list["+INDEX_FLAG+"].jobName},"
				+ "#{list["+INDEX_FLAG+"].startTime},"
				+ "#{list["+INDEX_FLAG+"].endTime},"
				+ "#{list["+INDEX_FLAG+"].totalProcessCount},"
				+ "#{list["+INDEX_FLAG+"].totalResultCount},"
				+ "#{list["+INDEX_FLAG+"].totalProcessTime},"
				+ "#{list["+INDEX_FLAG+"].avgProcessTime},"
				+ "#{list["+INDEX_FLAG+"].maxProcessTime},"
				+ "#{list["+INDEX_FLAG+"].minProcessTime},"
				+ "#{list["+INDEX_FLAG+"].errCount})";
		StringBuilder sbd = new StringBuilder();  
		sbd.append("insert into ").append(WorkerSnapshotDao.TABLE_NAME);  
		sbd.append("(").append(saveColumns).append(") ");  
		sbd.append("values");  
		sbd.append(setBatchSaveSql(values,workerSnapshots));
		return sbd.toString();
	}


}
