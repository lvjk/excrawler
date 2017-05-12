package six.com.crawler.work.space;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.dao.RedisManager;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月10日 下午4:30:42
 */
public class SegmentRedisWorkSpace<T extends WorkSpaceData> implements WorkSpace<T> {

	final static Logger log = LoggerFactory.getLogger(RedisWorkSpace.class);

	public final static String WORK_QUEUE_KEY_PRE = "workspace_doing_queue_";

	public final static String WORK_ERR_QUEUE_KEY_PRE = "workspace_err_queue_";

	public final static String WORK_DONE_QUEUE_KEY_PRE = "workspace_done_queue_";

	public final static int batch = 10000;

	private final String workSpaceName;

	private final String queueKey;

	private final String doneKey;

	private final String errQueueKey;

	private final Class<T> clz;

	private RedisManager redisManager;

	private DistributedLock distributedLock;

	private SegmentQueue segmentQueue;

	public SegmentRedisWorkSpace(RedisManager redisManager, DistributedLock distributedLock, String workSpaceName,
			Class<T> clz) {
		Objects.requireNonNull(redisManager, "redisManager must not be null");
		Objects.requireNonNull(workSpaceName, "workSpaceName must not be null");
		Objects.requireNonNull(clz, "clz must not be null");
		this.redisManager = redisManager;
		this.distributedLock = distributedLock;
		this.workSpaceName = workSpaceName;
		this.queueKey = WORK_QUEUE_KEY_PRE + workSpaceName;
		this.errQueueKey = WORK_ERR_QUEUE_KEY_PRE + workSpaceName;
		this.doneKey = WORK_DONE_QUEUE_KEY_PRE + workSpaceName;
		this.clz = clz;
		this.segmentQueue = new SegmentQueue(workSpaceName, redisManager);
	}

	public String getName() {
		return workSpaceName;
	}

	@Override
	public boolean push(T data) {
		Objects.requireNonNull(data, "data must not be null");
		distributedLock.lock();
		try {
			String doingKey = getDoingKey(data.getKey());
			if (!redisManager.isExecuted(doingKey)) {
				redisManager.set(doingKey, data);
				segmentQueue.push(doingKey);
				log.info("push workSpace[" + queueKey + "] data succeed:" + data.toString());
			} else {
				log.info("workSpace[" + queueKey + "] contained data:" + data.toString());
			}
			return true;
		} catch (Exception e) {
			log.error("push workSpace[" + queueKey + "] data err:" + data.toString(), e);
			throw e;
		} finally {
			distributedLock.unLock();
		}
	}

	@Override
	public boolean errRetryPush(T data) {
		Objects.requireNonNull(data, "data must not be null");
		distributedLock.lock();
		String doingKey = getDoingKey(data.getKey());
		try {
			redisManager.set(doingKey, data);
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			distributedLock.unLock();
		}
	}

	@Override
	public T pull() {
		T data = null;
		try {
			distributedLock.lock();
			String dataKey = segmentQueue.poll();
			if (null != dataKey) {
				data = redisManager.get(dataKey, clz);
			}
		} catch (Exception e) {
			log.error("pull workSpace[" + queueKey + "] data err", e);
			throw e;
		} finally {
			distributedLock.unLock();
		}
		return data;
	}

	public void repair() {
		try {
			distributedLock.lock();
			Set<String> dataKeys = getDoingKeys();
			if (null != dataKeys) {
				for (String dataKey : dataKeys) {
					segmentQueue.push(dataKey);
				}
			}
		} catch (Exception e) {
			log.error("repair workSpace[" + queueKey + "] err", e);
			throw e;
		} finally {
			distributedLock.unLock();
		}
	}

	public String batchGetDoingData(List<T> resutList, String cursorStr) {
		resutList = Collections.emptyList();
		return "";
	}

	public String batchGetErrData(List<T> resutList, String cursorStr) {
		return batchGet(resutList, cursorStr, errQueueKey);
	}

	public String batchGetDoneData(List<String> resutList, String cursorStr) {
		Objects.requireNonNull(resutList, "resutList must not be null");
		if (StringUtils.isBlank(cursorStr)) {
			cursorStr = "0";
		}
		Map<String, String> map = new HashMap<>();
		cursorStr = redisManager.hscan(doneKey, cursorStr, map, String.class);
		resutList.addAll(map.keySet());
		return cursorStr;
	}

	private String batchGet(List<T> resutList, String cursorStr, String type) {
		Objects.requireNonNull(resutList, "resutList must not be null");
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
			distributedLock.lock();
			try {
				isduplicate = redisManager.isExecuted(getDoneKey(key));
			} catch (Exception e) {
				throw e;
			} finally {
				distributedLock.unLock();
			}
		}
		return isduplicate;
	}

	@Override
	public void addDone(String key) {
		if (StringUtils.isNotBlank(key)) {
			distributedLock.lock();
			try {
				redisManager.set(getDoneKey(key), "1");
			} catch (Exception e) {
				throw e;
			} finally {
				distributedLock.unLock();
			}
		}
	}

	public void ack(T data) {
		Objects.requireNonNull(data, "data must not be null");
		distributedLock.lock();
		String doingKey = getDoingKey(data.getKey());
		try {
			redisManager.del(doingKey);
		} catch (Exception e) {
			throw e;
		} finally {
			distributedLock.unLock();
		}

	}

	@Override
	public int doingSize() {
		Set<String> dataSetKeys = getDoingKeys();
		return null == dataSetKeys ? 0 : dataSetKeys.size();
	}

	@Override
	public int errSize() {
		return redisManager.hllen(errQueueKey);
	}

	@Override
	public int doneSize() {
		Set<String> dataSetKeys = getDoneKeys();
		return null == dataSetKeys ? 0 : dataSetKeys.size();
	}

	@Override
	public void clearDoing() {
		distributedLock.lock();
		try {
			segmentQueue.clear();
			Set<String> list = getDoingKeys();
			if (null != list) {
				for (String dataKey : list) {
					redisManager.del(dataKey);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			distributedLock.unLock();
		}
	}

	@Override
	public void clearErr() {
		distributedLock.lock();
		try {
			redisManager.del(errQueueKey);
		} catch (Exception e) {
			throw e;
		} finally {
			distributedLock.unLock();
		}
	}

	@Override
	public void clearDone() {
		distributedLock.lock();
		try {
			Set<String> list = getDoneKeys();
			if (null != list) {
				for (String dataKey : list) {
					redisManager.del(dataKey);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			distributedLock.unLock();
		}
	}

	private Set<String> getDoingKeys() {
		String newDoingKey = queueKey + "_*";
		Set<String> list = redisManager.keys(newDoingKey);
		return list;
	}

	private String getDoingKey(String key) {
		String newDoingKey = queueKey + "_" + key;
		return newDoingKey;
	}

	private Set<String> getDoneKeys() {
		String newDoneKey = doneKey + "_*";
		Set<String> list = redisManager.keys(newDoneKey);
		return list;
	}

	private String getDoneKey(String key) {
		String newDoneKey = doneKey + "_" + key;
		return newDoneKey;
	}

}
