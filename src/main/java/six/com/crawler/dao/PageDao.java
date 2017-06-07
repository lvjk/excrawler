package six.com.crawler.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.po.PagePo;
import six.com.crawler.dao.provider.PageDaoProvider;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:08:25
 */
public interface PageDao extends BaseDao {

	/**
	 * 通过指定siteCode和pageKey查询，返回最新的一条数据
	 * 
	 * @param siteCode
	 * @param pageKey
	 * @return
	 */
	@SelectProvider(type = PageDaoProvider.class, method = "queryBySiteAndKey")
	public PagePo queryBySiteAndKey(@Param("siteCode") String siteCode, @Param("pageKey") String pageKey);

	/**
	 * 保存PagePo
	 * 
	 * @param page
	 * @return 返回保存的数据条数
	 */
	@InsertProvider(type = PageDaoProvider.class, method = "save")
	public int save(PagePo pagePo);

	/**
	 * 删除指定多少天以前的数据
	 * 
	 * @param beforeDays
	 *            多少天以前
	 * @return 返回删除掉的数据条数
	 */
	@Delete("delete from " + TableNames.SITE_PAGE_TABLE_NAME + " where datediff(curdate(),updateTime)>=#{beforeDays}")
	public int delBeforeDate(int beforeDays);

}
