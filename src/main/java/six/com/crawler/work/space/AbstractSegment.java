package six.com.crawler.work.space;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午2:34:14
 */
public abstract class AbstractSegment<T> {

	private final static String Separate = "_";

	private RedisManager redisManager;

	private static Random random = new Random();

	private Class<T> clz;

	private int segmentMaxSize;

	private String segmentsName;

	private String segmentNamePre;

	AbstractSegment(String segmentsName, String segmentNamePre, RedisManager redisManager, int segmentMaxSize,
			Class<T> clz) {
		Objects.requireNonNull(segmentsName, "segmentNames must not be null");
		Objects.requireNonNull(segmentNamePre, "segmentNamePre must not be null");
		Objects.requireNonNull(redisManager, "redisManager must not be null");
		Objects.requireNonNull(clz, "clz must not be null");
		this.segmentsName = segmentsName;
		this.segmentNamePre = segmentNamePre;
		this.redisManager = redisManager;
		this.clz = clz;
		this.segmentMaxSize = segmentMaxSize;
	}

	public String getSegmentsName() {
		return segmentsName;
	}

	public List<String> getSegments() {
		List<String> segmentNames = redisManager.lrange(getSegmentsName(), 0, -1, String.class);
		return segmentNames;
	}

	public String getSegment(int segmentIndex) {
		String segmentName = redisManager.lindex(getSegmentsName(), segmentIndex, String.class);
		return segmentName;
	}

	public String getSegmentPre() {
		return segmentNamePre;
	}

	public int getSementMaxSize() {
		return segmentMaxSize;
	}

	public Class<T> getDataClass() {
		return clz;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	protected String getWriteIndex() {
		String writeSegment = getRedisManager().lindex(getSegmentsName(), 0, String.class);
		if (null == writeSegment) {
			writeSegment = getKey(0);
			getRedisManager().lpush(getSegmentsName(), writeSegment);
			return writeSegment;
		} else {
			int llen = getSegmentSize(writeSegment);
			if (llen >= getSementMaxSize()) {
				int segmentNameSize = getRedisManager().llen(getSegmentsName());
				String newWriteIndex = getKey(segmentNameSize);
				getRedisManager().lpush(getSegmentsName(), newWriteIndex);
				writeSegment = newWriteIndex;
			}
			return writeSegment;
		}
	}

	protected String getKey(int index) {
		StringBuilder segmentName = new StringBuilder(getSegmentPre());
		segmentName.append(Separate);
		segmentName.append(index);
		segmentName.append(Separate);
		segmentName.append(SystemUtils.getMac());
		segmentName.append(Separate);
		segmentName.append(SystemUtils.getPid());
		segmentName.append(Separate);
		segmentName.append(random.nextLong());
		segmentName.append(Separate);
		segmentName.append(System.currentTimeMillis());
		return segmentName.toString();
	}

	public abstract int getSegmentSize(String segmentName);

	public void cleanUp() {
		List<String> segmentNames = getSegments();
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				int llen = getSegmentSize(segmentName);
				if (0 == llen) {
					getRedisManager().del(segmentName);
					getRedisManager().lrem(getSegmentsName(), 1, segmentName);
				}
			}
		}
	}

	public int segmentSize() {
		int segmentSize = 0;
		List<String> segmentNames = getSegments();
		if (null != segmentNames) {
			segmentSize = segmentNames.size();
		}
		return segmentSize;
	}

	public int size() {
		int size = 0;
		List<String> segmentNames = getSegments();
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				size += getSegmentSize(segmentName);
			}
		}
		return size;
	}

	public void clear() {
		List<String> segmentNames = getSegments();
		if (null != segmentNames) {
			for (String segmentName : segmentNames) {
				redisManager.del(segmentName);
			}
			redisManager.del(getSegmentsName());
		}
	}
}
