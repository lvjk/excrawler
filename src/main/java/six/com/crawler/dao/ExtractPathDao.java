package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.provider.ExtractPathDaoProvider;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午9:30:12
 */
public interface ExtractPathDao extends BaseDao{


	String PARAM_PATH_NAME="pathName";
	
	String PARAM_SITE_CODE="siteCode";
	
	
	@SelectProvider(type = ExtractPathDaoProvider.class, method = "queryBySite")
	public List<ExtractPath> queryBySite(@Param("siteCode")String siteCode);
	
	@SelectProvider(type = ExtractPathDaoProvider.class, method = "query")
	public List<ExtractPath> query(@Param("name")String pathName,@Param("siteCode")String siteCode);
	
	@SelectProvider(type = ExtractPathDaoProvider.class, method = "fuzzyQuery")
	public List<ExtractPath> fuzzyQuery(@Param("siteCode")String siteCode,@Param("name")String pathName);

	@InsertProvider(type = ExtractPathDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM)List<ExtractPath> paserResults);
	
	@DeleteProvider(type = ExtractPathDaoProvider.class, method = "delByName")
	public int delByName(String name);
	
	@DeleteProvider(type = ExtractPathDaoProvider.class, method = "delBySiteCode")
	public int delBySiteCode(String siteCode);

	public int update(ExtractPath t);

}
