package six.com.crawler.algorithm;

/**
 * @author six
 * @date 2016年8月1日 下午2:50:04
 */
public enum GroupType {
	/**
	 * 企事业单位
	 */
	Enterprise(0), // 企事业单位
	/**
	 * 政府机构
	 */
	Government(1), // 政府机构
	/**
	 * 小公司
	 */
	SmallCompany(2), // 小公司
	/**
	 * 中型公司
	 */
	MiddleCompany(3), // 中型公司
	/**
	 * 大公司
	 */
	BigCompany(4);// 大公司

	private final int value;

	GroupType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static GroupType paser(int groupType) {
		if (0 > groupType || groupType > 4) {
			throw new IllegalArgumentException("GroupType<0 || GroupType>4");
		}
		if (0 == groupType) {
			return Enterprise;
		} else if (1 == groupType) {
			return Government;
		} else if (2 == groupType) {
			return SmallCompany;
		} else if (3 == groupType) {
			return MiddleCompany;
		} else {
			return BigCompany;
		}
	}
}
