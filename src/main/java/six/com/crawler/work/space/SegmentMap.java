package six.com.crawler.work.space;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 下午2:57:37
 */
public class SegmentMap<T> {

	private int segmentMaxSize;
	private String segmentNames;
	private String segmentNamePre;
	private RedisManager redisManager;
	private static Random random = new Random();
	private final Class<T> clz;

	SegmentMap(String segmentNames, String segmentNamePre, RedisManager redisManager, int segmentMaxSize,
			Class<T> clz) {
		this.segmentNames = segmentNames;
		this.segmentNamePre = segmentNamePre;
		this.redisManager = redisManager;
		this.clz = clz;
		this.segmentMaxSize = segmentMaxSize;
	}

	public Index put(String mapKey, String key, T data) {
		if (null == mapKey) {
			mapKey = getWriteIndex();
		}
		redisManager.hset(mapKey, key, data);
		Index index = getIndex(mapKey, key);
		return index;
	}

	public T get(Index index) {
		T data = null;
		if (null != index) {
			data = redisManager.hget(index.getMapKey(), index.getDataKey(), clz);
		}
		return data;
	}

	public String where(String key) {
		String where = null;
		List<String> mapKeys = getMaps();
		if (null != mapKeys) {
			for (String mapKey : mapKeys) {
				if (redisManager.isExecuted(mapKey, key)) {
					where = mapKey;
					break;
				}
			}
		}
		return where;
	}

	public List<T> getData(String mapKey) {
		List<T> segmentNames = null;
		if (null != mapKey) {
			segmentNames = redisManager.hgetAllList(mapKey, clz);
		} else {
			segmentNames = Collections.emptyList();
		}
		return segmentNames;
	}

	public List<String> getMaps() {
		List<String> segmentNames = redisManager.lrange(this.segmentNames, 0, -1, String.class);
		return segmentNames;
	}

	public Index getIndex(String mapKey, String dataKey) {
		Index index = new Index();
		index.setMapKey(mapKey);
		index.setDataKey(dataKey);
		return index;
	}

	public boolean contains(String key) {
		return null != where(key);
	}

	public void del(Index index) {
		if (null != index && null != index.getMapKey() && null != index.getDataKey()) {
			redisManager.hdel(index.getMapKey(), index.getDataKey());
		}
	}

	private String getWriteIndex() {
		String writeIndex = redisManager.lindex(this.segmentNames, 0, String.class);
		if (null == writeIndex) {
			writeIndex = getKey(0);
			redisManager.lpush(this.segmentNames, writeIndex);
			return writeIndex;
		} else {
			int llen = redisManager.hllen(writeIndex);
			if (llen >= segmentMaxSize) {
				int segmentNameSize = redisManager.llen(this.segmentNames);
				String newWriteIndex = getKey(segmentNameSize);
				redisManager.lpush(this.segmentNames, newWriteIndex);
				writeIndex = newWriteIndex;
			}
			return writeIndex;
		}
	}

	private String getKey(int index) {
		return segmentNamePre + "_" + index + "_" + SystemUtils.getMac() + "_" + SystemUtils.getPid() + "_"
				+ random.nextLong() + "_" + System.currentTimeMillis();
	}

	public int size() {
		int size = 0;
		List<String> segmentNames = getMaps();
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				size += redisManager.hllen(segmentName);
			}
		}
		return size;
	}

	public void clear() {
		List<String> segmentNames = getMaps();
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				redisManager.del(segmentName);
			}
			redisManager.del(this.segmentNames);
		}
	}

	public void cleanUp() {
		List<String> segmentNames = getMaps();
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				int llen = redisManager.hllen(segmentName);
				if (0 == llen) {
					redisManager.del(segmentName);
					redisManager.lrem(this.segmentNames, 1, segmentName);
				}
			}
		}
	}

}
