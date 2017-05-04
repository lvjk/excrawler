package six.com.crawler.schedule.cache;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月16日 上午4:16:17 类说明 bdb 表常量 RegisterCenter Constants
 */
public class RedisCacheKeyHelper {

	private String basePre;
	
	RedisCacheKeyHelper(String basePre){
		this.basePre=basePre;
	}

	public String getResetPreKey() {
		StringBuilder keySb = new StringBuilder();
		keySb.append(basePre);
		return keySb.toString();
	}

	/**
	 * 获取执行的job缓存key
	 * 
	 * @return
	 */
	public String getJobsKey() {
		StringBuilder keySb = new StringBuilder();
		keySb.append(basePre);
		keySb.append("_execute_jobs");
		return keySb.toString();
	}

	/**
	 * 获取 job被执行的类型key
	 * 
	 * @return
	 */
	public String getDispatchTypeKey() {
		StringBuilder keySb = new StringBuilder();
		keySb.append(basePre);
		keySb.append("_job_dispatch_types");
		return keySb.toString();
	}

	/**
	 * 获取 jobSnapshot 注册 key 前缀=PRE_REDIS_REGISTER_CENTER+nodeName+"_jobs"
	 * 
	 * @param nodeName
	 *            job 所属的node name
	 * @param jobName
	 *            job's name
	 * @return job 注册 前缀key
	 */
	public String getJobSnapshotsKey() {
		StringBuilder keySb = new StringBuilder();
		keySb.append(basePre);
		keySb.append("_job");
		keySb.append("_snapshots");
		return keySb.toString();
	}

	/**
	 * 获取 WorkerSnapshot 注册前缀key
	 * 
	 * @param nodeName
	 *            job 所属的node name
	 * @param jobName
	 *            workerName 所属的jobName
	 * @return worker 注册前缀key
	 */
	public String getWorkerSnapshotsKey(String jobName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(basePre);
		keySb.append("_");
		keySb.append(jobName);
		keySb.append("_workers");
		return keySb.toString();
	}

	/**
	 * 获取 worker 的序号分配key
	 * 
	 * @param nodeName
	 *            job 所属的node name
	 * @param jobName
	 *            workerName 所属的jobName
	 * @return 获取 worker 的序号分配key
	 */
	public String getWorkerSerialNumbersKey(String jobName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(basePre);
		keySb.append("_");
		keySb.append(jobName);
		keySb.append("_workers_sernum");
		return keySb.toString();
	}

}
