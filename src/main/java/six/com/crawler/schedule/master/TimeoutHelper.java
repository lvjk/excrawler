package six.com.crawler.schedule.master;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月23日 上午11:51:45
 */
public class TimeoutHelper{

	public static boolean checkTimeout(Check check, long timeOut, String errMsg) {
		long endTime, startTime = System.currentTimeMillis();
		do {
			if (check.check()) {
				return true;
			} else {
				endTime = System.currentTimeMillis();
				if ((endTime - startTime) < timeOut) {
					continue;
				} else {
					throw new RuntimeException(errMsg);
				}
			}
		} while (true);
	}

	@FunctionalInterface
	public static interface Check {
		public boolean check();
	}
}
