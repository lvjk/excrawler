package six.com.crawler.work.extract;

import java.io.Serializable;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月17日 下午8:17:51 类说明 解析 path 必须有值 应该有值
 */
public enum MustHaveResult implements Serializable {
	MUST(1), // 必须有值
	OTHER(0);// 其他

	final int value;

	MustHaveResult(int value) {
		this.value = value;
	}

	public int get(){
		return value;
	}
	public static MustHaveResult getMustHaveResult(int type) {
		if (1 == type) {
			return MUST;
		} else {
			return OTHER;
		}

	}
}
