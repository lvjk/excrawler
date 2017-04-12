package six.com.crawler.node.lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午10:04:42
 * 
 *       基于 Constructor InterProcessMutex 的分布式写锁
 */
public class DistributedWriteLock implements DistributedLock {

	private InterProcessMutex interProcessMutex;

	public DistributedWriteLock(InterProcessReadWriteLock interProcessReadWriteLock) {
		this.interProcessMutex = interProcessReadWriteLock.writeLock();
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
