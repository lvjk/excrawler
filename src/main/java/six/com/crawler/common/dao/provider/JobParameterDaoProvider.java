package six.com.crawler.common.dao.provider;

import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 下午5:28:15
 */
public class JobParameterDaoProvider extends BaseProvider{
	
	public String query(Map<String, Object> parameters) {
		StringBuilder sql = new StringBuilder(
				"select jobName,attName,attValue from ex_crawler_platform_job_parameter");
		buildParameter(sql, parameters);
		return sql.toString();
	}
}
