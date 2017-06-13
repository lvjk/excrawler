package six.com.crawler.entity;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月12日 上午10:29:28
 */
public enum JobSnapshotStatus implements Serializable {

	/**
	 * 准备
	 */
	READY(1),

	/**
	 * 等待被执行
	 */
	WAITING_EXECUTED(2),

	/**
	 * 正在执行
	 */
	EXECUTING(3),

	/**
	 * 暂停
	 */
	SUSPEND(4),

	/**
	 * 停止
	 */
	STOP(5),

	/**
	 * 完成
	 */
	FINISHED(6);

	private final int value;

	private JobSnapshotStatus(int value) {
		this.value = value;
	}

	public static JobSnapshotStatus valueOf(int type) {
		if (1 == type) {
			return READY;
		} else if (2 == type) {
			return WAITING_EXECUTED;
		} else if (3 == type) {
			return EXECUTING;
		} else if (4 == type) {
			return SUSPEND;
		} else if (5 == type) {
			return STOP;
		} else if (6 == type) {
			return FINISHED;
		}  else {
			return READY;
		}

	}

	public int value() {
		return value;
	}

}
