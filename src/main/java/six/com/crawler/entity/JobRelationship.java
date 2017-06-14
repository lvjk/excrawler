package six.com.crawler.entity;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月14日 下午3:54:25 job 组
 */
public class JobRelationship extends BasePo {

	private static final long serialVersionUID = 2801633321033342662L;

	/** 串行* */
	public static int EXECUTE_TYPE_SERIAL = 1;
	/** 并行* */
	public static int EXECUTE_TYPE_PARALLEL = 2;
	/** 当前运行job 名称 **/
	private String currentJobName;
	/** 被触发的job 名称 **/
	private String nextJobName;
	/** 被触发的job执行方式 默认为串行 **/
	private int executeType = EXECUTE_TYPE_SERIAL;

	public String getCurrentJobName() {
		return currentJobName;
	}

	public void setCurrentJobName(String currentJobName) {
		this.currentJobName = currentJobName;
	}

	public String getNextJobName() {
		return nextJobName;
	}

	public void setNextJobName(String nextJobName) {
		this.nextJobName = nextJobName;
	}

	public int getExecuteType() {
		return executeType;
	}

	public void setExecuteType(int executeType) {
		this.executeType = executeType;
	}

}
