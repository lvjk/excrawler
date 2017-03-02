package six.com.crawler.common.dao.provider;

import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.common.dao.SiteDao;
import six.com.crawler.common.entity.Site;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月1日 上午11:58:06 
*/
public class SiteDaoProvider extends BaseProvider{

	public String querySites(Map<String, Object> map) {
		String sql = " select `code`,"
				+ " mainUrl,"
				+ "`describe` from "+SiteDao.TABLE_NAME;
		return sql;
	}
	
	
	public String save(Site site) {
		String columns = "`code`,"
				+ " mainUrl,"
				+ "`describe`";
		String values = "#{code},"
				+ "#{mainUrl},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(SiteDao.TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}
	
	
	public String del(String siteCode) {
		SQL sql = new SQL();
		sql.DELETE_FROM(SiteDao.TABLE_NAME);
		sql.WHERE("code = #{siteCode}");
		return sql.toString();
	}
}
