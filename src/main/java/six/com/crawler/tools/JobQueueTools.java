package six.com.crawler.tools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.HostAndPort;
import six.com.crawler.dao.EnhanceJedisCluster;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Page;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;

/**
 * 任务队列工具
 * @author weijiyong@tospur.com
 *
 */
public class JobQueueTools {
	
	private static String workSpaceName = "nb_cnnbfdc_project_info";
	private static String queueKey = "spider_redis_store_page_queue_" + workSpaceName;
	private static String proxyQueueKey = "spider_redis_store_page_proxy_queue_" + workSpaceName;
	
	private static String hostStr = "172.18.84.44:6379;172.18.84.45:6379;172.18.84.46:6379";
	
	public static void main(String[] args) {
		if(args.length<10){
			System.err.println("Use : JobQueueTools -jobName ${jobName} -redisHosts ${redisHosts} -siteCode ${siteCode} -url ${url} -httpMethod ${httpMethod} [-params ${params} -metamMap ${metaMap}]");
		}
		
		String siteCode=null;
		String url=null;
		String method=null;
		String metaMapStr=null;
		String paramStr=null;
		for (int i = 0; i < args.length; i++) {
			if(args[i].equals("-jobName")){
				workSpaceName=args[i+1];
				queueKey = "spider_redis_store_page_queue_" + workSpaceName;
				proxyQueueKey = "spider_redis_store_page_proxy_queue_" + workSpaceName;
			}else if(args[i].equals("-jobName")){
				hostStr=args[i+1];
			}else if(args[i].equals("-redisHosts")){
				hostStr=args[i+1];
			}else if(args[i].equals("-siteCode")){
				siteCode=args[i+1];
			}else if(args[i].equals("-url")){
				url=args[i+1];
			}else if(args[i].equals("-method")){
				method=args[i+1];
			}else if(args[i].equals("-metaMap")){
				metaMapStr=args[i+1];
			}else if(args[i].equals("-params")){
				paramStr=args[i+1];
			}
		}
		
		RedisManager redisManager=new RedisManager();
		
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
		
		String[] hosts = hostStr.split(";");
		String[] temp = null;
		Set<HostAndPort> set = new HashSet<>();
		for (String host : hosts) {
			temp = host.split(":");
			set.add(new HostAndPort(temp[0], Integer.valueOf(temp[1])));
		}
		Integer timeout = 200;
		redisManager.setJedisCluster(new EnhanceJedisCluster(set, timeout, config));
		
		Page page=new Page(siteCode,1,url,url);
		if(method.equals(HttpMethod.GET.value)){
			page.setMethod(HttpMethod.GET);
		}else{
			page.setMethod(HttpMethod.POST);
			if(null!=paramStr){
				Map<String,Object> params=(Map<String, Object>) JSON.parseObject(paramStr);
				page.setParameters(params);
			}
		}
		
		if(null!=metaMapStr){
			Map<String,Object> params=(Map<String, Object>) JSON.parseObject(metaMapStr);
			for (String key:params.keySet()) {
				page.getMetaMap().put(key, ArrayListUtils.asList(params.get(key).toString()));
			}
		}
		
		try {
			redisManager.hset(queueKey, page.getKey(), page);
			redisManager.rpush(proxyQueueKey, page.getKey());
		} catch (Exception e) {
			throw e;
		} 
	}
}
