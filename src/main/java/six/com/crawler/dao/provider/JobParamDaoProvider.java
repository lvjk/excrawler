package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.BaseDao;

import six.com.crawler.dao.TableNames;
import six.com.crawler.entity.JobParam;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 下午5:28:15
 */
public class JobParamDaoProvider extends BaseProvider{
	
	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		String columns="jobName,name,value";
		List<JobParam> extractPaths = (List<JobParam>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String values="(#{list["+INDEX_FLAG+"].jobName},"
				+ "#{list["+INDEX_FLAG+"].name},"
				+ "#{list["+INDEX_FLAG+"].value})";
		StringBuilder sbd = new StringBuilder();  
		sbd.append("insert into ").append(TableNames.JOB_PARAM_TABLE_NAME);  
		sbd.append("(").append(columns).append(") ");  
		sbd.append("values");  
		sbd.append(setBatchSaveSql(values,extractPaths));
		return sbd.toString();
	}
	
	public String update(Map<String, Object> map) {
		SQL sql=new SQL();
		sql.UPDATE(TableNames.JOB_PARAM_TABLE_NAME);
		sql.SET("`version` = #{newVersion}");
		sql.SET("`name` = #{name}");
		sql.SET("`value` = #{value}");
		sql.WHERE("`id` = #{id} and version = #{version}");
		return sql.toString();
	}

}
