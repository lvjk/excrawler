package six.com.crawler.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author six
 * @date 2016年6月24日 下午5:59:32
 */
public abstract class ReferenceResource {

	protected final AtomicInteger refCount = new AtomicInteger(0);
	protected volatile boolean isWait = false;
	static final Lock lock = new ReentrantLock();// 锁
	static final Condition holdWait = lock.newCondition();// hold信号

	/**
	 * 资源HOLD住 如果被其他使用那么 当前线程会等待 直到 引用=0 然后 引用加1
	 */
	public  void hold() {
		while (this.refCount.get() > 0) {
			lock.lock();
			isWait = true;
			try {
				holdWait.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				lock.unlock();
			}
		}
		this.refCount.incrementAndGet();
	}

	/**
	 * 释放资源
	 * 
	 */
	public  void release() {
		int ref = this.refCount.decrementAndGet();
		if (ref < 0) {
			throw new RuntimeException("Illegal release");
		} else {
			if (isWait) {
				lock.lock();
				isWait = false;
				try {
					holdWait.signal();
				} finally {
					lock.unlock();
				}
			}
		}
	}
}
