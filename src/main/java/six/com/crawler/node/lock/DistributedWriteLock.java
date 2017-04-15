package six.com.crawler.node.lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午10:04:42
 * 
 *       基于 Constructor InterProcessMutex 的分布式写锁
 */
public class DistributedWriteLock implements DistributedLock {

	final static Logger log = LoggerFactory.getLogger(DistributedWriteLock.class);
	private String path;
	private InterProcessMutex interProcessMutex;

	public DistributedWriteLock(String path, InterProcessReadWriteLock interProcessReadWriteLock) {
		this.path = path;
		this.interProcessMutex = interProcessReadWriteLock.writeLock();
	}

	@Override
	public void lock() {
		try {
			interProcessMutex.acquire();
		} catch (Exception e) {
			log.error("distributed writeLock path:" + path, e);
		}
	}

	@Override
	public void unLock() {
		try {
			interProcessMutex.release();
		} catch (Exception e) {
			log.error("distributed writeLock path:" + path, e);
		}
	}
}
