package six.com.common;

import six.com.crawler.common.ResourceLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月16日 下午12:17:32
 */
public class ResourceLockTest extends ResourceLock {

	int value;

	public static void main(String[] args) {
		final ResourceLockTest lock = new ResourceLockTest();
		for (int i = 0; i < 10; i++) {
			final int value = i;
			new Thread(() -> {
				String key = "key";
				lock.lock(key);
				try {
					lock.doSomeThing("Thread-" + value);
				} finally {
					lock.unlock(key);
				}
			}).start();
			;
		}
	}

	public void doSomeThing(String name) {
		System.out.println(name + " doSomeThing:" + value++);
	}

}
