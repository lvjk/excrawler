package six.com.crawler.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import six.com.crawler.dao.provider.JobDaoProvider;
import six.com.crawler.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午2:25:36
 */
public interface JobDao extends BaseDao{

	
	String TABLE_NAME="ex_crawler_platform_job";
	
	
	/**
	 * 通过id查询
	 * 
	 * @param id nextJobName
	 * @return
	 */
	@Select("select `name`,"
			+ "   nextJobName,"
			+ "   level,"
			+ " designatedNodeName,"
			+ "needNodes,"
			+ "isScheduled,"
			+ "cronTrigger,"
			+ "workFrequency,"
			+ "workerClass,"
			+ "queueName,"
			+ "`user`,"
			+ "`describe`,`version` from "+TABLE_NAME+" where name = #{name}")
	public Job query(String name);
	

	/**
	 * 支持 name%模糊查询
	 * @param jobName
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@SelectProvider(type = JobDaoProvider.class, method = "pageQuery")
	public List<Job> pageQuery(@Param("name")String jobName, 
			@Param("start")int pageIndex, 
			@Param("end")int pageSize);

	
	@SelectProvider(type = JobDaoProvider.class, method = "query")
	public List<Job> queryByParam(Map<String, Object> parameters);
	
	
	@SelectProvider(type = JobDaoProvider.class, method = "queryIsScheduled")
	public List<Job> queryIsScheduled();

	/**
	 * 保存数据
	 * 
	 * @param t
	 * @return
	 */
	/**
	 * 更新数据
	 * 
	 * @param t
	 * @return
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "save")
	public int save(Job job);

	/**
	 * 删除数据
	 * 
	 * @param id
	 * @return
	 */
	@Delete("delete from "+TABLE_NAME+" where name = #{jobName}")
	public int del(String jobName);

	/**
	 * 更新数据 job的 是否调度 
	 * @param t
	 * @return
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "updateIsScheduled")
	public int updateIsScheduled(
			@Param("version")int version,
			@Param("newVersion")int newVersion,
			@Param("name")String name,
			@Param("isScheduled")int isScheduled);
	/**
	 * 更新数据 job的 CronTrigger 时间表达式
	 * @param t
	 * @return
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "updateCronTrigger")
	public int updateCronTrigger(
			@Param("version")int version,
			@Param("newVersion")int newVersion,
			@Param("name")String name,
			@Param("cronTrigger") String cronTrigger);
}
