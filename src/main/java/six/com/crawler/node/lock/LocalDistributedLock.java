package six.com.crawler.node.lock;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午10:01:38
 * 
 *       基于 Constructor InterProcessMutex 的分布式读锁
 */
public class LocalDistributedLock implements DistributedLock {

	final static Logger log = LoggerFactory.getLogger(LocalDistributedLock.class);

	private String path;
	private static Interner<String> keyLock = Interners.<String>newWeakInterner();
	private static Map<String, ReentrantLockProxy> lockMap = new WeakHashMap<>();

	public LocalDistributedLock(String path) {
		this.path = path;
	}

	@Override
	public void lock() {
		synchronized (keyLock.intern(path)) {
			ReentrantLockProxy lock = lockMap.get(path);
			if (null == lock) {
				lock = new ReentrantLockProxy();
				lockMap.put(path, lock);
			}
			lock.count.incrementAndGet();
			lock.lock.lock();
		}

	}

	@Override
	public void unLock() {
		ReentrantLockProxy lock = lockMap.get(path);
		lock.lock.unlock();
		if (lock.count.decrementAndGet() == 0) {
			lockMap.remove(path);
		}
	}

	static class ReentrantLockProxy {
		ReentrantLock lock = new ReentrantLock();
		AtomicInteger count = new AtomicInteger(0);
	}

}
