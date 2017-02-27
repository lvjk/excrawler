package six.com.crawler.algorithm;

/**
 * @author six
 * @date 2016年8月1日 下午2:46:15
 */
public enum Role {
	/**
	 * 农民工
	 */
	Migrant(0), // 农民工
	/**
	 * 工人
	 */
	Worker(1), // 工人
	/**
	 * 文员
	 */
	Clerk(2), // 文员
	/**
	 * 公务员
	 */
	CivilServant(3), // 公务员
	/**
	 * 工程师
	 */
	Engineer(4), // 工程师
	/**
	 * 管理者
	 */
	Controller(5), // 管理者
	/**
	 * 老板
	 */
	Boss(6);// 老板

	private final int value;

	Role(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static Role paser(int role) {
		if (0 > role || role > 4) {
			throw new IllegalArgumentException("educationBackground<0 || educationBackground>6");
		}
		if (0 == role) {
			return Migrant;
		} else if (1 == role) {
			return Worker;
		} else if (2 == role) {
			return Clerk;
		} else if (3 == role) {
			return CivilServant;
		} else if (4 == role) {
			return Engineer;
		} else if (5 == role) {
			return Controller;
		} else {
			return Boss;
		}
	}
}
