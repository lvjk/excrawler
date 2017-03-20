package six.com.crawler.dao.provider;

import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月29日 下午12:14:15
 */
public class OperationEventDaoProvider {

	public String query(Map<String, Object> map) {
		StringBuilder sql = new StringBuilder();
		sql.append("select jobName,serialNub,operationType,className,`describe` ");
		sql.append(" from ex_crawler_platform_operation_event ");

		StringBuilder queryItem = new StringBuilder();
		Object ob = map.get("jobName");
		String andStr = " and ";
		if (null != ob) {
			queryItem.append(andStr);
			queryItem.append("jobName='" + ob).append("' ");
		}
		ob = map.get("operationType");
		if (null != ob) {
			queryItem.append(andStr);
			queryItem.append("operationType=" + ob).append(" ");
		}
		if (queryItem.length() > 0) {
			queryItem.delete(0, andStr.length());
			sql.append(" where ").append(queryItem);
		}
		sql.append(" order by serialNub asc");
		return sql.toString();
	}
}
