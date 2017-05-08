package six.com.crawler.work.store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.space.RedisWorkSpace;
import six.com.crawler.work.store.exception.StoreException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月29日 上午11:25:00
 */
public class RedisStore extends AbstarctStore {

	private final static String REDIS_SOTRE_KEY_PRE = "redisStore";

	private RedisWorkSpace<RedisStoreData> redisStoreQueue;

	private AtomicInteger storeCount = new AtomicInteger();

	public RedisStore(AbstractWorker<?> worker) {
		super(worker);
		String redisStoreKey = REDIS_SOTRE_KEY_PRE + "_" + worker.getJobSnapshot().getName() + "_"
				+ worker.getJobSnapshot().getId();
		redisStoreQueue = new RedisWorkSpace<RedisStoreData>(worker.getManager().getRedisManager(), redisStoreKey,
				RedisStoreData.class);
	}

	@Override
	protected int insideStore(List<Map<String, String>> results) throws StoreException {
		int storeCount = 0;
		for (Map<String, String> result : results) {
			RedisStoreData redisStoreData = new RedisStoreData();
			redisStoreData.setDataMap(result);
			redisStoreData.setKey(getRedisStoreDataKey());
			redisStoreQueue.push(redisStoreData);
			storeCount++;
		}
		return storeCount;
	}

	/**
	 * 因为worker name 肯定是唯一的， 所以通过 worker name 再加上计数 就可以保证 key唯一
	 * 
	 * @return
	 */
	private String getRedisStoreDataKey() {
		String key = getWorker().getName() + "_" + storeCount.getAndIncrement();
		return key;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
