package six.com.crawler.node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午9:41:26
 */
public enum NodeType {

	/**
	 * 单机节点
	 */
	SINGLE(0),
	/**
	 * 集群主节点
	 */
	MASTER(1),

	/**
	 * 集群工作节点
	 */
	WORKER(2);

	final int value;

	NodeType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static NodeType valueOf(int type) {
		if (0 == type) {
			return SINGLE;
		} else if (1 == type) {
			return MASTER;
		} else if (2 == type) {
			return WORKER;
		} else {
			return WORKER;
		}
	}
}
