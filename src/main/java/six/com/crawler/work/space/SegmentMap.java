package six.com.crawler.work.space;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 下午2:57:37
 * 
 *       redis分段 map ,根据index 保存和ack数据
 */
public class SegmentMap<T> extends AbstractSegment<T> {

	SegmentMap(String segmentNames, String segmentNamePre, RedisManager redisManager, int segmentMaxSize,
			Class<T> clz) {
		super(segmentNames, segmentNamePre, redisManager, segmentMaxSize, clz);
	}

	public Index put(String mapKey, String dataKey, T data) {
		Index index = null;
		if (null != data && StringUtils.isNotBlank(dataKey)) {
			if (null == mapKey) {
				mapKey = getWriteSegment();
			}
			getRedisManager().hset(mapKey, dataKey, data);
			index = getIndex(mapKey, dataKey);
		}
		return index;
	}

	public T get(Index index) {
		T data = null;
		if (null != index) {
			data = getRedisManager().hget(index.getMapKey(), index.getDataKey(), getDataClass());
		}
		return data;
	}

	public String where(String key) {
		String where = null;
		if (StringUtils.isNotBlank(key)) {
			List<String> mapKeys = getSegments();
			if (null != mapKeys) {
				for (String mapKey : mapKeys) {
					if (getRedisManager().isExecuted(mapKey, key)) {
						where = mapKey;
						break;
					}
				}
			}
		}
		return where;
	}

	public List<T> getData(String segmentMapKey) {
		List<T> segmentNames = null;
		if (StringUtils.isNotBlank(segmentMapKey)) {
			segmentNames = getRedisManager().hgetAllList(segmentMapKey, getDataClass());
		} else {
			segmentNames = Collections.emptyList();
		}
		return segmentNames;
	}

	public Index getIndex(String mapKey, String dataKey) {
		Index index = null;
		if (StringUtils.isNotBlank(mapKey) && StringUtils.isNotBlank(dataKey)) {
			index = new Index();
			index.setMapKey(mapKey);
			index.setDataKey(dataKey);
		}
		return index;
	}

	public boolean contains(String key) {
		return null != where(key);
	}

	public void del(Index index) {
		if (null != index && StringUtils.isNotBlank(index.getMapKey()) && StringUtils.isNotBlank(index.getDataKey())) {
			getRedisManager().hdel(index.getMapKey(), index.getDataKey());
		}
	}

	public String batchGet(String segmentMapKey, List<T> resutList, String cursorStr) {
		if (StringUtils.isNotBlank(segmentMapKey)) {
			Objects.requireNonNull(resutList, "resutList must not be null");
			if (StringUtils.isBlank(cursorStr)) {
				cursorStr = "0";
			}
			Map<String, T> map = new HashMap<>();
			cursorStr = getRedisManager().hscan(segmentMapKey, cursorStr, map, getDataClass());
			resutList.addAll(map.values());
		}
		return cursorStr;
	}

	@Override
	public int getSegmentSize(String segmentName) {
		int size = getRedisManager().hllen(segmentName);
		return size;
	}
}
