package six.com.crawler.schedule;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author six
 * @date 2016年8月29日 下午3:10:52 job worker thread 工厂 用来统一命名
 */
public class JobWorkerThreadFactory implements ThreadFactory {
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(0);
	private final String namePrefix;

	JobWorkerThreadFactory() {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = "JobWorker-thread-";
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon()){
			t.setDaemon(false);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY){
			t.setPriority(Thread.NORM_PRIORITY);
		}	
		return t;
	}

}
