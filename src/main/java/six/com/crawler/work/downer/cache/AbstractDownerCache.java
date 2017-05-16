package six.com.crawler.work.downer.cache;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午5:58:27
 */
public abstract class AbstractDownerCache implements DownerCache {

	protected final static Logger log = LoggerFactory.getLogger(AbstractDownerCache.class);

	private String siteCode;
	private AtomicBoolean writeCacheEnd = new AtomicBoolean(false);
	private LinkedBlockingQueue<Page> writeCacheQueue = new LinkedBlockingQueue<Page>();
	private Thread cacheThread;

	public AbstractDownerCache(String siteCode) {
		this.siteCode = siteCode;
		cacheThread = new Thread(() -> {
			loopDoWirte();
		}, "downer-write-cache-thread");
		cacheThread.start();
	}

	@Override
	public final void write(Page page) {
		writeCacheQueue.add(page);
	}

	@Override
	public final Page read(Page page) {
		return doRead(page);
	}

	private void loopDoWirte() {
		while (!writeCacheEnd.get()) {
			Page page = null;
			while (null != (page = writeCacheQueue.poll())) {
				try {
					doWirte(page);
				} catch (Exception e) {
					log.error("donwer cache write page:" + page.toString(), e);
					writeCacheQueue.add(page);
				}
			}
		}
	}

	protected abstract void doWirte(Page page);

	protected abstract Page doRead(Page page);

	public String getSiteCode() {
		return siteCode;
	}

	@Override
	public void close() {
		writeCacheEnd.set(true);
	}

}
