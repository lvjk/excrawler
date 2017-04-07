package six.com.crawler.work.space;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.RedisManager;
import six.com.crawler.utils.ObjectCheckUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月21日 上午10:08:42 工作队列key 是由队列前缀key+任务name 由于redis支持rpush
 *       lpop队列操作, 利用2个key(存储数据key,存储数据) 来时实现redis队列ack: 1.hset处理数据,
 *       然后rpush数据key 2.lpop 数据key, 然后通过数据key hget获取数据, 3.等数据实际处理完后再hdel数据
 */
public class RedisWorkSpace<T extends WorkSpaceData> implements WorkSpace<T> {

	final static Logger LOG = LoggerFactory.getLogger(RedisWorkSpace.class);

	public final static String WORK_PROXY_QUEUE_KEY_PRE = "spider_redis_store_page_proxy_queue_";

	public final static String WORK_QUEUE_KEY_PRE = "spider_redis_store_page_queue_";

	public final static String WORK_ERR_QUEUE_KEY_PRE = "spider_redis_store_page_err_queue_";

	public final static String WORK_DONE_QUEUE_KEY_PRE = "spider_redis_store_page_done_duplicate_";

	public final static int batch = 10000;

	private String workSpaceName;

	private RedisManager redisManager;

	private String proxyQueueKey;

	private String queueKey;

	private String doneKey;

	private String errQueueKey;

	private Class<T> clz;

	public RedisWorkSpace(RedisManager redisManager, String workSpaceName, Class<T> clz) {
		ObjectCheckUtils.checkNotNull(redisManager, "redisManager");
		ObjectCheckUtils.checkNotNull(workSpaceName, "workSpaceName");
		ObjectCheckUtils.checkNotNull(clz, "clz");
		this.redisManager = redisManager;
		this.workSpaceName = workSpaceName;
		this.proxyQueueKey = WORK_PROXY_QUEUE_KEY_PRE + workSpaceName;
		this.queueKey = WORK_QUEUE_KEY_PRE + workSpaceName;
		this.errQueueKey = WORK_ERR_QUEUE_KEY_PRE + workSpaceName;
		this.doneKey = WORK_DONE_QUEUE_KEY_PRE + workSpaceName;
		this.clz = clz;
	}

	public String getName() {
		return workSpaceName;
	}

	@Override
	public boolean push(T data) {
		ObjectCheckUtils.checkNotNull(data, "data");
		redisManager.lock(queueKey);
		try {
			redisManager.hset(queueKey, data.getKey(), data);
			redisManager.rpush(proxyQueueKey, data.getKey());
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(queueKey);
		}
	}

	@Override
	public T pull() {
		T data = null;
		boolean isRepair = false;
		boolean againGet = false;
		while (!isRepair || againGet) {
			// 先从代理队列里获取头元素数据key 并移除
			String dataKey = redisManager.lpop(proxyQueueKey, String.class);
			if (isRepair && null == dataKey) {
				break;
			} else {
				if (null != dataKey) {
					// 通过数据key 再获取数据
					data = redisManager.hget(queueKey, dataKey, clz);
					if (null == data) {
						throw new RuntimeException("don't find value by data's key[" + dataKey + "] from queue");
					}
					break;
				} else {
					repairDoingData();
					isRepair = true;
					againGet = true;
				}
			}
		}
		return data;
	}

	/**
	 * 填充代理队列数据 将实践存储的数据填充到代理队列
	 */
	private void repairDoingData() {
		redisManager.lock(queueKey);
		try {
			int proxyQueueLlen = redisManager.llen(proxyQueueKey);
			int queueKeyLlen = redisManager.hllen(queueKey);
			if (queueKeyLlen != proxyQueueLlen) {
				redisManager.del(proxyQueueKey);
				String cursorStr = "0";
				Map<String, T> map = new HashMap<>();
				do {
					cursorStr = redisManager.hscan(queueKey, cursorStr, map, clz);
					map.keySet().stream().forEach(mapKey -> {
						redisManager.rpush(proxyQueueKey, mapKey);
					});
					map.clear();
				} while (!"0".equals(cursorStr));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(queueKey);
		}
	}

	public String batchGetDoingData(List<T> resutList, String cursorStr) {
		return batchGet(resutList, cursorStr, queueKey);
	}

	public String batchGetErrData(List<T> resutList, String cursorStr) {
		return batchGet(resutList, cursorStr, errQueueKey);
	}

	private String batchGet(List<T> resutList, String cursorStr, String type) {
		ObjectCheckUtils.checkNotNull(resutList, "resutList");
		if (StringUtils.isBlank(cursorStr)) {
			cursorStr = "0";
		}
		Map<String, T> map = new HashMap<>();
		cursorStr = redisManager.hscan(type, cursorStr, map, clz);
		resutList.addAll(map.values());
		return cursorStr;
	}

	public void addErr(T data) {
		redisManager.hset(errQueueKey, data.getKey(), data);
	}

	public void againDoErrQueue() {
		Map<String, T> map = new HashMap<>();
		String cursorStr = "0";
		do {
			cursorStr = redisManager.hscan(errQueueKey, cursorStr, map, clz);
			map.values().stream().forEach(data -> {
				push(data);
			});
			map.clear();
		} while (!"0".equals(cursorStr));
		redisManager.del(errQueueKey);
	}

	@Override
	public boolean isDone(String key) {
		boolean isduplicate = false;
		if (StringUtils.isNotBlank(key)) {
			redisManager.lock(doneKey);
			try {
				isduplicate = redisManager.isExecuted(doneKey, key);
			} catch (Exception e) {
				throw e;
			} finally {
				redisManager.unlock(doneKey);
			}
		}
		return isduplicate;
	}

	@Override
	public void addDone(T data) {
		redisManager.lock(doneKey);
		try {
			redisManager.hset(doneKey, data.getKey(), "1");
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(doneKey);
		}
	}

	public void ack(T data) {
		ObjectCheckUtils.checkNotNull(data, "data");
		redisManager.lock(queueKey);
		try {
			redisManager.hdel(queueKey, data.getKey());
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(queueKey);
		}

	}

	@Override
	public int doingSize() {
		return redisManager.hllen(queueKey);
	}

	@Override
	public int errSize() {
		return redisManager.hllen(errQueueKey);
	}

	@Override
	public int doneSize() {
		return redisManager.hllen(doneKey);
	}

	@Override
	public void clearDoing() {
		redisManager.lock(queueKey);
		try {
			redisManager.del(proxyQueueKey);
			redisManager.del(queueKey);
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(queueKey);
		}
	}

	@Override
	public void clearErr() {
		redisManager.lock(queueKey);
		try {
			redisManager.del(errQueueKey);
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(queueKey);
		}
	}

	@Override
	public void clearDone() {
		redisManager.lock(queueKey);
		try {
			redisManager.del(doneKey);
		} catch (Exception e) {
			throw e;
		} finally {
			redisManager.unlock(queueKey);
		}
	}
}
