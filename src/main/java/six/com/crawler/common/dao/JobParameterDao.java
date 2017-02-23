package six.com.crawler.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.common.dao.provider.JobParameterDaoProvider;
import six.com.crawler.common.entity.JobParameter;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月12日 下午4:22:38 
*/
public interface JobParameterDao extends BaseDao{

	@SelectProvider(type = JobParameterDaoProvider.class, method = "query")
	public List<JobParameter> query(Map<String,Object> parameters);
	
	@Delete("delete from ex_crawler_platform_job_parameter where id=#{id} ")
	public int delete(int id);
}
