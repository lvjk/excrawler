package six.com.crawler.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Select;

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
	

}