package six.com.crawler.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;

import redis.clients.jedis.HostAndPort;
import six.com.crawler.BaseTest;
import six.com.crawler.dao.EnhanceJedisCluster;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.Page;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.utils.JavaSerializeUtils;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月9日 下午5:00:26
 */
public class RedisManagerTest extends BaseTest {

	@Test
	public void test() {
//		Set<String> keys=redisManager.keys("exCrawler_cache*");
//		for(String key:keys){
//			redisManager.del(key);
//		}
//		EnhanceJedisCluster jedis = newJedis();
//		String fuzzKey = RedisWorkSpace.WORK_ERR_QUEUE_KEY_PRE+ "*";
//		Set<String> keys = jedis.keys(fuzzKey);
//		for (String key : keys) {
//			jedis.del(key);
//		}
//
//		List<HttpProxy> list = httpPorxyService.getHttpProxys();
//		EnhanceJedisCluster jedis = newJedis();
//		final byte[] keyBytes = JavaSerializeUtils.serializeString(HttpProxyPool.REDIS_HTTP_PROXY_POOL);
//		jedis.del(keyBytes);
//		String fuzzKey = HttpProxyPool.REDIS_HTTP_PROXY_POOL + "_" + "*";
//		Set<String> keys = jedis.keys(fuzzKey);
//		for (String key : keys) {
//			jedis.del(key);
//		}
//		for(HttpProxy httpProxy:list){
//			jedis.hset(JavaSerializeUtils.serializeString(HttpProxyPool.REDIS_HTTP_PROXY_POOL),
//					JavaSerializeUtils.serializeString(httpProxy.toString()),
//					JavaSerializeUtils.serialize(httpProxy));
//		}
//		
//		
		// HttpProxyPool
		// Set<String>
		// keys=redisManager.keys(HttpProxyPool.REDIS_HTTP_PROXY_POOL+"_"+"*");
		// for(String key:keys){
		// redisManager.del(key);
		// }
		// String hkey = RedisWorkQueue.PRE_QUEUE_KEY + "cq315house_house_info";
		// int size=redisManager.hllen(hkey);
		// String cursorStr = "0";
		// int count=0;
		// do {
		// List<Page> list = new ArrayList<Page>();
		// cursorStr = redisManager.hscan(hkey,cursorStr, list, Page.class);
		// for (Page page : list) {
		// count++;
		// System.out.println("page key:" + page.getPageKey()+"|总数量:" +
		// count+"|游标:" + cursorStr);
		// }
		// list.clear();
		// } while (!"0".equals(cursorStr));
		// System.out.println("实际总数:" + size+"|获取总数:" + count);
	}

	public EnhanceJedisCluster newJedis() {
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
		String hostStr = "172.30.103.80:6379;172.30.103.80:6380;172.30.103.81:6379;172.30.103.81:6380;172.30.103.82:6379;172.30.103.82:6380;";
		String[] hosts = hostStr.split(";");
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
