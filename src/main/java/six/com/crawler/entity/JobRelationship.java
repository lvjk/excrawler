package six.com.crawler.entity;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月14日 下午3:54:25  job 组
 */
public class JobRelationship extends BasePo{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 2801633321033342662L;
	
	/**
	 * 串行
	 */
	public static int TRIGGER_TYPE_SERIAL =1;
	
	/**
	 * 并行
	 */
	public static int TRIGGER_TYPE_PARALLEL=2;
	
	
	private String currentJobName;
	private String nextJobName;
	private int triggerType;
	
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
	public int getTriggerType() {
		return triggerType;
	}
	public void setTriggerType(int triggerType) {
		this.triggerType = triggerType;
	}

}
