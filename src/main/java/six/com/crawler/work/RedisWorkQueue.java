package six.com.crawler.work;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月21日 上午10:08:42 队列key 是由队列前缀key+任务name 由于redis支持rpush
 *       lpop队列操作, 利用2个key(存储数据key,存储数据) 来时实现redis队列ack: 1.hset处理数据,
 *       然后rpush数据key 2.lpop 数据key, 然后通过数据key hget获取数据, 3.等数据实际处理完后再hdel数据
 */
public class RedisWorkQueue implements WorkQueue {

	final static Logger LOG = LoggerFactory.getLogger(RedisWorkQueue.class);

	//redis page 代理队列 key 前缀
	public final static String PRE_PROXY_QUEUE_KEY = "spider_redis_store_page_proxy_queue_";
	//redis page 队列 key 前缀
	public final static String PRE_QUEUE_KEY = "spider_redis_store_page_queue_";
	
	public final static String PRE_ERR_QUEUE_KEY = "spider_redis_store_page_err_queue_";
	//redis 处理过数据 key 去重前缀
	public final static String PRE_DONE_DUPLICATE_KEY = "spider_redis_store_page_done_duplicate_";
	
	private RedisManager redisManager;

	private String proxyQueueKey;

	private String queueKey;
	
	protected String errQueueKey;

	private String doneDuplicateKey;

	public RedisWorkQueue(RedisManager redisManager,String queueName) {
		this.redisManager = redisManager;
		proxyQueueKey = PRE_PROXY_QUEUE_KEY.concat(queueName);
		queueKey = PRE_QUEUE_KEY.concat(queueName);
		errQueueKey=PRE_ERR_QUEUE_KEY.concat(queueName);
		doneDuplicateKey = PRE_DONE_DUPLICATE_KEY.concat(queueName);
	}

	@Override
	public Page pull() {
		Page page = null;
		// 加锁
		redisManager.lock(queueKey);
		try {
			// 先从代理队列里获取头元素数据key 并移除
			String dataKey = redisManager.lpop(proxyQueueKey, String.class);
			if (null != dataKey) {
				// 通过数据key 再获取数据
				page = redisManager.hget(queueKey, dataKey, Page.class);
				if (null == page) {
					throw new RuntimeException("don't find value by data's key[" + dataKey + "] from queue");
				}
			}
		} finally {
			redisManager.unlock(queueKey);
		}
		return page;
	}

	/**
	 * 填充代理队列数据 将实践存储的数据填充到代理队列
	 */
	public void repair() {
		redisManager.lock(queueKey);
		try {
			int proxyQueueLlen = redisManager.llen(proxyQueueKey);
			int queueKeyLlen = redisManager.hllen(queueKey);
			if (queueKeyLlen != proxyQueueLlen) {
				redisManager.del(proxyQueueKey);
				Map<String, Page> findMap = redisManager.hgetAll(queueKey, Page.class);
				if (null != findMap) {
					for (String key : findMap.keySet()) {
						redisManager.rpush(proxyQueueKey, key);
					}
				}
			}
		} finally {
			redisManager.unlock(queueKey);
		}
	}

	// 找到url 添加 判断url是否处理过或者在待处理队列？ 如果没有那么加入队列
	@Override
	public void finish(Page page) {
		String key = page.getPageKey();
		// 加锁
		redisManager.lock(doneDuplicateKey);
		try {
			redisManager.hdel(queueKey, key);
			redisManager.hset(doneDuplicateKey, key,page.getFinalUrl());
		} finally {
			// 解锁
			redisManager.unlock(doneDuplicateKey);
		}

	}

	@Override
	public int size() {
		return redisManager.llen(proxyQueueKey);
	}

	@Override
	public void push(Page page) {
		if (null != page) {
			String key = page.getPageKey();
			// 加锁
			redisManager.lock(queueKey);
			try {
				// 判断 队列里是否有重复的数据
				Page data = redisManager.hget(queueKey, key, Page.class);
				if (null == data) {
					// 再存入实际数据
					redisManager.hset(queueKey, key, page);
					// 先想代理队列放入数据key
					redisManager.rpush(proxyQueueKey, key);
				}
			} finally {
				// 解锁
				redisManager.unlock(queueKey);
			}
		}
	}

	@Override
	public void clear() {
		redisManager.del(proxyQueueKey);
		redisManager.del(queueKey);
	}

	/**
	 * 先根据历史数据判断urlMd5是否处理过
	 */
	@Override
	public boolean duplicateKey(String pageKey) {
		boolean isduplicate = false;
		if (StringUtils.isNotBlank(pageKey)) {
			// 加锁判
			redisManager.lock(doneDuplicateKey);
			try {
				// 先根据历史数据判断urlMd5是否处理过
				isduplicate = redisManager.isExecuted(doneDuplicateKey, pageKey);
			} finally {
				// 解锁
				redisManager.unlock(doneDuplicateKey);
			}
		}
		return isduplicate;
	}

	@Override
	public void addDuplicateKey(String pageKey,String url) {
		// 加锁判断 队列里是否有重复的数据
		redisManager.lock(doneDuplicateKey);
		try {
			redisManager.hset(doneDuplicateKey, pageKey,url);
		} finally {
			// 解锁
			redisManager.unlock(doneDuplicateKey);
		}

	}

	/**
	 * size==0
	 */
	@Override
	public boolean isEmptyForWaiting() {
		return size() == 0;
	}

	@Override
	public void retryPush(Page page) {
		if (null != page) {
			String dataKey = page.getPageKey();
			Page data = null;
			// 加锁
			redisManager.lock(queueKey);
			try {
				// 先将数据key放入代理队列
				redisManager.rpush(proxyQueueKey, dataKey);
				// 判断 队列里是否有重复的数据
				data = redisManager.hget(queueKey, dataKey, Page.class);
				if (null == data) {
					// 再存入实际数据
					redisManager.hset(queueKey, dataKey, page);
				}
			} finally {
				// 解锁
				redisManager.unlock(queueKey);
			}
		}
	}

	@Override
	public void pushErr(Page page) {
		//redisManager.hset(errQueueKey, page.getPageKey(),page);
	}
}
