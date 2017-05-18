package six.com.crawler.dao.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.dao.TableNames;
import six.com.crawler.dao.po.PagePo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:12:03
 */
public class PageDaoProvider {

	public String queryByPageKey(Map<String, Object> map) {
		SQL sql = new SQL();
		sql.SELECT("`siteCode`,`pageKey`,`data`");
		sql.FROM(TableNames.SITE_PAGE_TABLE_NAME);
		sql.WHERE("`siteCode` = #{siteCode} and `pageKey` = #{pageKey}");
		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	public String queryByPageKeys(Map<String, Object> map) {
		String sitecode = null;
		List<String> urlMd5s = null;
		Object pm = map.get("param1");
		StringBuilder preSql = new StringBuilder();
		preSql.append("select siteCode,");
		preSql.append("depth,originalUrl,");
		preSql.append("finalUrl,referer,");
		preSql.append("ancestorUrl,pageNum,charset,downerType,type,waitJsLoadElement ");
		preSql.append("from " + TableNames.SITE_PAGE_TABLE_NAME);
		if (pm != null) {
			sitecode = pm.toString();
		} else {
			sitecode = "";
		}
		pm = map.get("param2");
		if (pm != null) {
			urlMd5s = (List<String>) pm;
		} else {
			urlMd5s = Collections.EMPTY_LIST;
		}
		StringBuilder parameterSql = new StringBuilder();
		parameterSql.append(" where siteCode= '").append(sitecode).append("'");
		parameterSql.append(" and urlMd5 in (");
		for (String md5 : urlMd5s) {
			parameterSql.append("'").append(md5).append("',");
		}
		if (urlMd5s.size() > 0) {
			parameterSql.deleteCharAt(parameterSql.length() - 1);
			parameterSql.append(")");
			preSql.append(parameterSql);
		}
		return preSql.toString();
	}

	public String save(PagePo page) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(TableNames.SITE_PAGE_TABLE_NAME);
		sql.append("(`siteCode`,pageKey,data) ");
		sql.append("values(#{siteCode},#{pageKey},#{data}) ");
		sql.append("ON DUPLICATE KEY UPDATE data=#{data}");
		return sql.toString();
	}

	public String bathSave(Map<String, Object> map) {
		String columns = "`siteCode`," + "pageKey," + "data";
		String values = "#{siteCode}," + "#{pageKey}," + "#{data}";
		SQL sql = new SQL();
		sql.INSERT_INTO(TableNames.SITE_PAGE_TABLE_NAME);
		sql.VALUES(columns, values);
		return sql.toString();
	}

	public String update(PagePo page) {
		SQL sql = new SQL();
		sql.UPDATE(TableNames.SITE_PAGE_TABLE_NAME);
		sql.SET("`data` = #{data}");
		sql.WHERE("`siteCode` = #{siteCode} and `pageKey` = #{pageKey}");
		return sql.toString();
	}
}
