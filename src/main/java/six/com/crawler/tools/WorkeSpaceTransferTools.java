package six.com.crawler.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import six.com.crawler.dao.EnhanceJedisCluster;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Page;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.work.space.RedisWorkSpace;
import six.com.crawler.work.space.SegmentRedisWorkSpace;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 上午10:43:17
 */
public class WorkeSpaceTransferTools {

	public static void main(String[] args) {

	}

	public static void transfer(String workSpaceName, String redisConnection) {
		if (StringUtils.isNotBlank(workSpaceName) && StringUtils.isNotBlank(redisConnection)) {
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
			WorkSpace<Page> doWorkQueue = new RedisWorkSpace<>(doRedisManager, distributedLock, workSpaceName,
					Page.class);
			WorkSpace<Page> targetWorkQueue = new SegmentRedisWorkSpace<>(doRedisManager, distributedLock,
					workSpaceName, Page.class);
			List<String> doneList = new ArrayList<>();
			String cursorStr = "0";
			do {
				cursorStr = doWorkQueue.batchGetDoneData(doneList, cursorStr);
				for (String dataKey : doneList) {
					targetWorkQueue.addDone(dataKey);
				}
				doneList.clear();
			} while (!"0".equals(cursorStr));
			doWorkQueue.clearDone();
		}

	}

	public static void del(String workSpaceName, String redisConnection, String keyPre) {
		if (StringUtils.isNotBlank(workSpaceName) && StringUtils.isNotBlank(redisConnection)) {
			EnhanceJedisCluster doRedis = newJedis(redisConnection);
			RedisManager doRedisManager = new six.com.crawler.dao.RedisManager();
			doRedisManager.setJedisCluster(doRedis);
			Set<String> keys = doRedisManager.keys(keyPre + "*");
			for (String key : keys) {
				doRedisManager.del(key);
			}
		}
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
		EnhanceJedisCluster jedisCluster = new EnhanceJedisCluster(set, 6000, config);
		return jedisCluster;
	}
}
