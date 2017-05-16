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

	/**
	 * 获取所有分段名集合的name
	 * 
	 * @return
	 */
	public String getSegmentsName() {
		return segmentsName;
	}

	/**
	 * 获取所有分段名集合
	 * 
	 * @return
	 */
	public List<String> getSegments() {
		List<String> segmentNames = redisManager.lrange(getSegmentsName(), 0, -1, String.class);
		return segmentNames;
	}

	/**
	 * 通过索引获取一个分段名
	 * 
	 * @param segmentIndex
	 * @return
	 */
	public String getSegment(int segmentIndex) {
		String segmentName = redisManager.lindex(getSegmentsName(), segmentIndex, String.class);
		return segmentName;
	}

	/**
	 * 获取分段名前缀
	 * 
	 * @return
	 */
	public String getSegmentPre() {
		return segmentNamePre;
	}

	/**
	 * 获取分段最大数量
	 * 
	 * @return
	 */
	public int getSementMaxSize() {
		return segmentMaxSize;
	}

	/**
	 * 获取数据class
	 * 
	 * @return
	 */
	public Class<T> getDataClass() {
		return clz;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	/**
	 * 获取可写入数据的分段名
	 * 
	 * @return
	 */
	protected String getWriteSegment() {
		String writeSegment = getRedisManager().lindex(getSegmentsName(), 0, String.class);
		if (null == writeSegment) {
			writeSegment = newSegmentName(0);
			getRedisManager().lpush(getSegmentsName(), writeSegment);
			return writeSegment;
		} else {
			int llen = getSegmentSize(writeSegment);
			if (llen >= getSementMaxSize()) {
				int segmentNameSize = getRedisManager().llen(getSegmentsName());
				String newWriteIndex = newSegmentName(segmentNameSize);
				getRedisManager().lpush(getSegmentsName(), newWriteIndex);
				writeSegment = newWriteIndex;
			}
			return writeSegment;
		}
	}

	/**
	 * 生成一个分段名称
	 * 
	 * @param index
	 * @return
	 */
	protected String newSegmentName(int index) {
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

	/**
	 * 获取指定分段数量
	 * 
	 * @param segmentName
	 * @return
	 */
	public abstract int getSegmentSize(String segmentName);

	/**
	 * 整理分段
	 */
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

	/**
	 * 获取分段数量
	 * 
	 * @return
	 */
	public int segmentSize() {
		int segmentSize = 0;
		List<String> segmentNames = getSegments();
		if (null != segmentNames) {
			segmentSize = segmentNames.size();
		}
		return segmentSize;
	}

	/**
	 * 总数据量
	 * 
	 * @return
	 */
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

	/**
	 * clear 所有数据
	 */
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
