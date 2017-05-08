package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.BaseDao;
import six.com.crawler.dao.JobSnapshotDao;
import six.com.crawler.entity.JobSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月24日 下午3:40:34
 */
public class JobSnapshotDaoProvider extends BaseProvider {

	String selectColumns = "id," 
					+ "`name`," 
					+ "DATE_FORMAT(startTime,'%Y-%m-%d %H:%i:%s') startTime,"
					+ "DATE_FORMAT(endTime,'%Y-%m-%d %H:%i:%s') endTime," 
					+ "`status`," 
					+ "downloadState,"
					+ "totalProcessCount," 
					+ "totalResultCount,"
					+ "totalProcessTime," 
					+ "avgProcessTime," 
					+ "maxProcessTime," 
					+ "minProcessTime," 
					+ "errCount,"
					+ "runtimeParamsJson,"
					+ "`version`";
	public String query(Map<String, Object> map) {
		SQL sql = new SQL();
		sql.SELECT(selectColumns);
		sql.FROM(JobSnapshotDao.TABLE_NAME);
		sql.WHERE("id=#{id} and name=#{name}");
		return sql.toString();
	}
	
	public String queryByJob(String jobName) {
		SQL sql = new SQL();
		sql.SELECT(selectColumns);
		sql.FROM(JobSnapshotDao.TABLE_NAME);
		sql.WHERE("name=#{jobName}");
		sql.ORDER_BY("startTime desc");
		return sql.toString();
	}
	
	public String queryLast(Map<String, Object> map) {
		SQL sql = new SQL();
		sql.SELECT(selectColumns);
		sql.FROM(JobSnapshotDao.TABLE_NAME);
		sql.WHERE("`name`=#{jobName}");
		sql.ORDER_BY("startTime desc limit 0,1");
		return sql.toString();
	}
	
	public String queryCurrentJob(Map<String, Object> map){
		SQL sql = new SQL();
		sql.SELECT(selectColumns);
		sql.FROM(JobSnapshotDao.TABLE_NAME);
		sql.WHERE("`name`=#{jobName} and date_format(startTime, '%Y%m%d') = date_format(NOW(), '%Y%m%d') and downloadState = 1 ");
		sql.ORDER_BY("startTime desc limit 0,1");
		return sql.toString();
	}

	public String save(JobSnapshot jobSnapshot) {
		String columns = "id," + "`name`," + "startTime," + "endTime," + "`status`,"
				+ "totalProcessCount," + "totalResultCount," + "totalProcessTime," + "avgProcessTime,"
				+ "maxProcessTime," + "minProcessTime," + "errCount,"+ "runtimeParamsJson";
		String values = "#{id}," + "#{name}," + "#{startTime}," + "#{endTime}," + "#{status},"
				+ "#{totalProcessCount}," + "#{totalResultCount}," + "#{totalProcessTime}," + "#{avgProcessTime},"
				+ "#{maxProcessTime}," + "#{minProcessTime}," + "#{errCount},"+ "#{runtimeParamsJson}";
		SQL sql = new SQL();
		sql.INSERT_INTO(JobSnapshotDao.TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		List<JobSnapshot> jobSnapshots = (List<JobSnapshot>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String columns = "id," + "`name`,"+ "startTime," + "endTime," + "`status`,"
				+ "totalProcessCount," + "totalResultCount," + "totalProcessTime," + "avgProcessTime,"
				+ "maxProcessTime," + "minProcessTime," + "errCount,"+ "runtimeParamsJson";
		String values = "(#{list[" + INDEX_FLAG + "].id}," + "#{list[" + INDEX_FLAG + "].name},"
				+ "#{list[" + INDEX_FLAG + "].startTime}," + "#{list[" + INDEX_FLAG
				+ "].endTime}," + "#{list[" + INDEX_FLAG + "].status}," + "#{list[" + INDEX_FLAG
				+ "].totalProcessCount}," + "#{list[" + INDEX_FLAG + "].totalResultCount}," + "#{list[" + INDEX_FLAG
				+ "].totalProcessTime}," + "#{list[" + INDEX_FLAG + "].avgProcessTime}," + "#{list[" + INDEX_FLAG
				+ "].maxProcessTime}," + "#{list[" + INDEX_FLAG + "].minProcessTime}," + "#{list[" + INDEX_FLAG
				+ "].errCount},#{list[" + INDEX_FLAG + "].runtimeParamsJson})";
		StringBuilder sbd = new StringBuilder();
		sbd.append("insert into ").append(JobSnapshotDao.TABLE_NAME);
		sbd.append("(").append(columns).append(") ");
		sbd.append("values");
		sbd.append(setBatchSaveSql(values, jobSnapshots));
		return sbd.toString();
	}
	
	public String update(JobSnapshot jobSnapshot) {
		SQL sql = new SQL();
		sql.UPDATE(JobSnapshotDao.TABLE_NAME);
		sql.SET("`name`=#{name}");
		sql.SET("`startTime`=#{startTime}");
		sql.SET("`endTime`=#{endTime}");
		sql.SET("`status`=#{status}");
		sql.SET("`totalProcessCount`=#{totalProcessCount}");
		sql.SET("`totalResultCount`=#{totalResultCount}");
		sql.SET("`totalProcessTime`=#{totalProcessTime}");
		sql.SET("`avgProcessTime`=#{avgProcessTime}");
		sql.SET("`maxProcessTime`=#{maxProcessTime}");
		sql.SET("`minProcessTime`=#{minProcessTime}");
		sql.SET("`errCount`=#{errCount}");
		sql.SET("`runtimeParamsJson`=#{runtimeParamsJson}");
		sql.WHERE("`id` = #{id} and `name` = #{name}");
		return sql.toString();
	}
	
	public String updateStatus(Map<String, Object> map) {
		SQL sql = new SQL();
		sql.UPDATE(JobSnapshotDao.TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("`status`=#{status}");
		sql.WHERE("`id` = #{id} and version = #{version}");
		return sql.toString();
	}
	
	public String updateDownloadStatus(Map<String, Object> map) {
		SQL sql = new SQL();
		sql.UPDATE(JobSnapshotDao.TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("`downloadState`=#{downloadState}");
		sql.WHERE("`id` = #{id} and version = #{version}");
		return sql.toString();
	}
}
