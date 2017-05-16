package six.com.crawler.dao.provider;

import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.TableNames;
import six.com.crawler.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月23日 下午5:46:33
 */
public class JobDaoProvider extends BaseProvider {

	
	public String query(Map<String, Object> parameters) {
		StringBuilder sql = new StringBuilder();
		sql.append("select `name`,"
				+ "   level," 
				+ " designatedNodeName,"
				+ "needNodes,"
				+ "threads,"
				+ "isScheduled,"
				+ "cronTrigger,"
				+ "workFrequency,"
				+ "workerClass,"
				+ "workSpaceName,"
				+ "`user`,"
				+ "`describe`,"
				+ "`version`");
		sql.append(" from "+TableNames.JOB_TABLE_NAME);
		buildParameter(sql, parameters);
		sql.append(" order by `level` asc,`name`");
		return sql.toString();
	}
	

	public String pageQuery(Map<String, Object> queryParams) {
		String sql="select b.totalSize,a.* "
				+ " from("
				+ "		select `name`,"
				+ "			  `level`,"
				+ "			designatedNodeName,"
				+ "         needNodes,"
				+ "			threads,"
				+ "       isScheduled,"
				+ "       cronTrigger,"
				+ "     workFrequency,"
				+ "       workerClass,"
				+ "     workSpaceName,"
				+ "              user,"
				+ "         `describe`,"
				+ "			`version` "
				+ "       from "+TableNames.JOB_TABLE_NAME
				+ "      where `name` like concat(#{name},'%')"
				+ "      order by `name` asc ) a,"
				+ "   (select FOUND_ROWS() totalSize)b limit #{start},#{end}";
		return sql;
	}
	
	public String queryIsScheduled(){
		StringBuilder sql = new StringBuilder();
		sql.append("select `name`,"
				+ "   level,"
				+ " designatedNodeName,"
				+ "needNodes,"
				+ "threads,"
				+ "isScheduled,"
				+ "cronTrigger,"
				+ "workFrequency,"
				+ "workerClass,"
				+ "workSpaceName,"
				+ "`user`,"
				+ "`describe`,"
				+ "`version`");
		sql.append(" from "+TableNames.JOB_TABLE_NAME);
		sql.append(" where isScheduled=1");
		return sql.toString();
	
	}
	
	public String save(Job job) {
		String columns = "`name`,"
				+ "level,"
				+ "designatedNodeName,"
				+ "needNodes,"
				+ "threads,"
				+ "isScheduled,"
				+ "cronTrigger,"
				+ "workFrequency,"
				+ "workerClass,"
				+ "workSpaceName,"
				+ "`user`,"
				+ "`describe`";
		
		String values = "#{name},"
				+ "#{level},"
				+ "#{designatedNodeName},"
				+ "#{needNodes},"
				+ "#{threads},"
				+ "#{isScheduled},"
				+ "#{cronTrigger},"
				+ "#{workFrequency},"
				+ "#{workerClass},"
				+ "#{workSpaceName},"
				+ "#{user},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(TableNames.JOB_TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}


	public String updateIsScheduled(Map<String, Object> queryParams) {
		SQL sql=new SQL();
		sql.UPDATE(TableNames.JOB_TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("isScheduled = #{isScheduled}");
		sql.WHERE("name = #{name} and version = #{version}");
		return sql.toString();
	}
	
	public String updateCronTrigger(Map<String, Object> queryParams) {
		SQL sql=new SQL();
		sql.UPDATE(TableNames.JOB_TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("cronTrigger = #{cronTrigger}");
		sql.WHERE("name = #{name} and version = #{version}");
		return sql.toString();
	}
	
}
