package six.com.crawler.work.downer;

import java.util.concurrent.locks.StampedLock;

import six.com.crawler.entity.Page;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.cache.DbDownerCache;
import six.com.crawler.work.downer.cache.DownerCache;
import six.com.crawler.work.downer.impl.ApacheHttpDowner;
import six.com.crawler.work.downer.impl.ChromeDowner;
import six.com.crawler.work.downer.impl.OkHttpDowner;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月14日 上午10:53:52
 */
public class DownerManager {

	static final StampedLock stampedLock = new StampedLock();

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static DownerManager getInstance() {
		return DownerManagerUtils.DownerManager;
	}

	/**
	 * 单例模式 实现赖加载
	 * 
	 * @author six
	 * @email 359852326@qq.com
	 */
	private static class DownerManagerUtils {
		static DownerManager DownerManager = new DownerManager();
	}

	public Downer buildDowner(DownerType downerType, String siteCode, AbstractCrawlWorker worker, boolean openDownCache,
			boolean useDownCache) {
		Downer downer = null;
		DownerCache downerCache = null;
		if (openDownCache || useDownCache) {
			downerCache = new DbDownerCache(siteCode,worker.getJobSnapshot(), worker.getManager().getPageDao());
		} else {
			downerCache = new DownerCache() {
				@Override
				public void write(Page page) {
				}

				@Override
				public Page read(Page page) {
					return page;
				}

				@Override
				public void close() {
				}
			};
		}
		if (downerType == DownerType.OKHTTP) {
			downer = new OkHttpDowner(worker, openDownCache, useDownCache, downerCache);
		} else if (downerType == DownerType.HTTPCLIENT) {
			downer = new ApacheHttpDowner(worker, openDownCache, useDownCache, downerCache);
		} else if (downerType == DownerType.CHROME) {
			downer = new ChromeDowner(worker, openDownCache, useDownCache, downerCache);
		} else {
			downer = new OkHttpDowner(worker, openDownCache, useDownCache, downerCache);
		}
		return downer;
	}

}
