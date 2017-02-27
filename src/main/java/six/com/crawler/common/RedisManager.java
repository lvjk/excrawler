package six.com.crawler.common;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import six.com.crawler.common.configure.SpiderConfigure;
import six.com.crawler.common.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月21日 上午9:34:41 有的方法实现了
 *       while(retry<REDIS_STORE_MAX_RETRY_COUNT) 主要是之前环境redis 经常偶发性timeout
 */
@Component
public class RedisManager implements InitializingBean {

	final static Logger LOG = LoggerFactory.getLogger(RedisManager.class);

	@Autowired
	private SpiderConfigure configure;

	private EnhanceJedisCluster jedisCluster;
	// redis lock time out 3分钟
	private int lockTimeout;
	// redis 锁 前缀
	private final static String PRE_LOCK = "spider_redis_lock_";

	private final static String HOST_PID;

	static {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];

		String host = "";
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
			host = addr.getHostAddress().toString();// 获得本机IP
			HOST_PID = host.concat("_").concat(pid);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 *            >0那么 设置 expire redis key过期时间 秒
	 */
	public void set(final String key, final Object value) {
		final byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		final byte[] valueBytes = JavaSerializeUtils.serialize(value);
		RedisRetryHelper.execute(() -> {
			String result = jedisCluster.set(keyBytes, valueBytes);
			return result;
		});
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 *            >0那么 设置 expire redis key过期时间 秒
	 */
	public void set(final String key, final Object value, final int expire) {
		final byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		final byte[] valueBytes = JavaSerializeUtils.serialize(value);
		RedisRetryHelper.execute(() -> {
			String result = jedisCluster.set(keyBytes, valueBytes);
			if (expire > 0) {
				jedisCluster.expire(keyBytes, expire);
			}
			return result;
		});
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 *            >0那么 设置 expire redis key过期时间 秒
	 */
	public void lpush(String key, Object value) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		byte[] valueBytes = JavaSerializeUtils.serialize(value);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.lpush(keyBytes, valueBytes);
		});
	}

	public <T> T lindex(String key, int index, Class<T> clz) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			T result = null;
			byte[] dataBytes = jedisCluster.lindex(keyBytes, index);
			if (null != dataBytes) {
				result = JavaSerializeUtils.unSerialize(dataBytes, clz);
			}
			return result;
		});
	}
	
	public String lset(String key, int index,Object value) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		byte[] valueBytes = JavaSerializeUtils.serialize(value);
		return RedisRetryHelper.execute(() -> {
			return jedisCluster.lset(keyBytes, index, valueBytes);
		});
	}
	
	public int lrem(String key, int count,Object value) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		byte[] valueBytes = JavaSerializeUtils.serialize(value);
		return RedisRetryHelper.execute(() -> {
			return jedisCluster.lrem(keyBytes, count, valueBytes).intValue();
		});
	}

	public <T> List<T> lrange(String key, int start, int end, Class<T> clz) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			List<byte[]> list = jedisCluster.lrange(keyBytes, start, end);
			List<T> tempList = new ArrayList<>(list.size());
			list.forEach(bs -> {
				T t = JavaSerializeUtils.unSerialize(bs, clz);
				tempList.add(t);
			});
			return tempList;
		});
	}

	public Long incr(String key) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		Long incr = RedisRetryHelper.execute(() -> {
			return jedisCluster.incr(keyBytes);
		});
		return incr;
	}

	public <T> T get(String key, Class<T> clz) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			byte[] bytes = jedisCluster.get(keyBytes);
			return JavaSerializeUtils.unSerialize(bytes, clz);
		});
	}

	/**
	 * 设置key 的有效期
	 * 
	 * @param key
	 * @param expire
	 *            单位秒
	 */
	public void expire(String key, int expire) {
		byte[] keyByte = JavaSerializeUtils.serializeString(key);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.expire(keyByte, expire);
		});
	}

	/**
	 * 
	 * @param listkey
	 *            集合key
	 * @param valuekey
	 *            数据key
	 * @param value
	 *            数据
	 */
	public void hset(String hkey, String fkey, Object ob) {
		byte[] lKeyByte = JavaSerializeUtils.serializeString(hkey);
		byte[] vKeyByte = JavaSerializeUtils.serializeString(fkey);
		byte[] value = JavaSerializeUtils.serialize(ob);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.hset(lKeyByte, vKeyByte, value);
		});
	}

	public void rpush(String key, Object value) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		byte[] valueBytes = JavaSerializeUtils.serialize(value);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.rpush(keyBytes, valueBytes);
		});
	}

	public <T> T lpop(String key, Class<T> clz) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			byte[] data = jedisCluster.lpop(keyBytes);
			return JavaSerializeUtils.unSerialize(data, clz);
		});
	}

	/**
	 * 
	 * @param listkey
	 *            集合key
	 * @param valuekey
	 *            数据key
	 * @param value
	 *            数据
	 */
	public <T> Map<String, T> hgetAll(String hkey, Class<T> clz) {
		byte[] hKeyBytes = JavaSerializeUtils.serializeString(hkey);
		return RedisRetryHelper.execute(() -> {
			Map<String, T> result = new HashMap<>();
			Map<byte[], byte[]> map = jedisCluster.hgetAll(hKeyBytes);
			map.forEach((mapKey, mapValue) -> {
				T t = JavaSerializeUtils.unSerialize(mapValue, clz);
				result.put(JavaSerializeUtils.unSerializeString(mapKey), t);
			});
			return result;
		});
	}

	public int hllen(String hkey) {
		byte[] hKeyBytes = JavaSerializeUtils.serializeString(hkey);
		return RedisRetryHelper.execute(() -> {
			Long llenL = jedisCluster.hlen(hKeyBytes);
			return llenL.intValue();
		});
	}

	public <T> T hget(String hkey, String fKey, Class<T> clz) {
		byte[] hKeyBytes = JavaSerializeUtils.serializeString(hkey);
		byte[] fKeyBytes = JavaSerializeUtils.serializeString(fKey);
		return RedisRetryHelper.execute(() -> {
			byte[] bytes = jedisCluster.hget(hKeyBytes, fKeyBytes);
			return JavaSerializeUtils.unSerialize(bytes, clz);
		});
	}

	public boolean isExecuted(String hkey, String fKey) {
		byte[] hKeyBytes = JavaSerializeUtils.serializeString(hkey);
		byte[] fKeyBytes = JavaSerializeUtils.serializeString(fKey);
		return RedisRetryHelper.execute(() -> {
			byte[] result = jedisCluster.hget(hKeyBytes, fKeyBytes);
			return result != null;
		});
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 *            >0那么 设置 expire redis key过期时间 秒
	 */
	public void del(String key) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.del(keyBytes);
		});
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 *            >0那么 设置 expire redis key过期时间 秒
	 */
	public void hdel(String hKey, String fKey) {
		byte[] hKeyBytes = JavaSerializeUtils.serializeString(hKey);
		byte[] fKeyBytes = JavaSerializeUtils.serializeString(fKey);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.hdel(hKeyBytes, fKeyBytes);
		});
	}

	public <T> List<T> lrange(final String key, final long start, final long end, Class<T> clz) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			List<T> result = new ArrayList<>();
			List<byte[]> bytes = jedisCluster.lrange(keyBytes, start, end);
			bytes.forEach(bs -> {
				T t = JavaSerializeUtils.unSerialize(bs, clz);
				result.add(t);
			});
			return result;
		});
	}

	/**
	 * 可重入锁:判断锁的host pid 线程id 是否是当前线程如果是那么放行 最终想法是 锁key存当前锁的时间(host pid 线程id),
	 * 当其他第一个要获取锁的时候 判断key里的时间是否超时，如果超时那么它将会检查锁住此(host pid 线程id)key的线程是否还在正常运行，
	 * 如果还在正常运行 那么继续等待，否则将获取此锁并日志记录锁住此key的线程已经挂掉导致此锁没有被unlike
	 * 
	 * @param key
	 * @return
	 */
	public void lock(String key) {
		final String lockKey = PRE_LOCK.concat(key);
		final String lockValuePre = HOST_PID + "_" + Thread.currentThread().getId();
		RedisRetryHelper.execute(() -> {
			String redisLockValue = null;
			long startTime = System.currentTimeMillis();
			while (true) {
				long nowTime = System.currentTimeMillis();
				String lockValue = lockValuePre + "_" + nowTime;
				// 1.如果set lockValue返回1 直接获取锁
				if (jedisCluster.setnx(lockKey, lockValue) == 1) {
					// 因為 checkLockIsErr 暫時沒有實現 所以這裡需要設置超時
					jedisCluster.expire(lockKey, lockTimeout);
					break;
				}
				// 2.獲取 已經被鎖的信息
				redisLockValue = jedisCluster.get(lockKey);
				if (null == redisLockValue)
					continue;
				// 3.获取lockKey 锁的 主机进程 线程
				String user = getLockUser(redisLockValue);
				// 4.判断当前主机进程 线程 与已经锁住的 主机进程 线程是否相等
				if (lockValuePre.equals(user)) {
					break;
				}
				// 5.判斷是否超時
				if (nowTime - startTime >= lockTimeout * 1000) {
					// 6. 如果超時了，檢查 鎖著 主机进程 线程 是否異常 如果異常
					if (checkLockIsErr(user)) {
						// 7. 刪除鎖
						RedisRetryHelper.execute(() -> {
							return jedisCluster.del(lockKey);
						});
						LOG.error("this redis lock key[" + key + "] err of " + redisLockValue);
						continue;
					}
				}
			}
			return null;
		});
	}

	private String getLockUser(String redisLockValue) {
		int index = redisLockValue.lastIndexOf("_");
		String urser = redisLockValue.substring(0, index);
		return urser;
	}

	private boolean checkLockIsErr(String redisLockValue) {
		return true;
	}

	public void unlock(String key) {
		String lockKey = PRE_LOCK.concat(key);
		RedisRetryHelper.execute(() -> {
			return jedisCluster.del(lockKey);
		});
	}

	public int llen(String key) {
		byte[] keyByte = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			Long llen = jedisCluster.llen(keyByte);
			return llen.intValue();
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
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
		String hostStr = configure.getConfig("redis.host", null);
		String[] hosts = hostStr.split(";");
		String[] temp = null;
		Set<HostAndPort> set = new HashSet<>();
		for (String host : hosts) {
			temp = host.split(":");
			set.add(new HostAndPort(temp[0], Integer.valueOf(temp[1])));
		}
		Integer timeout = configure.getConfig("redis.timeout", 200);
		lockTimeout = configure.getConfig("redis.lock.timeout", 60);
		jedisCluster = new EnhanceJedisCluster(set, timeout, config);
	}

	public Set<String> keys(String pattern) {
		return RedisRetryHelper.execute(() -> {
			return jedisCluster.keys(pattern);
		});
	}

	@PreDestroy
	public void destory() {
		try {
			jedisCluster.close();
		} catch (IOException e) {
			LOG.error("jedisCluster close err", e);
		}
	}

	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}
}
