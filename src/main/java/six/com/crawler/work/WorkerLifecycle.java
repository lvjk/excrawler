package six.com.crawler.work;

import org.springframework.context.Lifecycle;

/**
 * @author six
 * @date 2016年1月15日 下午6:45:26
 */
public interface WorkerLifecycle extends Lifecycle {

	public static final String READY = "ready";// 准备

	public static final String STARTED = "started";// 运行
	
	public static final String WAITED = "waited";// 等待
	
	public static final String REST = "rest";// 休息下

	public static final String SUSPEND = "suspend";// 暂停
	
	public static final String STOPED = "stop";// 停止
	
	public static final String FINISHED = "finished";// 停止

	public static final String DESTROY = "destroy";// 销毁


	/**
	 * 开始方法 只有当 state== ready 时调用
	 */
	public void start();
	
	/**
	 * 在运行状态下，没有处理数据事等待
	 */
	public void rest();

	/**
	 * 暂停方法 只有在state==stared时候调用
	 */
	public void suspend();
	
	/**
	 * 继续运行方法只有在state==suspend时调用
	 */
	public void goOn();
	
	/**
	 * 停止方法在任何状态时候都可以调用
	 */
	public void stop();
	
	
	public void finish();

	/**
	 * 销毁 方法 最后结束时候调用
	 */
	public void destroy();

	/**
	 * 获取状态
	 * 
	 * @return
	 */
	public WorkerLifecycleState getState();

}
