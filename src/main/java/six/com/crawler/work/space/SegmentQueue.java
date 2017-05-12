package six.com.crawler.work.space;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Random;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月11日 上午8:42:58
 */
public class SegmentQueue {

	private final static int PROXY_DOING_SEGMENT_MAX_SIZE = 2000;
	private final static String SEGMENT_QUEUE_NAME = "workspace_segment_queue_";
	private final static String SEGMENT_QUEUE_NAME_KEYS = "workspace_segment_queue_keys_";
	private static String MAC;
	private static String PID;
	private String segmentNames;
	private String segmentNamePre;
	private RedisManager redisManager;
	private String readIndex;
	private static Random random = new Random();

	static {
		MAC = getLocalMac();
		PID = getPid();
	}

	SegmentQueue(String name, RedisManager redisManager) {
		this.segmentNames = SEGMENT_QUEUE_NAME_KEYS + name;
		this.segmentNamePre = SEGMENT_QUEUE_NAME + name;
		this.redisManager = redisManager;
	}

	public String poll() {
		String value = null;
		while (true) {
			if (null == readIndex) {
				readIndex = getReadIndex();
			}
			if (null != readIndex) {
				value = redisManager.lpop(readIndex, String.class);
				if (null != value) {
					break;
				} else {
					delReadIndex(readIndex);
					readIndex = null;
					continue;
				}
			} else {
				break;
			}
		}
		return value;
	}

	public void push(String key) {
		String writeIndex = getWriteIndex();
		redisManager.rpush(writeIndex, key);
	}

	private String getWriteIndex() {
		String writeIndex = redisManager.lindex(this.segmentNames, 0, String.class);
		if (null == writeIndex) {
			writeIndex = getKey(0);
			redisManager.lpush(this.segmentNames, writeIndex);
			return writeIndex;
		} else {
			int llen = redisManager.llen(writeIndex);
			if (llen >= PROXY_DOING_SEGMENT_MAX_SIZE) {
				int segmentNameSize = redisManager.llen(this.segmentNames);
				String newWriteIndex = getKey(segmentNameSize);
				redisManager.lpush(this.segmentNames, newWriteIndex);
				writeIndex = newWriteIndex;
			}
			return writeIndex;
		}
	}

	private String getReadIndex() {
		String readIndex = redisManager.lindex(this.segmentNames, -1, String.class);
		return readIndex;
	}

	private void delReadIndex(String readIndex) {
		redisManager.lrem(this.segmentNames, 1, readIndex);
	}

	private String getKey(int index) {
		return segmentNamePre + "_" + index + "_" + MAC + "_" + PID + "_" + random.nextLong() + "_"
				+ System.currentTimeMillis();
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

	private static String getLocalMac() {
		String mac = "";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			byte[] macBytes = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < macBytes.length; i++) {
				int temp = macBytes[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			mac = sb.toString().toUpperCase();
		} catch (Exception e) {
		}
		return mac;
	}

	private static String getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		return pid;
	}
}
