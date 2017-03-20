package six.com.crawler.utils;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午10:28:33 类说明
 */
public class ThreadUtils {

	public static void sleep(long coolPeriod) {
		if (coolPeriod > 0) {
			try {
				Thread.sleep(coolPeriod);
			} catch (InterruptedException e) {
				//ignore;
			}
		}
	}

}
