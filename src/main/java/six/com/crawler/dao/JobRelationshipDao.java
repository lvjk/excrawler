package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import six.com.crawler.entity.JobRelationship;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月14日 下午4:05:34 
*/
public interface JobRelationshipDao extends BaseDao{

	public static String TABLE_NAME = "ex_crawler_platform_job_relationship";
	/**
	 * 通过id查询
	 * 
	 * @param id nextJobName
	 * @return
	 */
	@Select("select currentJobName,"
			+ "   nextJobName,"
			+ " executeType,"
			+ "`version` from "+TABLE_NAME+" where currentJobName = #{jobName}")
	public List<JobRelationship> query(String jobName);
}
