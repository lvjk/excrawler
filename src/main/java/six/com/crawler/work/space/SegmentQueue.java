package six.com.crawler.work.space;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月11日 上午8:42:58
 */
public class SegmentQueue<T> extends AbstractSegment<T> {

	private String readIndex;

	SegmentQueue(String segmentNames, String segmentNamePre, RedisManager redisManager, int segmentMaxSize,
			Class<T> clz) {
		super(segmentNames, segmentNamePre, redisManager, segmentMaxSize, clz);
	}

	public T poll() {
		T value = null;
		while (true) {
			if (null == readIndex) {
				readIndex = getRedisManager().lindex(getSegmentsName(), -1, String.class);
			}
			if (null != readIndex) {
				value = getRedisManager().lpop(readIndex, getDataClass());
				if (null != value) {
					break;
				} else {
					getRedisManager().lrem(getSegmentsName(), 1, readIndex);
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
		String writeIndex = getWriteSegment();
		getRedisManager().rpush(writeIndex, index);
	}

	@Override
	public int getSegmentSize(String segmentName) {
		int size = getRedisManager().llen(segmentName);
		return size;
	}
}
