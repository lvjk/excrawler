package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import six.com.crawler.dao.provider.ExtractItemDaoProvider;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月13日 上午11:02:19
 */
public interface ExtractItemDao extends BaseDao {


	@Select("select jobName,serialNub,pathName,`primary`,type,outputType,outputKey,mustHaveResult,`describe` from "
			+ TableNames.JOB_EXTRACT_ITEM_TABLE_NAME + " where jobName=#{jobName} order by serialNub asc")
	public List<ExtractItem> query(String jobName);

	@InsertProvider(type = ExtractItemDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM) List<ExtractItem> paserResults);
	
	@Delete("delete from "+TableNames.JOB_EXTRACT_ITEM_TABLE_NAME+" where jobName = #{jobName}")
	public int del(String jobName);
}
