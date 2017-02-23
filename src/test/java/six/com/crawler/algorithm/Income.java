package six.com.crawler.algorithm;

/**
 * @author six
 * @date 2016年8月1日 下午3:03:00
 */
public enum Income {
	/**
	 * 年收入5万以内
	 */
	Income_0(0, "年收入5万以内"), 
	/**
	 * 年收入10万以内
	 */
	Income_1(1, "年收入10万以内"), 
	/**
	 * 年收入20万以内
	 */
	Income_2(2, "年收入20万以内"), 
	/**
	 * 年收入50万以内
	 */
	Income_3(3, "年收入50万以内"), 
	/**
	 * 年收入100万以内
	 */
	Income_4(4,"年收入100万以内");

	private final int type;
	private final String describe;

	Income(int type, String describe) {
		this.type = type;
		this.describe = describe;
	}

	public int type() {
		return type;
	}

	public String describe() {
		return describe;
	}

	public static Income paser(int income) {
		if (0 > income || income > 4) {
			throw new IllegalArgumentException("income<0 || income>4");
		}
		if (0 == income) {
			return Income_0;
		} else if (1 == income) {
			return Income_1;
		} else if (2 == income) {
			return Income_2;
		} else if (3 == income) {
			return Income_3;
		} else {
			return Income_4;
		}
	}
}
