package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.provider.SiteDaoProvider;
import six.com.crawler.entity.Site;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午1:15:22
 * 
 *       网页站点实体类 存储层访问接口
 * 
 */
public interface SiteDao extends BaseDao {

	/**
	 * 分页查询
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return 返回分页后的数据集合
	 */
	@SelectProvider(type = SiteDaoProvider.class, method = "querySites")
	public List<Site> pageQuery(int pageIndex, int pageSize);

	/**
	 * 根据指定code查询
	 * 
	 * @param sitecode
	 * @return 返回相等code的数据
	 */
	@Select("select code,mainurl,visitFrequency,`describe` from " + TableNames.SITE_TABLE_NAME + " where code=#{code}")
	public Site query(String code);

	/**
	 * 保存指定site
	 * 
	 * @param site
	 * @return 返回受影响数据条数
	 */
	@InsertProvider(type = SiteDaoProvider.class, method = "save")
	public int save(Site site);

	/**
	 * 删除指定code的站点数据
	 * 
	 * @param sitecode
	 * @return 返回受影响数据条数
	 */
	@Delete("delete from " + TableNames.SITE_TABLE_NAME + " where code=#{code}")
	public int del(String sitecode);

}
