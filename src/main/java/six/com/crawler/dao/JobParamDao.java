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


	
	/**
	 * 通过指定任务名称查询任务参数
	 * @param jobName 任务名称
	 * @return 返回查询到的任务参数集合
	 */
	@Select("select id,jobName,name,value,`version` from "+TableNames.JOB_PARAM_TABLE_NAME+" where jobName = #{jobName}")
	public List<JobParam> queryByJob(String jobName);
	
	/**
	 * 批量保存任务参数集合
	 * @param jobParams 任务参数集合
	 * @return 返回保存的数据条数
	 */
	@InsertProvider(type = JobParamDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM)List<JobParam> jobParams);
	
	
	/**
	 * 根据任务参数id和版本更新 任务参数值
	 * @param version 当前数据版本
	 * @param newVersion 更新后的数据版本
	 * @param id  任务参数id
	 * @param name 任务参数名称
	 * @param value 任务参数值
	 * @return 返回更新到的数据条数
	 */
	@UpdateProvider(type = JobParamDaoProvider.class, method = "update")
	public int update(
			@Param("version")int version,
			@Param("newVersion")int newVersion,
			@Param("id")String id,
			@Param("name")String name,
			@Param("value")String value);
	
	/**
	 * 通过指定任务参数id删除数据
	 * @param id 任务参数id
	 * @return 返回删除掉的数据条数
	 */
	@Delete("delete from "+TableNames.JOB_PARAM_TABLE_NAME+" where `id` = #{id}")
	public int del(@Param("id")String id);
	
	/**
	 * 通过指定任务名称删除其任务参数
	 * @param jobName 任务名称
	 * @return 返回删除掉的数据条数
	 */
	@Delete("delete from "+TableNames.JOB_PARAM_TABLE_NAME+" where jobName = #{jobName}")
	public int delByJob(String jobName);
	

}
