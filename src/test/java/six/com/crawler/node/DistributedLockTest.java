package six.com.crawler.node;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月15日 下午12:09:47
 */
public class DistributedLockTest extends BaseTest {

	@Test
	public void test() {
		DistributedLock lock = clusterManager.getReadLock("test");
		try {
			lock.lock();
			System.out.println("hellow");
		} finally {
			lock.unLock();
		}
	}
}
