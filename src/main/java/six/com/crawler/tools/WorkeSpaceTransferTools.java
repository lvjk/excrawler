package six.com.crawler.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import six.com.crawler.dao.EnhanceJedisCluster;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Page;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.utils.DbHelper;
import six.com.crawler.utils.MD5Utils;
import six.com.crawler.work.space.SegmentRedisWorkSpace;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 上午10:43:17
 */
public class WorkeSpaceTransferTools {

	public final static String WORK_DONE_QUEUE_KEY_PRE = "workspace_done_queue_";

	public static void main(String[] args) {
		transfer("tmsf_house_info");
		// del("spider_redis_store");
		// doMysql();
		// nb_cnnbfdc_unit_info
		// WorkeSpaceTransferTools.testPull1("nb_cnnbfdc_room_info");
	}

	public static void transfer(String workSpaceName) {
		// String redisConnection =
		// "192.168.0.13:6379;192.168.0.13:6380;192.168.0.14:6379;192.168.0.14:6380;192.168.0.15:6379;192.168.0.15:6380;192.168.0.13:6381;192.168.0.14:6381";

		String redisConnection = "172.18.88.44:6379;172.18.88.45:6379;172.18.88.46:6379";
		// String redisConnection = "122.112.214.233:6379;122.112.214.233:6380;"
		// + "122.112.214.232:6379;122.112.214.232:6380;" +
		// "122.112.210.132:6379;122.112.210.132:6380;";
		EnhanceJedisCluster doRedis = newJedis(redisConnection);
		RedisManager doRedisManager = new six.com.crawler.dao.RedisManager();
		doRedisManager.setJedisCluster(doRedis);
		DistributedLock distributedLock = new DistributedLock() {
			@Override
			public void unLock() {

			}

			@Override
			public void lock() {

			}
		};
		WorkSpace<Page> targetWorkQueue = new SegmentRedisWorkSpace<>(doRedisManager, distributedLock, workSpaceName,
				Page.class);
		Page page = null;
		while (null != (page = targetWorkQueue.pull())) {
			System.out.println("页面数据:" + page.toString());
		}
	}

	public static void del(String keyPre) {
		// 122.112.210.132
		// String redisConnection =
		// "172.18.84.44:6379;172.18.84.45:6379;172.18.84.46:6379";
		// String redisConnection =
		// "122.112.210.132:6379;2.112.210.132:6380;122.112.214.232:6379;122.112.214.232:6380;122.112.214.233:6379;122.112.214.233:6380";
		String redisConnection = "192.168.0.13:6379;192.168.0.13:6380;192.168.0.14:6379;192.168.0.14:6380;192.168.0.15:6379;192.168.0.15:6380;192.168.0.13:6381;192.168.0.14:6381";
		EnhanceJedisCluster doRedis = newJedis(redisConnection);
		RedisManager doRedisManager = new six.com.crawler.dao.RedisManager();
		doRedisManager.setJedisCluster(doRedis);
		Set<String> keys = doRedisManager.keys(keyPre + "*");
		for (String key : keys) {
			doRedisManager.del(key);
			System.out.println("删除key:" + key);
		}
	}

	public static void doMysql() {
		String url = "jdbc:mysql://172.30.103.83:3306/excrawler?"
				+ "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
		String user = "excrawler";
		String password = "Aa123456";
		String redisConnection = "172.30.103.81:6379;172.30.103.82:6379;172.30.103.83:6379";
		String workSpaceName = "nb_cnnbfdc_room_info";
		String countSql = "select count(1) count from ex_dc_nb_cnnbfdc_room_info_20170427175943";
		String sql = "select originUrl from ex_dc_nb_cnnbfdc_room_info_20170427175943 limit ?,?";
		AtomicInteger index = new AtomicInteger(0);
		Connection countConn = DbHelper.getConnection(url, user, password);
		int count = getCount(countConn, countSql);
		DbHelper.close(countConn);
		int segmentSize = 50000;
		int thread = count % segmentSize == 0 ? count / segmentSize : count / segmentSize + 1;
		ExecutorService executor = Executors.newFixedThreadPool(thread);
		EnhanceJedisCluster jedisCluster = WorkeSpaceTransferTools.newJedis(redisConnection);
		RedisManager redisManager = new RedisManager();
		redisManager.setJedisCluster(jedisCluster);
		CountDownLatch cdl = new CountDownLatch(thread);
		StampedLock setStateLock = new StampedLock();
		DistributedLock distributedLock = new DistributedLock() {
			@Override
			public void unLock() {

			}

			@Override
			public void lock() {

			}
		};
		SegmentRedisWorkSpace<Page> workSpace = new SegmentRedisWorkSpace<>(redisManager, distributedLock,
				workSpaceName, Page.class);
		for (int i = 0; i < thread; i++) {
			executor.execute(() -> {
				Connection conn = DbHelper.getConnection(url, user, password);
				PreparedStatement ps = null;
				int start = index.getAndIncrement() * segmentSize;
				String threadName = Thread.currentThread().getName();
				try {
					ps = conn.prepareStatement(sql);
					ps.setInt(1, start);
					ps.setInt(2, segmentSize);
					ResultSet result = ps.executeQuery();
					String originUrl = null;
					String dataKey = null;
					String jedisKey = null;
					long startTime = 0;
					long endTime = 0;
					while (result.next()) {
						originUrl = result.getString("originUrl");
						startTime = System.currentTimeMillis();
						dataKey = MD5Utils.MD5(originUrl);
						jedisKey = getDoneKey(workSpaceName, dataKey);
						String value = redisManager.get(jedisKey, String.class);
						if (null != value) {
							redisManager.del(jedisKey);
						}
						long stamp = 0;
						try {
							stamp = setStateLock.writeLock();
							workSpace.addDone(dataKey);
						} finally {
							setStateLock.unlock(stamp);
						}
						endTime = System.currentTimeMillis();
						System.out.println("线程[" + threadName + "]处理key[" + jedisKey + "]耗时:" + (endTime - startTime));

					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					cdl.countDown();
					DbHelper.close(conn);
				}
			});
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static int getCount(Connection conn, String sql) {
		PreparedStatement ps = null;
		int count = 0;
		try {
			ps = conn.prepareStatement(sql);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				count = result.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return count;
	}

	public static void testPull1(String workSpaceName) {
		String redisConnection = "172.30.103.83:6379;172.30.103.83:6380;172.30.103.81:6379;172.30.103.81:6380;172.30.103.82:6379;172.30.103.82:6380;";
		EnhanceJedisCluster jedisCluster = WorkeSpaceTransferTools.newJedis(redisConnection);
		RedisManager redisManager = new RedisManager();
		redisManager.setJedisCluster(jedisCluster);
		DistributedLock distributedLock = new DistributedLock() {
			@Override
			public void unLock() {

			}

			@Override
			public void lock() {

			}
		};
		SegmentRedisWorkSpace<Page> workSpace = new SegmentRedisWorkSpace<>(redisManager, distributedLock,
				workSpaceName, Page.class);
		Page page = null;
		boolean repair = false;
		while (true) {
			long start = System.currentTimeMillis();
			page = workSpace.pull();
			long end = System.currentTimeMillis();
			System.out.println("pull time:" + (end - start));
			if (null != page) {
				workSpace.errRetryPush(page);
			} else {
				if (!repair) {
					workSpace.repair();
					repair = true;
					continue;
				} else {
					break;
				}
			}
		}
	}

	private static String getDoneKey(String workSpace, String key) {
		String newDoneKey = WORK_DONE_QUEUE_KEY_PRE + workSpace + "_" + key;
		return newDoneKey;
	}

	public static EnhanceJedisCluster newJedis(String connectionStr) {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxIdle(10);
		config.setBlockWhenExhausted(true);
		// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted), 如果超时就抛异常, 小于零: 阻塞不确定的时间
		config.setMaxWaitMillis(-1);
		// 连接耗尽时是否阻塞, false报异常, ture阻塞直到超时, 默认true
		config.setBlockWhenExhausted(true);
		// 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
		config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
		// 是否启用pool的jmx管理功能
		config.setJmxEnabled(false);
		// 是否启用后进先出
		config.setLifo(true);
		// 最大空闲连接数
		config.setMaxIdle(8);
		// 最大连接数
		config.setMaxTotal(8);
		// 逐出连接的最小空闲时间 默认1800 000 毫秒(30分钟)
		config.setMinEvictableIdleTimeMillis(1800000);
		// 最小空闲连接数
		config.setMinIdle(0);
		// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
		config.setNumTestsPerEvictionRun(3);
		// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数
		// 时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
		config.setSoftMinEvictableIdleTimeMillis(1800000);
		// 在获取连接的时候检查有效性
		config.setTestOnBorrow(true);
		// 在空闲时检查有效性
		config.setTestWhileIdle(true);
		// 逐出扫描的时间间隔(毫秒) 如果为负数, 则不运行逐出线程
		config.setTimeBetweenEvictionRunsMillis(-1);
		String[] hosts = connectionStr.split(";");
		String[] temp = null;
		Set<HostAndPort> set = new HashSet<>();
		for (String host : hosts) {
			temp = host.split(":");
			set.add(new HostAndPort(temp[0], Integer.valueOf(temp[1])));
		}
		EnhanceJedisCluster jedisCluster = new EnhanceJedisCluster(set, 60000, config);
		return jedisCluster;
	}
}
