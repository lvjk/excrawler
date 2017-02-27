package six.com.crawler.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author six
 * @date 2016年8月30日 下午1:45:22 资源锁
 */
public abstract class ResourceLock {

	private final static ReentrantLock lock = new ReentrantLock();
	private final static Map<String, AtomicInteger> lockMap = new ConcurrentHashMap<>();
	private final static Condition condition = lock.newCondition();

	public void lock(String key) {
		AtomicInteger index = null;
		lock.lock();
		try {
			while (true) {
				index = lockMap.computeIfAbsent(key, mapKey -> new AtomicInteger());
				if (index.get() == 0) {
					index.incrementAndGet();
					//break;
				} else {
//					try {
//						condition.await();
//					} catch (InterruptedException e) {
//						throw new RuntimeException(e);
//					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public void unlock(String key) {
		lock.lock();
		try {
			AtomicInteger index = lockMap.get(key);
			if (index.decrementAndGet() == 0) {
				lockMap.remove(key);
			}
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
}
