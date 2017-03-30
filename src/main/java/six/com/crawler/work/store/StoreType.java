package six.com.crawler.work.store;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午11:34:02
 */
public enum StoreType {

	CONSOLE(0), DB(1), HTTP(2), FILE(3),REDIS(4);

	final int value;

	StoreType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static StoreType valueOf(int type) {
		if (0 == type) {
			return CONSOLE;
		} else if (1 == type) {
			return DB;
		} else if (2 == type) {
			return HTTP;
		} else if (3 == type) {
			return FILE;
		} else if (4 == type) {
			return REDIS;
		}  else {
			return CONSOLE;
		}

	}

}
