package six.com.crawler.common.dao.provider;

import java.util.Map;


import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.common.dao.JobDao;
import six.com.crawler.common.dao.JobSnapshotDao;
import six.com.crawler.common.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月23日 下午5:46:33
 */
public class JobDaoProvider extends BaseProvider {

	
	public String totalNodeJobInfo(String nodeName){
		StringBuilder sql = new StringBuilder();
		sql.append("select  a.totalJobSize totalJobSize,"
				        + " b.totalScheduleJobSize totalScheduleJobSize,"
				        + " c.totalNoScheduleJobSize totalNoScheduleJobSize "
				        + " from("
				        + "				select count(1) totalJobSize,hostNode"
				        + "             from "+JobDao.TABLE_NAME+" where hostNode=#{nodeName})a "
				        + " left join ("
				        + "				select count(1) totalScheduleJobSize,hostNode "
				        + "				from "+JobDao.TABLE_NAME+" where  hostNode=#{nodeName} and isScheduled=1 )b "
				        + " on a.hostNode=b.hostNode "
				        + " left join("
				        + "				select count(1) totalNoScheduleJobSize,hostNode "
				        + "             from "+JobDao.TABLE_NAME+" where  hostNode=#{nodeName} and isScheduled=0)c  "
				        + " on b.hostNode=c.hostNode");
		return sql.toString();
		
	}
	
	public String query(Map<String, Object> parameters) {
		StringBuilder sql = new StringBuilder();
		sql.append("select `name`,"
				+ " hostNode,"
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
		String columns = "select `name`,"
				+ " hostNode,"
				+ "   level,"
				+ "workFrequency,"
				+ "isScheduled,"
				+ "needNodes,"
				+ "cronTrigger,"
				+ "workerClass,"
				+ "queueName,"
				+ "`user`,"
				+ "`describe`";
		
		String values = "#{name},"
				+ "#{hostNode},"
				+ "#{level},"
				+ "#{workFrequency},"
				+ "#{isScheduled},"
				+ "#{needNodes},"
				+ "#{cronTrigger},"
				+ "#{workerClass},"
				+ "#{queueName}"
				+ "#{user},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(JobSnapshotDao.TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}

	public String update(Job job) {
		SQL sql=new SQL();
		sql.UPDATE(JobDao.TABLE_NAME);
		sql.SET("hostNode = #{hostNode}");
		sql.SET("level = #{level}");
		sql.SET("workFrequency = #{workFrequency}");
		sql.SET("isScheduled = #{isScheduled}");
		sql.SET("needNodes = #{needNodes}");
		sql.SET("cronTrigger = #{cronTrigger}");
		sql.SET("workerClass = #{workerClass}");
		sql.SET("queueName = #{queueName}");
		sql.SET("user = #{user}");
		sql.SET("describe = #{describe}");
		sql.WHERE("name = #{name}");
		return sql.toString();
	}
}
