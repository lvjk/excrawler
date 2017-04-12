package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.provider.SiteDaoProvider;
import six.com.crawler.entity.Site;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午1:15:22
 */
public interface SiteDao extends BaseDao {

	String TABLE_NAME = "ex_crawler_platform_site";

	@SelectProvider(type = SiteDaoProvider.class, method = "querySites")
	public List<Site> querySites(int pageIndex, int pageSize);

	@Select("select code,mainurl,visitFrequency,`describe` from " + TABLE_NAME + " where code=#{sitecode}")
	public Site query(String sitecode);

	@InsertProvider(type = SiteDaoProvider.class, method = "save")
	public int save(Site site);

	@DeleteProvider(type = SiteDaoProvider.class, method = "del")
	public int del(String sitecode);

	public Site update(String sitecode);
}
