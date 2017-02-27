package six.com.crawler.schedule;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月16日 上午4:16:17 类说明 bdb 表常量 RegisterCenter Constants
 */
public class RedisRegisterKeyUtils {

	final static String PRE_REDIS_REGISTER_CENTER = "spider_redis_register_center";
	
	public static String getNodesPreKey() {
		StringBuilder keySb = new StringBuilder();
		keySb.append(PRE_REDIS_REGISTER_CENTER);
		keySb.append("_nodes");
		return keySb.toString();
	}
	
	public static String getNodesKey(String nodeName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(getNodesPreKey());
		keySb.append("_");
		keySb.append(nodeName);
		return keySb.toString();
	}
	
	
	public static String getResetPreKey(String nodeName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(PRE_REDIS_REGISTER_CENTER);
		keySb.append("_");
		keySb.append(nodeName);
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
	public static String getJobSnapshotsKey(String nodeName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(PRE_REDIS_REGISTER_CENTER);
		keySb.append("_");
		keySb.append(nodeName);
		keySb.append("_jobs");
		return keySb.toString();
	}


	/**
	 * 获取 WorkerSnapshot 注册前缀key
	 * @param nodeName
	 *            job 所属的node name
	 * @param jobName
	 *            workerName 所属的jobName
	 * @return worker 注册前缀key
	 */
	public static String getWorkerSnapshotsKey(String nodeName,String jobName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(PRE_REDIS_REGISTER_CENTER);
		keySb.append("_");
		keySb.append(nodeName);
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
	public static String getWorkerSerialNumbersKey(String nodeName,String jobName) {
		StringBuilder keySb = new StringBuilder();
		keySb.append(PRE_REDIS_REGISTER_CENTER);
		keySb.append("_");
		keySb.append(nodeName);
		keySb.append("_");
		keySb.append(jobName);
		keySb.append("_workers_sernum");
		return keySb.toString();
	}

}
