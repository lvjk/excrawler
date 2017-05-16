package six.com.crawler.work;

import java.io.Serializable;

public enum WorkerLifecycleState  implements Serializable{
	/**
	 * 准备
	 */
	READY(WorkerLifecycle.READY),
	/**
	 * 开始
	 */
	STARTED(WorkerLifecycle.STARTED),
	
	/**
	 * 等待
	 */
	WAITED(WorkerLifecycle.WAITED),
	
	/**
	 * 等待
	 */
	REST(WorkerLifecycle.REST),
	/**
	 * 暂停
	 */
	SUSPEND(WorkerLifecycle.SUSPEND),

	/**
	 * 停止
	 */
	STOPED(WorkerLifecycle.STOPED),
	
	
	/**
	 * 完成
	 */
	FINISHED(WorkerLifecycle.FINISHED),

	/**
	 * 销毁
	 */
	DESTROY(WorkerLifecycle.DESTROY);

	private final String event;

	private WorkerLifecycleState(String event) {
		this.event = event;
	}

	public String getEvent() {
		return event;
	}
}
