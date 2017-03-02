package six.com.crawler.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import six.com.crawler.common.dao.provider.JobParamDaoProvider;
import six.com.crawler.common.entity.JobParam;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月12日 下午4:22:38 
*/
public interface JobParamDao extends BaseDao{

	String TABLE_NAME="ex_crawler_platform_job_param";

	@Select("select jobName,name,value from "+TABLE_NAME+" where jobName = #{jobName}")
	public List<JobParam> queryJobParams(String jobName);
	
	@InsertProvider(type = JobParamDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM)List<JobParam> jobParams);
	
	@Delete("delete from "+TABLE_NAME+" where jobName = #{jobName}")
	public int delJobParams(String jobName);
	

}
