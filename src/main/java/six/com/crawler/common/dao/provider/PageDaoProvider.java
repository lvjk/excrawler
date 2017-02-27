package six.com.crawler.common.dao.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import six.com.crawler.common.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午12:12:03
 */
public class PageDaoProvider {

	@SuppressWarnings("unchecked")
	public String query(Map<String, Object> map) {
		String sitecode = null;
		List<String> urlMd5s = null;
		Object pm = map.get("param1");
		StringBuilder preSql = new StringBuilder();
		preSql.append("select siteCode,");
		preSql.append("depth,originalUrl,");
		preSql.append("finalUrl,referer,");
		preSql.append("ancestorUrl,pageNum,charset,downerType,type,waitJsLoadElement ");
		preSql.append("from ex_crawler_platform_seed_page ");
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
	public String save(Map<String, Object> map) {
		List<Page> pages = null;
		Object pm = map.get("list");
		StringBuilder sql = new StringBuilder("INSERT INTO ex_crawler_platform_seed_page"
				+ " (urlMd5,sitecode,originalUrl,firstUrl,finalUrl,ancestorUrl,referer,"
				+ "pageNum,charset,downerType,waitJsLoadElement,type) VALUES");
		pages = (List<Page>) pm;
		for (Page page : pages) {
			sql.append("(");
			sql.append("'").append(page.getPageKey()).append("',");
			sql.append("'").append(page.getSiteCode()).append("',");
			sql.append("'").append(page.getOriginalUrl()).append("',");
			sql.append("'").append(page.getFirstUrl()).append("',");
			sql.append("'").append(page.getFinalUrl()).append("',");
			sql.append("'").append(page.getAncestorUrl()).append("',");
			sql.append("'").append(page.getReferer()).append("',");
			sql.append("").append(page.getPageNum()).append(",");
			sql.append("'").append(page.getCharset()).append("',");
			sql.append("").append(page.getDownerType().value()).append(",");
			sql.append("'").append(page.getWaitJsLoadElement()).append("',");
			sql.append("").append(page.getType().value());
			sql.append(")");
			sql.append(",");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(";");
		return sql.toString();
	}
}
