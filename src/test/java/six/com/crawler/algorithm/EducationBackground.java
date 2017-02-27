package six.com.crawler.algorithm;

/**
 * @author six
 * @date 2016年8月1日 下午2:40:14 学历背景
 */
public enum EducationBackground {
	/**
	 * 小学
	 */
	Primary(0), // 小学
	/**
	 * 初中
	 */
	JuniorMiddle(1), // 初中
	/**
	 * 高中
	 */
	High(2), // 高中
	/**
	 * 大专
	 */
	JuniorCollege(3), // 大专
	/**
	 *  本科
	 */
	Undergraduate(4), // 本科
	/**
	 *硕士
	 */
	Master(5), // 硕士
	/**
	 * 博士
	 */
	Doctor(6);// 博士

	private final int value;

	EducationBackground(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static EducationBackground paser(int educationBackground) {
		if (0 > educationBackground || educationBackground > 6) {
			throw new IllegalArgumentException("educationBackground<0 || educationBackground>6");
		}
		if (0 == educationBackground) {
			return Primary;
		} else if (1 == educationBackground) {
			return JuniorMiddle;
		} else if (2 == educationBackground) {
			return High;
		} else if (3 == educationBackground) {
			return JuniorCollege;
		} else if (4 == educationBackground) {
			return Undergraduate;
		} else if (5 == educationBackground) {
			return Master;
		} else {
			return Doctor;
		}
	}
}
