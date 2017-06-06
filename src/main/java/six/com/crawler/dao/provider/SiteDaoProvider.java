package six.com.crawler.dao.provider;

import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.TableNames;
import six.com.crawler.entity.Site;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月1日 上午11:58:06 
*/
public class SiteDaoProvider extends BaseProvider{
	
	public String querySites(Map<String, Object> map) {
		String sql = " select `code`,"
				+ " mainUrl,"
				+ " visitFrequency,"
				+ "`describe` from "+TableNames.SITE_TABLE_NAME;
		return sql;
	}
	
	
	public String save(Site site) {
		String columns = "`code`,"
				+ " mainUrl,"
				+ " visitFrequency,"
				+ "`describe`";
		String values = "#{code},"
				+ "#{mainUrl},"
				+ "#{visitFrequency},"
				+ "#{describe}";
		SQL sql = new SQL();
		sql.INSERT_INTO(TableNames.SITE_TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}
	
}
