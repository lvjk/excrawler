package six.com.crawler.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import six.com.crawler.common.dao.provider.JobDaoProvider;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午2:25:36
 */
public interface JobDao extends BaseDao{

	
	String TABLE_NAME="ex_crawler_platform_job";
	
	
	@SelectProvider(type = JobDaoProvider.class, method = "totalNodeJobInfo")
	Node totalNodeJobInfo(String nodeName);
	/**
	 * 通过id查询
	 * 
	 * @param id
	 * @return
	 */
	@Select("select `name`,"
			+ " localNode,"
			+ "   level,"
			+ "workFrequency,"
			+ "isScheduled,"
			+ "needNodes,"
			+ "cronTrigger,"
			+ "workerClass,"
			+ "queueName,"
			+ "`user`,"
			+ "`describe` from "+TABLE_NAME+" where name = #{name} order by `level` asc,`name`")
	public Job query(String name);
	
	
	@Select("select `name`,"
			+ " localNode,"
			+ "   level,"
			+ "workFrequency,"
			+ "isScheduled,"
			+ "needNodes,"
			+ "cronTrigger,"
			+ "workerClass,"
			+ "queueName,"
			+ "`user`,"
			+ "`describe` from "+TABLE_NAME+" where localNode = #{localNode} order by `level` asc,`name`")
	public List<Job> queryByNode(String localNode);
	

	@Select("select `name`,"
			+ " localNode,"
			+ "   level,"
			+ "workFrequency,"
			+ "isScheduled,"
			+ "needNodes,"
			+ "cronTrigger,"
			+ "workerClass,"
			+ "queueName,"
			+ "`user`,"
			+ "`describe` from "+TABLE_NAME+" where name like #{jobName} order by `level` asc,`name`")
	public List<Job> fuzzyQuery(String jobName);
	
	

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
	 * 更新数据
	 * 
	 * @param t
	 * @return
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "update")
	public int update(Job job);

}
