package six.com.crawler.common.dao.provider;

import java.util.List;
import java.util.Map;

import six.com.crawler.common.dao.ExtractItemDao;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月13日 下午12:05:10
 */
public class ExtractItemDaoProvider {

	@SuppressWarnings("unchecked")
	public String save(Map<String, Object> map) {
		Object pm = map.get("list");
		List<ExtractItem> paserResults = (List<ExtractItem>) pm;
		StringBuilder sql = new StringBuilder("INSERT INTO "+ExtractItemDao.tableName+"("
				+ "jobName,pathName,type,resultKey,mustHaveResult,pageType,`describe`) VALUES");
		for (ExtractItem paserResult : paserResults) {
			sql.append("(");
			sql.append("'").append(paserResult.getJobName()).append("',");
			sql.append("'").append(paserResult.getPathName()).append("',");
			sql.append("").append(paserResult.getType()).append(",");
			sql.append("'").append(paserResult.getResultKey()).append("',");
			sql.append("").append(paserResult.getMustHaveResult().get()).append(",");
			sql.append("").append(paserResult.getPageType().value()).append(",");
			sql.append("'").append(paserResult.getDescribe()).append("'");
			sql.append(")");
			sql.append(",");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(";");
		return sql.toString();
	}
}
