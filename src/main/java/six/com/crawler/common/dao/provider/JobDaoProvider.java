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

	public String query(Map<String, Object> parameters) {
		StringBuilder sql = new StringBuilder();
		sql.append("select `name`,cronTrigger,needNodes,level,httpProxyType,"
				+ "loadImages,isSnapshotTable,fixedTableName,");
		sql.append(" everyProcessDelayTime,hostNode,isScheduled,workerClass,"
				+ "queueName,resultStoreClass,siteCode,`describe`");
		sql.append(" from "+JobDao.TABLE_NAME);
		buildParameter(sql, parameters);
		sql.append(" order by `level` asc,`name`");
		return sql.toString();
	}
	
	public String save(Job job) {
		String columns = "`name`,"
				+ "siteCode,"
				+ "hostNode,"
				+ "level,"
				+ "workerClass,"
				+ "httpProxyType,"
				+ "loadImages,"
				+ "needNodes,"				
				+ "everyProcessDelayTime,"
				+ "queueName,"
				+ "user,"
				+ "resultStoreClass,"
				+ "isScheduled,"
				+ "isSnapshotTable,"
				+ "fixedTableName,"
				+ "cronTrigger,"
				+ "describe";
		String values = "#{name},"
				+ "#{siteCode},"
				+ "#{hostNode},"
				+ "#{level},"
				+ "#{workerClass},"
				+ "#{httpProxyType},"
				+ "#{loadImages},"
				+ "#{needNodes},"
				+ "#{everyProcessDelayTime},"
				+ "#{queueName}"
				+ "#{user},"
				+ "#{resultStoreClass},"
				+ "#{isScheduled},"
				+ "#{isSnapshotTable},"
				+ "#{fixedTableName},"
				+ "#{cronTrigger},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(JobSnapshotDao.TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}

	public String update(Job job) {
		SQL sql=new SQL();
		sql.UPDATE(JobDao.TABLE_NAME);
		sql.SET("siteCode = #{siteCode}");
		sql.SET("hostNode = #{hostNode}");
		sql.SET("level = #{level}");
		sql.SET("workerClass = #{workerClass}");
		sql.SET("httpProxyType = #{httpProxyType}");
		sql.SET("loadImages = #{loadImages}");
		sql.SET("needNodes = #{needNodes}");
		sql.SET("cronTrigger = #{cronTrigger}");
		sql.SET("isSnapshotTable = #{isSnapshotTable}");
		sql.SET("fixedTableName = #{fixedTableName}");
		sql.SET("everyProcessDelayTime = #{everyProcessDelayTime}");
		sql.SET("user = #{user}");
		sql.SET("describe = #{describe}");
		sql.SET("queueName = #{queueName}");
		sql.SET("resultStoreClass = #{resultStoreClass}");
		sql.SET("isScheduled = #{isScheduled}");
		sql.WHERE("name = #{name}");
		return sql.toString();
	}
}
