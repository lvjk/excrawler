package six.com.crawler.dao.provider;

import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.JobDao;
import six.com.crawler.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月23日 下午5:46:33
 */
public class JobDaoProvider extends BaseProvider {

	
//	public String totalNodeJobInfo(String localNode){
//		StringBuilder sql = new StringBuilder();
//		sql.append("select  a.totalJobSize totalJobSize,"
//				        + " b.totalScheduleJobSize totalScheduleJobSize,"
//				        + " c.totalNoScheduleJobSize totalNoScheduleJobSize "
//				        + " from("
//				        + "				select count(1) totalJobSize,localNode"
//				        + "             from "+JobDao.TABLE_NAME+" where localNode=#{localNode})a "
//				        + " left join ("
//				        + "				select count(1) totalScheduleJobSize,localNode "
//				        + "				from "+JobDao.TABLE_NAME+" where  localNode=#{localNode} and isScheduled=1 )b "
//				        + " on a.localNode=b.localNode "
//				        + " left join("
//				        + "				select count(1) totalNoScheduleJobSize,localNode "
//				        + "             from "+JobDao.TABLE_NAME+" where  localNode=#{localNode} and isScheduled=0)c  "
//				        + " on b.localNode=c.localNode");
//		return sql.toString();
//		
//	}
	
	public String query(Map<String, Object> parameters) {
		StringBuilder sql = new StringBuilder();
		sql.append("select `name`,"
				+ "   nextJobName,"
				+ "   level,"
				+ " designatedNodeName,"
				+ "needNodes,"
				+ "isScheduled,"
				+ "cronTrigger,"
				+ "workFrequency,"
				+ "workerClass,"
				+ "queueName,"
				+ "`user`,"
				+ "`describe`,"
				+ "`version`");
		sql.append(" from "+JobDao.TABLE_NAME);
		buildParameter(sql, parameters);
		sql.append(" order by `level` asc,`name`");
		return sql.toString();
	}
	

	public String pageQuery(Map<String, Object> queryParams) {
		String sql="select b.totalSize,a.* "
				+ " from("
				+ "		select `name`,"
				+ "       nextJobName,"
				+ "			  `level`,"
				+ "			designatedNodeName,"
				+ "         needNodes,"
				+ "       isScheduled,"
				+ "       cronTrigger,"
				+ "     workFrequency,"
				+ "       workerClass,"
				+ "         queueName,"
				+ "              user,"
				+ "         `describe`,"
				+ "			`version` "
				+ "       from "+JobDao.TABLE_NAME
				+ "      where `name` like concat(#{name},'%')"
				+ "      order by `name` asc ) a,"
				+ "   (select FOUND_ROWS() totalSize)b limit #{start},#{end}";
		return sql;
	}
	
	public String save(Job job) {
		String columns = "`name`,"
				+ "nextJobName,"
				+ "level,"
				+ "designatedNodeName,"
				+ "needNodes,"
				+ "isScheduled,"
				+ "cronTrigger,"
				+ "workFrequency,"
				+ "workerClass,"
				+ "queueName,"
				+ "`user`,"
				+ "`describe`";
		
		String values = "#{name},"
				+ "#{nextJobName},"
				+ "#{level},"
				+ "#{designatedNodeName},"
				+ "#{needNodes},"
				+ "#{isScheduled},"
				+ "#{cronTrigger},"
				+ "#{workFrequency},"
				+ "#{workerClass},"
				+ "#{queueName},"
				+ "#{user},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(JobDao.TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}


	public String updateIsScheduled(Map<String, Object> queryParams) {
		SQL sql=new SQL();
		sql.UPDATE(JobDao.TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("isScheduled = #{isScheduled}");
		sql.WHERE("name = #{name} and version = #{version}");
		return sql.toString();
	}
	
	public String updateCronTrigger(Map<String, Object> queryParams) {
		SQL sql=new SQL();
		sql.UPDATE(JobDao.TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("cronTrigger = #{cronTrigger}");
		sql.WHERE("name = #{name} and version = #{version}");
		return sql.toString();
	}
}
