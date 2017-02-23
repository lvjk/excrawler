package six.com.crawler.common.dao;

import org.apache.ibatis.annotations.Select;

import six.com.crawler.common.entity.Site;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午1:15:22
 */
public interface SiteDao extends BaseDao{

	@Select("select code,mainurl,downerType,proxy_enable,localAddress_enable,`describe` from ex_crawler_platform_site where code=#{sitecode}")
	public Site query(String sitecode);

	public int save(Site site);

	public int delete(String sitecode);

	public Site update(String sitecode);
}
