package six.com.crawler.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import six.com.crawler.common.dao.provider.JobDaoProvider;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParameter;

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
	 * @param id
	 * @return
	 */
	@Select("select name,cronTrigger,needNodes,level,httpProxyType,loadImages,isSnapshotTable,fixedTableName,"
			+ "everyProcessDelayTime,hostNode,workerClass,queueName,"
			+ "isScheduled,resultStoreClass,"
			+ "siteCode,`describe` from "+TABLE_NAME+" where name = #{name} ")
	@Results({ @Result(id = true, column = "name", property = "name"),
			@Result(property = "cronTrigger", column = "cronTrigger"),
			@Result(property = "hostNode", column = "hostNode"), 
			@Result(property = "needNodes", column = "needNodes"),
			@Result(property = "level", column = "level"),
			@Result(property = "httpProxyType", column = "httpProxyType"),
			@Result(property = "loadImages", column = "loadImages"),
			@Result(property = "isSnapshotTable", column = "isSnapshotTable"),
			@Result(property = "fixedTableName", column = "fixedTableName"),
			@Result(property = "user", column = "user"),
			@Result(property = "everyProcessDelayTime", column = "everyProcessDelayTime"),
			@Result(property = "queueName", column = "queueName"),
			@Result(property = "resultStoreClass", column = "resultStoreClass"),
			@Result(property = "siteCode", column = "siteCode"),
			@Result(property = "isScheduled", column = "isScheduled"),
			@Result(property = "describe", column = "describe") })
	public Job query(String name);

	@Select("select name,cronTrigger,needNodes,level,httpProxyType,"
			+ "loadImages,isSnapshotTable,fixedTableName,"
			+ "everyProcessDelayTime,hostNode,workerClass,"
			+ "queueName,resultStoreClass,siteCode,"
			+ "`describe` from "+TABLE_NAME+" where name like #{jobName} order by `level` asc,`name`")
	@Results({ @Result(id = true, column = "name", property = "name"),
			@Result(property = "cronTrigger", column = "cronTrigger"),
			@Result(property = "hostNode", column = "hostNode"), @Result(property = "needNodes", column = "needNodes"),
			@Result(property = "level", column = "level"),
			@Result(property = "httpProxyType", column = "httpProxyType"),
			@Result(property = "loadImages", column = "loadImages"),
			@Result(property = "isSnapshotTable", column = "isSnapshotTable"),
			@Result(property = "fixedTableName", column = "fixedTableName"),
			@Result(property = "user", column = "user"),
			@Result(property = "everyProcessDelayTime", column = "everyProcessDelayTime"),
			@Result(property = "resultStoreClass", column = "resultStoreClass"),
			@Result(property = "siteCode", column = "siteCode"), @Result(property = "queueName", column = "queueName"),
			@Result(property = "describe", column = "describe") })
	public List<Job> fuzzyQuery(String jobName);

	@SelectProvider(type = JobDaoProvider.class, method = "query")
	public List<Job> queryByParam(Map<String, Object> parameters);

	@Select("select jobname,attname,attvalue from ex_crawler_platform_job_parameter where jobname = #{jobName}")
	public List<JobParameter> queryJobParameter(String jobName);

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
	public int delete(String id);

	/**
	 * 更新数据
	 * 
	 * @param t
	 * @return
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "update")
	public int update(Job job);

}
