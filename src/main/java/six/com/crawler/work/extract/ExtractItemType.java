package six.com.crawler.work.extract;

import java.io.Serializable;

/**
 * @author six
 * @date 2016年8月24日 下午5:26:58
 */
public enum ExtractItemType implements Serializable {
	STRING(1), // 字符
	URL(2), // URL
	TEXT(3), // 文本
	PHONE(4), // 电话
	NUMBER(5), // 数字
	DATE(6), // 日期
	META(7); // 元数据

	private final int value;

	ExtractItemType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static ExtractItemType valueOf(int type) {
		if (1 == type) {
			return STRING;
		} else if (2 == type) {
			return URL;
		} else if (3 == type) {
			return TEXT;
		} else if (4 == type) {
			return PHONE;
		} else if (5 == type) {
			return NUMBER;
		} else if (6 == type) {
			return DATE;
		} else if (7 == type) {
			return META;
		} else {
			return STRING;
		}

	}
}
