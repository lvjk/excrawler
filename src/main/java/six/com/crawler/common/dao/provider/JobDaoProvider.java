package six.com.crawler.common.dao.provider;

import java.util.Map;


import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.common.dao.JobDao;
import six.com.crawler.common.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月23日 下午5:46:33
 */
public class JobDaoProvider extends BaseProvider {

	
	public String totalNodeJobInfo(String localNode){
		StringBuilder sql = new StringBuilder();
		sql.append("select  a.totalJobSize totalJobSize,"
				        + " b.totalScheduleJobSize totalScheduleJobSize,"
				        + " c.totalNoScheduleJobSize totalNoScheduleJobSize "
				        + " from("
				        + "				select count(1) totalJobSize,localNode"
				        + "             from "+JobDao.TABLE_NAME+" where localNode=#{localNode})a "
				        + " left join ("
				        + "				select count(1) totalScheduleJobSize,localNode "
				        + "				from "+JobDao.TABLE_NAME+" where  localNode=#{localNode} and isScheduled=1 )b "
				        + " on a.localNode=b.localNode "
				        + " left join("
				        + "				select count(1) totalNoScheduleJobSize,localNode "
				        + "             from "+JobDao.TABLE_NAME+" where  localNode=#{localNode} and isScheduled=0)c  "
				        + " on b.localNode=c.localNode");
		return sql.toString();
		
	}
	
	public String query(Map<String, Object> parameters) {
		StringBuilder sql = new StringBuilder();
		sql.append("select `name`,"
				+ " localNode,"
				+ "   level,"
				+ "workFrequency,"
				+ "isScheduled,"
				+ "needNodes,"
				+ "cronTrigger,"
				+ "workerClass,"
				+ "queueName,"
				+ "`user`,"
				+ "`describe`");
		sql.append(" from "+JobDao.TABLE_NAME);
		buildParameter(sql, parameters);
		sql.append(" order by `level` asc,`name`");
		return sql.toString();
	}
	
	public String save(Job job) {
		String columns = "`name`,"
				+ "localNode,"
				+ "level,"
				+ "workFrequency,"
				+ "isScheduled,"
				+ "needNodes,"
				+ "cronTrigger,"
				+ "workerClass,"
				+ "queueName,"
				+ "`user`,"
				+ "`describe`";
		
		String values = "#{name},"
				+ "#{localNode},"
				+ "#{level},"
				+ "#{workFrequency},"
				+ "#{isScheduled},"
				+ "#{needNodes},"
				+ "#{cronTrigger},"
				+ "#{workerClass},"
				+ "#{queueName},"
				+ "#{user},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(JobDao.TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}

	public String update(Job job) {
		SQL sql=new SQL();
		sql.UPDATE(JobDao.TABLE_NAME);
		sql.SET("`localNode` = #{localNode}");
		sql.SET("`level` = #{level}");
		sql.SET("workFrequency = #{workFrequency}");
		sql.SET("isScheduled = #{isScheduled}");
		sql.SET("needNodes = #{needNodes}");
		sql.SET("cronTrigger = #{cronTrigger}");
		sql.SET("workerClass = #{workerClass}");
		sql.SET("queueName = #{queueName}");
		sql.SET("`user` = #{user}");
		sql.SET("`describe` = #{describe}");
		sql.WHERE("name = #{name}");
		return sql.toString();
	}
}
