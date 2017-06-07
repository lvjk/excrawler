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
public interface ExtractPathDao extends BaseDao {

	String PARAM_PATH_NAME = "pathName";

	String PARAM_SITE_CODE = "siteCode";

	/**
	 * 通过指定sitecode查询相关的元素抽取路径集合
	 * 
	 * @param siteCode 网站编号
	 * @return  返回匹配到的数据集合
	 */
	@SelectProvider(type = ExtractPathDaoProvider.class, method = "queryBySite")
	public List<ExtractPath> queryBySite(@Param("siteCode") String siteCode);

	/**
	 * 通过指定siteCode和name查询相关的元素抽取路径集合
	 * 
	 * @param siteCode 网站编号
	 * @param name 元素抽取路径名称
	 * @return 返回匹配到的数据集合
	 */
	@SelectProvider(type = ExtractPathDaoProvider.class, method = "query")
	public List<ExtractPath> queryBySiteAndName(@Param("siteCode") String siteCode, @Param("name") String name);


	/**
	 * 通过指定siteCode和name模糊查询相关的元素抽取路径集合
	 * 
	 * @param siteCode 网站编号
	 * @param name 元素抽取路径名称
	 * @return 返回匹配到的数据集合
	 */
	@SelectProvider(type = ExtractPathDaoProvider.class, method = "fuzzyQuery")
	public List<ExtractPath> fuzzyQuery(@Param("siteCode") String siteCode, @Param("name") String pathName);

	/**
	 * 批量保存元素抽取路径集合
	 * @param paserResults 需要批量保存的元素抽取路径集合
	 * @return 插入的数据条数
	 */
	@InsertProvider(type = ExtractPathDaoProvider.class, method = "batchSave")
	public int batchSave(@Param(BATCH_SAVE_PARAM) List<ExtractPath> paserResults);

	/**
	 * 通过指定名字删除元素抽取路径
	 * @param name 元素抽取路径名称
	 * @return 删除的数据条数
	 */
	@DeleteProvider(type = ExtractPathDaoProvider.class, method = "delByName")
	public int delByName(String name);

	/**
	 * 通过指定sitecode删除元素抽取路径
	 * @param siteCode 网站编号
	 * @return 删除的数据条数
	 */
	@DeleteProvider(type = ExtractPathDaoProvider.class, method = "delBySiteCode")
	public int delBySiteCode(String siteCode);

}
