package six.com.crawler.dao.provider;

import java.util.Map;

import six.com.crawler.dao.TableNames;
import six.com.crawler.dao.po.PagePo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:12:03
 */
public class PageDaoProvider {

	public String queryBySiteAndKey(Map<String, Object> map) {
		StringBuilder sql=new StringBuilder();
		sql.append(" select `jobName`,`jobSnapshotId`,`siteCode`,`pageKey`,`pageUrl`,`pageSrc`,`data`");
		sql.append(" from "+TableNames.SITE_PAGE_TABLE_NAME);
		sql.append(" where `siteCode` = #{siteCode} and `pageKey` = #{pageKey} order by updateTime desc limit 0,1");
		return sql.toString();
	}

	public String save(PagePo page) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(TableNames.SITE_PAGE_TABLE_NAME);
		sql.append("(`jobName`,`jobSnapshotId`,`siteCode`,pageKey,pageUrl,pageSrc,data) ");
		sql.append("values(#{jobName},#{jobSnapshotId},#{siteCode},#{pageKey},#{pageUrl},#{pageSrc},#{data}) ");
		sql.append("ON DUPLICATE KEY UPDATE pageSrc=#{pageSrc},data=#{data}");
		return sql.toString();
	}

}
