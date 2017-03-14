package six.com.crawler.common.entity;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午9:41:26
 */
public enum NodeType {

	//主节点           备用主节点                            工作节点          主节点工作节点
	MASTER(0), MASTER_STANDBY(1), WORKER(2),MASTER_WORKER(3);

	final int value;

	NodeType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static NodeType valueOf(int type) {
		if (0 == type) {
			return MASTER;
		} else if (1 == type) {
			return MASTER_STANDBY;
		} else if (2 == type) {
			return WORKER;
		}  else if (3 == type) {
			return MASTER_WORKER;
		}else {
			return WORKER;
		}

	}
}
