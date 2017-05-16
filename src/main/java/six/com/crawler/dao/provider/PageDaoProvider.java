package six.com.crawler.dao.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.dao.TableNames;
import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:12:03
 */
public class PageDaoProvider {

	@SuppressWarnings("unchecked")
	public String queryByPageKey(Map<String, Object> map) {
		String sitecode = null;
		List<String> urlMd5s = null;
		Object pm = map.get("param1");
		StringBuilder preSql = new StringBuilder();
		preSql.append("select siteCode,");
		preSql.append("depth,originalUrl,");
		preSql.append("finalUrl,referer,");
		preSql.append("ancestorUrl,pageNum,charset,downerType,type,waitJsLoadElement ");
		preSql.append("from "+TableNames.SITE_PAGE_TABLE_NAME);
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
		preSql.append("from "+TableNames.SITE_PAGE_TABLE_NAME);
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

	public String save(Page page) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(TableNames.SITE_PAGE_TABLE_NAME);
		sql.append("(`siteCode`,`pageKey`,`bytes`)");
		String columns = "`siteCode`,"
				+ "pageKey,"
				+ "bytes";
		String values = "#{siteCode},"
				+ "#{pageKey},"
				+ "#{bytes}";
		return sql.toString();
	}
	
	public String bathSave(Map<String, Object> map) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(TableNames.SITE_PAGE_TABLE_NAME);
		sql.append("(`siteCode`,`pageKey`,`bytes`)");
		String columns = "`siteCode`,"
				+ "pageKey,"
				+ "bytes";
		String values = "#{siteCode},"
				+ "#{pageKey},"
				+ "#{bytes}";
		return sql.toString();
	}
}
