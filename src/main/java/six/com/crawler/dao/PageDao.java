package six.com.crawler.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.po.PagePo;
import six.com.crawler.dao.provider.PageDaoProvider;
import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:08:25
 */
public interface PageDao extends BaseDao {

	@SelectProvider(type = PageDaoProvider.class, method = "queryByPageKey")
	public PagePo queryByPageKey(@Param("siteCode") String siteCode, @Param("pageKey") String pageKey);

	@SelectProvider(type = PageDaoProvider.class, method = "queryByPageKeys")
	public List<Page> queryByPageKeys(String siteCode, List<String> pageKeys);

	@InsertProvider(type = PageDaoProvider.class, method = "save")
	public int save(PagePo page);
	
	@InsertProvider(type = PageDaoProvider.class, method = "update")
	public int update(PagePo page);

	@InsertProvider(type = PageDaoProvider.class, method = "bathSave")
	public int bathSave(List<Page> list);

}
