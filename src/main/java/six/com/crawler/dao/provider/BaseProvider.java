package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 下午5:33:23
 */
public class BaseProvider {

	public static final String INDEX_FLAG = "{index}";

	public <T> String setBatchSaveSql(String values, List<T> list) {
		StringBuilder sbd = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sbd.append(values.replace(INDEX_FLAG, String.valueOf(i))).append(",");
		}
		if (list.size() > 0) {
			sbd.deleteCharAt(sbd.length() - 1);
		}
		return sbd.toString();
	}

	public void buildParameter(StringBuilder preSql, Map<String, Object> map) {
		if (null != map) {
			Object parameter = map.get("id");
			if (null != parameter) {
				int id = (int) parameter;
				preSql.append(" where id=" + id);
			} else {
				StringBuilder parameterSql = new StringBuilder();
				String and = " and ";
				parameter = null;
				for (String key : map.keySet()) {
					parameter = map.get(key);
					parameterSql.append(key).append("=");
					if (parameter instanceof String) {
						parameterSql.append("'");
						parameterSql.append(parameter);
						parameterSql.append("'");
						parameterSql.append(and);
					} else {
						parameterSql.append(parameter);
						parameterSql.append(and);
					}
				}
				if (parameterSql.length() > 0) {
					parameterSql.delete(parameterSql.length() - and.length(), parameterSql.length());
					preSql.append(" where ").append(parameterSql);
				}
			}
		}
	}
}
