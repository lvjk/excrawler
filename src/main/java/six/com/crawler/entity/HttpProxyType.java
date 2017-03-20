package six.com.crawler.entity;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月14日 上午9:29:21
 */
public enum HttpProxyType {
	// 禁用
	DISABLE(0),
	// 启用一个
	ENABLE_ONE(1),
	// 启用多个
	ENABLE_MANY(2),
	// 启用阿布
	ENABLE_ABU(3);

	private final int value;

	HttpProxyType(int value) {
		this.value = value;
	}

	public int get() {
		return value;
	}

	public static HttpProxyType valueOf(int type) {
		if (0 == type) {
			return DISABLE;
		} else if (1 == type) {
			return ENABLE_ONE;
		} else if (2 == type) {
			return ENABLE_MANY;
		} else if (3 == type) {
			return ENABLE_ABU;
		} else {
			return DISABLE;
		}

	}

}
