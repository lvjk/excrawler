package six.com.crawler.common.dao.provider;

import java.util.List;
import java.util.Map;

import six.com.crawler.common.entity.Job;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 上午11:12:15
 */
public class TriggerTimeDaoProvider {

	@SuppressWarnings("unchecked")
	public String queryTriggerTime(Map<String, Object> map) {
		StringBuilder sql = new StringBuilder(
				"select jobname,triggerTime,cycleTime " + "from ex_crawler_platform_job_schedule where jobname in (");
		List<Job> jobs = (List<Job>) map.get("list");
		for (Job job : jobs) {
			sql.append("'").append(job.getName()).append("',");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
		return sql.toString();
	}

}
