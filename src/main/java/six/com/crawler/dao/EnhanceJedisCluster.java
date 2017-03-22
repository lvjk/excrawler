package six.com.crawler.dao;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年12月19日 上午10:37:57 
*/
public class EnhanceJedisCluster extends JedisCluster{

	public EnhanceJedisCluster(HostAndPort node, int timeout, int maxAttempts) {
		super(node, timeout, maxAttempts);
	}
	
	public EnhanceJedisCluster(Set<HostAndPort> nodes, int timeout, final GenericObjectPoolConfig poolConfig) {
		super(nodes, timeout, DEFAULT_MAX_REDIRECTIONS, poolConfig);
	}
	
	public Set<String> keys(String pattern) {
		Set<String> keys = new TreeSet<>();
		Map<String, JedisPool> clusterNodes = getClusterNodes();
		for (String k : clusterNodes.keySet()) {
			JedisPool jp = clusterNodes.get(k);
			Jedis connection = jp.getResource();
			try {
				keys.addAll(connection.keys(pattern));
			} finally {
				// 用完一定要close这个链接
				connection.close();
			}
		}
		return keys;
	}
	
	

}
