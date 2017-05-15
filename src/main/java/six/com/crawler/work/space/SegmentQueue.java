package six.com.crawler.work.space;

import java.util.List;
import java.util.Random;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月11日 上午8:42:58
 */
public class SegmentQueue<T> {

	private int segmentMaxSize;
	private String segmentNames;
	private String segmentNamePre;
	private RedisManager redisManager;
	private String readIndex;
	private static Random random = new Random();
	private Class<T> clz;

	SegmentQueue(String segmentNames, String segmentNamePre, RedisManager redisManager, int segmentMaxSize,
			Class<T> clz) {
		this.segmentNames = segmentNames;
		this.segmentNamePre = segmentNamePre;
		this.redisManager = redisManager;
		this.clz = clz;
		this.segmentMaxSize = segmentMaxSize;
	}

	public T poll() {
		T value = null;
		while (true) {
			if (null == readIndex) {
				readIndex = redisManager.lindex(this.segmentNames, -1, String.class);
			}
			if (null != readIndex) {
				value = redisManager.lpop(readIndex, clz);
				if (null != value) {
					break;
				} else {
					redisManager.lrem(this.segmentNames, 1, readIndex);
					readIndex = null;
					continue;
				}
			} else {
				break;
			}
		}
		return value;
	}

	public void push(T index) {
		String writeIndex = getWriteIndex();
		redisManager.rpush(writeIndex, index);
	}

	private String getWriteIndex() {
		String writeIndex = redisManager.lindex(this.segmentNames, 0, String.class);
		if (null == writeIndex) {
			writeIndex = getKey(0);
			redisManager.lpush(this.segmentNames, writeIndex);
			return writeIndex;
		} else {
			int llen = redisManager.llen(writeIndex);
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

	public void clear() {
		List<String> segmentNames = redisManager.lrange(this.segmentNames, 0, -1, String.class);
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				redisManager.del(segmentName);
			}
			redisManager.del(this.segmentNames);
		}
	}
}
