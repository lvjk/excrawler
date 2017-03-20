package six.com.crawler.node;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月14日 下午5:24:05 
*/
public enum NodeCommandType {

	/**
	 * 执行job
	 */
	EXECUTE_JOB,
	/**
	 * 暂停job
	 */
	SUSPEND_JOB,
	/**
	 * 继续job
	 */
	GOON_JOB,
	/**
	 * 停止job
	 */
	STOP_JOB,
	/**
	 * 停止所有
	 */
	STOP_ALL,
	/**
	 * 结束worker
	 */
	END_WORKER;
}
