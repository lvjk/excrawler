package six.com.crawler.common;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

import redis.clients.jedis.JedisCluster;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月27日 下午3:13:14
 * 
 *       基于redis实现分布式锁,无需给锁key加上过期时间，程序会自动检测
 */
public class RedisLock {

	private static long SYSTEM_START_TIME = System.currentTimeMillis();
	private static String mac;
	private static String pid;
	private static String nodeKeepliveInfoPre;

	static {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		pid = name.split("@")[0];
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
		nodeKeepliveInfoPre = mac + "_" + pid + "_" + SYSTEM_START_TIME + "_";
	}

	private Thread keepliveThread;
	private JedisCluster jedisCluster;
	private String nodeKeepliveInfoKeyPre;
	private String nodeKeepliveInfoKey;
	private long loopKeepliveInterval;
	private int keepliveInfoExpire;
	private long checkLockIntervalTime;

	public RedisLock(JedisCluster jedisCluster, String nodeKeepLiveInfoKeyPre, long loopKeepliveInterval,
			long checkLockIntervalTime) {
		this.jedisCluster = jedisCluster;
		this.nodeKeepliveInfoKeyPre = nodeKeepLiveInfoKeyPre;
		this.nodeKeepliveInfoKey = getNodeKeepliveInfoKey(mac, pid, String.valueOf(SYSTEM_START_TIME));
		this.loopKeepliveInterval = loopKeepliveInterval;
		this.keepliveInfoExpire = (int) (loopKeepliveInterval) / 1000 * 2;
		this.checkLockIntervalTime = checkLockIntervalTime;
		initKeepLive();
	}

	/**
	 * 节点mac+进程id+进程启动时间保证节点重启问题
	 */
	private void initKeepLive() {
		keepliveThread = new Thread(() -> {
			String nodeInfo = null;
			while (true) {
				nodeInfo = nodeKeepliveInfoPre + String.valueOf(System.currentTimeMillis());
				jedisCluster.set(nodeKeepliveInfoKey, nodeInfo);
				jedisCluster.expire(nodeKeepliveInfoKey, keepliveInfoExpire);
				try {
					Thread.sleep(loopKeepliveInterval);
				} catch (InterruptedException e) {
				}
			}

		}, "node-keeplive-thread");
		keepliveThread.setDaemon(true);
		keepliveThread.start();
	}

	public void lock(String lockKey) {
		while (true) {
			if (1 == jedisCluster.setnx(lockKey, getNodeLockInfo())) {
				return;
			}
			String nodeLockInfo = jedisCluster.get(lockKey);
			if (isReentrant(nodeLockInfo)) {
				return;
			}
			String nodeInfoKey = getNodeKeepliveInfoKey(nodeLockInfo);
			String lastKeepNodeInfo = jedisCluster.get(nodeInfoKey);
			do {
				try {
					Thread.sleep(checkLockIntervalTime);// 这个时间需要根据节点刷新时间取一个合适值
				} catch (InterruptedException e) {
				}
				String tempNodeInfo = jedisCluster.get(nodeInfoKey);
				if (isNotKeeplive(lastKeepNodeInfo, tempNodeInfo)) {
					// 证明节点挂了
					unlock(lockKey);
					break;
				} else {
					lastKeepNodeInfo = tempNodeInfo;
				}
			} while (true);
		}
	}

	/**
	 * 判断是否符合可重入锁
	 * @param nodeLockInfo
	 * @return
	 */
	private boolean isReentrant(String nodeLockInfo) {
		String[] meta = nodeLockInfo.split("_");
		if (mac.equals(meta[0])
				&&pid.equals(meta[1])
				&&SYSTEM_START_TIME == Long.valueOf(meta[2])
				&&Thread.currentThread().getId() == Long.valueOf(meta[3])) {
			return true;
		}
		return false;
	}
	/**
	 * 判断目标节点是否还在线
	 * 
	 * @param lastKeepliveInfo
	 * @param newKeepliveInfo
	 * @return
	 */
	private boolean isNotKeeplive(String lastKeepliveInfo, String newKeepliveInfo) {
		String[] lastMeta = lastKeepliveInfo.split("_");
		String[] newMeta = newKeepliveInfo.split("_");
		// mac pid 启动时间 系统时间
		if (lastMeta[0] != newMeta[0]) {
			// 当前Hold key的节点已被其他节点占据
			return true;
		} else {
			if (lastMeta[1] != newMeta[1]) {
				// pid发生变化表示节点已经重启
				return true;
			} else {
				if (lastMeta[2] != newMeta[2]) {
					// 启动时间发生变化表示节点已经重启
					return true;
				} else {
					if (lastMeta[3] != newMeta[3]) {
						// 系统时间发生变化表示节点正常存活
						return false;
					} else {
						return true;
					}
				}
			}
		}
	}


	public void unlock(String lockKey) {
		jedisCluster.del(lockKey);
	}

	private String getNodeLockInfo() {
		return mac + "_" + pid + "_" + SYSTEM_START_TIME + "_" + Thread.currentThread().getId()
				+ +System.currentTimeMillis();
	}

	private String getNodeKeepliveInfoKey(String mac, String pid, String systemStartTime) {
		String nodeKeepLiveInfoKey = nodeKeepliveInfoKeyPre + mac + pid + systemStartTime;
		return nodeKeepLiveInfoKey;
	}

	private String getNodeKeepliveInfoKey(String nodeLockInfo) {
		String[] meta = nodeLockInfo.split("_");
		return getNodeKeepliveInfoKey(meta[0], meta[1], meta[2]);
	}

}
