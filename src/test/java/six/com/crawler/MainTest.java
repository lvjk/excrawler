package six.com.crawler;

import org.junit.Test;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月27日 上午10:53:29
 */
public class MainTest extends BaseTest {

	@Test
	public synchronized void test() {
		try {
			this.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
