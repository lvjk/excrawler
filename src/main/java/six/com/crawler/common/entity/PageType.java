package six.com.crawler.common.entity;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 下午2:11:43
 */
public enum PageType {
	LISTING(1), DATA(2), XML(3), JSON(4);

	final int value;

	PageType(int value) {
		this.value = value;
	}
	
	public int value(){
		return value;
	}

	public static PageType valueOf(int type) {
		if (1 == type) {
			return LISTING;
		} else if (2 == type) {
			return DATA;
		} else if (3 == type) {
			return XML;
		} else if (4 == type) {
			return JSON;
		} else {
			return LISTING;
		}

	}
}
