package six.com.crawler.dao;

import java.util.List;

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
public interface JobDao extends BaseDao {

	/**
	 * 通过指定任务名称查询
	 * 
	 * @param name 任务名称
	 *            
	 * @return 查询到的数据实体
	 */
	@Select("select `name`," + "   level," + " designatedNodeName," + "needNodes," + "threads," + "isScheduled,"
			+ "cronTrigger," + "workFrequency," + "workerClass," + "workSpaceName," + "`user`,"
			+ "`describe`,`version` from " + TableNames.JOB_TABLE_NAME + " where name = #{name}")
	public Job query(String name);

	/**
	 * 通过指定任务name进行模糊分页查询
	 * 
	 * @param name
	 *            任务名称前缀
	 * @param pageIndex
	 *            分页起始位置
	 * @param pageSize
	 *            分页偏移位置
	 * @return 返回查询到的数据集合
	 */
	@SelectProvider(type = JobDaoProvider.class, method = "pageQuery")
	public List<Job> pageQuery(@Param("name") String name, @Param("start") int pageIndex, @Param("end") int pageSize);

	/**
	 * 查询开启定时调度的任务
	 * 
	 * @return 返回查询到开启定时调度的任务集合
	 */
	@SelectProvider(type = JobDaoProvider.class, method = "queryIsScheduled")
	public List<Job> queryIsScheduled();

	/**
	 * 保存指定任务数据
	 * 
	 * @param job
	 *            任务实体对象
	 * @return 保存的数据条数
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "save")
	public int save(Job job);

	/**
	 * 通过指定任务名称删除数据
	 * 
	 * @param name
	 *            任务名称
	 * @return 返回删除掉的数据条数
	 */
	@Delete("delete from " + TableNames.JOB_TABLE_NAME + " where name = #{name}")
	public int del(String name);

	/**
	 * 根据指定任务名称版本数据更新 任务是否开启定时调度
	 * 
	 * @param version
	 *            当前数据版本
	 * @param newVersion
	 *            数据更新后的版本号
	 * @param name
	 *            任务名称
	 * @param isScheduled
	 *            是否开启定时调度
	 * @return 返回更新到数据条数
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "updateIsScheduled")
	public int updateIsScheduled(@Param("version") int version, @Param("newVersion") int newVersion,
			@Param("name") String name, @Param("isScheduled") int isScheduled);

	/**
	 * 根据指定任务名称版本数据更新任务时间调度表达式
	 * 
	 * @param version
	 *            当前数据版本
	 * @param newVersion
	 *            数据更新后的版本号
	 * @param name
	 *            任务名称
	 * @param cronTrigger
	 *            时间调度表达式
	 * @return 返回更新到数据条数
	 */
	@UpdateProvider(type = JobDaoProvider.class, method = "updateCronTrigger")
	public int updateCronTrigger(@Param("version") int version, @Param("newVersion") int newVersion,
			@Param("name") String name, @Param("cronTrigger") String cronTrigger);

}
