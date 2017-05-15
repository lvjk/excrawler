package six.com.crawler.dao;

import java.io.IOException;
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
import redis.clients.jedis.ScanResult;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.utils.JavaSerializeUtils;

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

	public <T> void lpushAll(String key, List<T> values) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		for (T value : values) {
			byte[] valueBytes = JavaSerializeUtils.serialize(value);
			RedisRetryHelper.execute(() -> {
				return jedisCluster.lpush(keyBytes, valueBytes);
			});
		}
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

	public String lset(String key, int index, Object value) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		byte[] valueBytes = JavaSerializeUtils.serialize(value);
		return RedisRetryHelper.execute(() -> {
			return jedisCluster.lset(keyBytes, index, valueBytes);
		});
	}

	public int lrem(String key, int count, Object value) {
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

	public int incrInt(String key) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		int incr = RedisRetryHelper.execute(() -> {
			return jedisCluster.incr(keyBytes).intValue();
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

	/**
	 * 扫描 hset
	 * 
	 * @param hkey
	 * @param cursorStr
	 * @param list
	 * @param clz
	 * @return
	 */
	public <T> String hscan(String hkey, String cursorStr, Map<String, T> map, Class<T> clz) {
		byte[] hKeyByte = JavaSerializeUtils.serializeString(hkey);
		byte[] cursorByte = JavaSerializeUtils.serializeString(cursorStr);
		return RedisRetryHelper.execute(() -> {
			ScanResult<Map.Entry<byte[], byte[]>> scanResult = jedisCluster.hscan(hKeyByte, cursorByte);
			List<Map.Entry<byte[], byte[]>> scanResultltList = scanResult.getResult();
			for (int i = 0; i < scanResultltList.size(); i++) {
				Map.Entry<byte[], byte[]> mapentry = scanResultltList.get(i);
				byte[] keyBytes = mapentry.getKey();
				byte[] valueBytes = mapentry.getValue();
				String putKey = JavaSerializeUtils.unSerializeString(keyBytes);
				T t = JavaSerializeUtils.unSerialize(valueBytes, clz);
				map.put(putKey, t);
			}
			return scanResult.getStringCursor();
		});
	}

	/**
	 * 扫描 hset
	 * 
	 * @param hkey
	 * @param cursorStr
	 * @param list
	 * @param clz
	 * @return
	 */
	public <T> String hscan(String hkey, String cursorStr, List<T> list, Class<T> clz) {
		byte[] hKeyByte = JavaSerializeUtils.serializeString(hkey);
		byte[] cursorByte = JavaSerializeUtils.serializeString(cursorStr);
		return RedisRetryHelper.execute(() -> {
			ScanResult<Map.Entry<byte[], byte[]>> scanResult = jedisCluster.hscan(hKeyByte, cursorByte);
			List<Map.Entry<byte[], byte[]>> scanResultltList = scanResult.getResult();
			for (int i = 0; i < scanResultltList.size(); i++) {
				Map.Entry<byte[], byte[]> mapentry = scanResultltList.get(i);
				byte[] valueBytes = mapentry.getValue();
				T t = JavaSerializeUtils.unSerialize(valueBytes, clz);
				list.add(t);
			}
			return scanResult.getStringCursor();
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

	/**
	 * 
	 * @param listkey
	 *            集合key
	 * @param valuekey
	 *            数据key
	 * @param value
	 *            数据
	 */
	public <T> List<T> hgetAllList(String hkey, Class<T> clz) {
		byte[] hKeyBytes = JavaSerializeUtils.serializeString(hkey);
		return RedisRetryHelper.execute(() -> {
			Map<byte[], byte[]> map = jedisCluster.hgetAll(hKeyBytes);
			List<T> result = new ArrayList<>(map.size());
			map.forEach((mapKey, mapValue) -> {
				T t = JavaSerializeUtils.unSerialize(mapValue, clz);
				result.add(t);
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

	public boolean isExecuted(String key) {
		byte[] keyBytes = JavaSerializeUtils.serializeString(key);
		return RedisRetryHelper.execute(() -> {
			byte[] result = jedisCluster.get(keyBytes);
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

	public void setJedisCluster(EnhanceJedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}
}
