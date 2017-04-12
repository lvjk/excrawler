package six.com.crawler.node.lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午10:01:38
 * 
 *       基于 Constructor InterProcessMutex 的分布式读锁
 */
public class DistributedReadLock implements DistributedLock {

	private InterProcessMutex interProcessMutex;

	public DistributedReadLock(InterProcessReadWriteLock interProcessReadWriteLock) {
		this.interProcessMutex = interProcessReadWriteLock.readLock();
	}

	@Override
	public void lock() throws Exception {
		interProcessMutex.acquire();
	}

	@Override
	public void unLock() throws Exception {
		interProcessMutex.release();
	}

}
