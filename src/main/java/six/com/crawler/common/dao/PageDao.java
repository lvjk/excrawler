package six.com.crawler.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.common.dao.provider.PageDaoProvider;
import six.com.crawler.common.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:08:25
 */
public interface PageDao extends BaseDao{

	@SelectProvider(type = PageDaoProvider.class, method = "query")
	public List<Page> query(String siteCode, List<String> urlMd5);

	@InsertProvider(type = PageDaoProvider.class, method = "save")
	public int save(List<Page> pages);
}
