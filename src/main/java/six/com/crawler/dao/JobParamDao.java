package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import six.com.crawler.dao.provider.JobParamDaoProvider;
import six.com.crawler.entity.JobParam;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月12日 下午4:22:38 
*/
public interface JobParamDao extends BaseDao{


	@Select("select id,jobName,name,value,`version` from "+TableNames.JOB_PARAM_TABLE_NAME+" where jobName = #{jobName}")
	public List<JobParam> queryJobParams(String jobName);
	
	@InsertProvider(type = JobParamDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM)List<JobParam> jobParams);
	
	
	@UpdateProvider(type = JobParamDaoProvider.class, method = "update")
	public int update(
			@Param("version")int version,
			@Param("newVersion")int newVersion,
			@Param("id")String id,
			@Param("name")String name,
			@Param("value")String value);
	
	@Delete("delete from "+TableNames.JOB_PARAM_TABLE_NAME+" where `id` = #{id}")
	public int del(@Param("id")String id);
	
	@Delete("delete from "+TableNames.JOB_PARAM_TABLE_NAME+" where jobName = #{jobName}")
	public int delJobParams(String jobName);
	

}
