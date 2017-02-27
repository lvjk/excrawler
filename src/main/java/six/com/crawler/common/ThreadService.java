package six.com.crawler.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author six
 * @date 2016年6月27日 下午2:52:49
 */
public abstract class ThreadService implements Runnable {

	// 执行线程
	protected final Thread thread;
	// 线程回收时间，默认90S
	private static final long JoinTime = 90 * 1000;
	// 是否已经被Notify过
	protected volatile boolean hasNotified = false;
	// 线程是否已经停止
	protected volatile boolean stoped = false;
	//用来记录线程数id
	private static final AtomicInteger threadId = new AtomicInteger(0);

	public ThreadService(boolean daemon,String name) {
		this.thread = new Thread(this);
		thread.setDaemon(daemon);
		thread.setName(name);
	}

	public void start() {
		this.thread.start();
	}

	public void shutdown() {
		this.shutdown(false);
	}

	public void stop() {
		this.stop(false);
	}

	public void makeStop() {
		this.stoped = true;
	}

	public void stop(final boolean interrupt) {
		this.stoped = true;
		synchronized (this) {
			if (!this.hasNotified) {
				this.hasNotified = true;
				this.notify();
			}
		}

		if (interrupt) {
			this.thread.interrupt();
		}
	}

	public void shutdown(final boolean interrupt) {
		this.stoped = true;
		synchronized (this) {
			if (!this.hasNotified) {
				this.hasNotified = true;
				this.notify();
			}
		}

		try {
			if (interrupt) {
				this.thread.interrupt();
			}
			if (!this.thread.isDaemon()) {
				this.thread.join(this.getJointime());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void wakeup() {
		synchronized (this) {
			if (!this.hasNotified) {
				this.hasNotified = true;
				this.notify();
			}
		}
	}

	protected void waitForRunning(long interval) {
		synchronized (this) {
			if (this.hasNotified) {
				this.hasNotified = false;
				this.onWaitEnd();
				return;
			}

			try {
				this.wait(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				this.hasNotified = false;
				this.onWaitEnd();
			}
		}
	}

	protected void onWaitEnd() {
	}

	public boolean isStoped() {
		return stoped;
	}

	public long getJointime() {
		return JoinTime;
	}

	protected static int getId(){
		return threadId.incrementAndGet();
	}
}
