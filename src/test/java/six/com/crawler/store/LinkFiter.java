package six.com.crawler.store;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年4月15日 下午6:48:47 类说明
 */
public class LinkFiter {

	final static int maxChar = 16;
	
	static BigDecimal max=new BigDecimal(maxChar);
	
	static JedisPool pool;
	
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(50);
		config.setMaxIdle(20);
		config.setMaxWaitMillis(60000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		pool = new JedisPool(config, "127.0.0.1", 6379, 100000);
	}

	public static void main(String[] a) throws IOException {
		int max=1000000;
		String temp="http://geek.csdn.net/news/detail/294661";
		String newStr=null;
		String MD5=null;
		List<String> list=new ArrayList<String>();
		for(int i=0;i<max;i++){
			newStr=temp+i;
			MD5=DigestUtils.md5Hex(newStr);
			list.add(MD5);
		}
		for(int i=0;i<0;i++){
			MD5=list.get(i);
			save(MD5);
		}
		for(int i=0;i<list.size();i++){
			MD5=list.get(i);
			if (isLive(MD5)) {
				System.out.println("存在");
			}else{
				System.out.println("不存在");
			}
		}
		pool.close();
	}

	@SuppressWarnings("deprecation")
	public static boolean isLive(String MD5) {
		Jedis jedis = pool.getResource();
		final int size = MD5.length();
		char c = 0;
		BigDecimal key = null;
		Boolean response = null;
		BigDecimal lastLayerIndex = null;
		int lastOffsetIndex = 1;
		int tempOffset=1;
		BigDecimal beforeCout=null;
		BigDecimal temp=null;
		BigDecimal tempKey=null;
		try {
			for (int depth = 0; depth < size; depth++) {
				beforeCout=new BigDecimal(0);
				c = MD5.charAt(depth);
				tempOffset= getOffset(c);
				tempKey = getKey(depth, lastLayerIndex, lastOffsetIndex, c);
				for(int i=depth-1;i>=0;i--){
					temp=max.pow(i);
					beforeCout=beforeCout.add(temp);
				}
				key=beforeCout.add(tempKey);//273
				response = jedis.getbit(key.toString(), tempOffset);
				if (!response) {// 判断是否存在
					return false;
				}
				lastOffsetIndex = tempOffset;
				lastLayerIndex = tempKey;
			}
		} finally {
			pool.returnBrokenResource(jedis);
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static void save(String MD5) {
		Jedis jedis = pool.getResource();
		final int size = MD5.length();
		char c = 0;
		BigDecimal key = null;
		BigDecimal lastLayerIndex = null;
		int lastOffsetIndex = 1;
		int tempOffset=1;
		BigDecimal beforeCout=null;
		BigDecimal temp=null;
		BigDecimal tempKey=null;
		try {
			for (int depth = 0; depth < size; depth++) {
				beforeCout=new BigDecimal(0);
				c = MD5.charAt(depth);
				tempOffset= getOffset(c);
				tempKey = getKey(depth, lastLayerIndex, lastOffsetIndex, c);
				for(int i=depth-1;i>=0;i--){
					temp=max.pow(i);
					beforeCout=beforeCout.add(temp);
				}
				key=beforeCout.add(tempKey);
				jedis.setbit(key.toString(), tempOffset, true);
				lastOffsetIndex = tempOffset;
				lastLayerIndex = tempKey;
			}

		} catch (Throwable t) {

		} finally {
			pool.returnBrokenResource(jedis);
		}
	}

	public static BigDecimal getKey(int depath, BigDecimal lastLayerIndex, int lastOffsetIndex, char c) {
		if (depath == 0) {
			return new BigDecimal(1);
		}
		// (lastLayerIndex-1)*maxDepth+lastOffsetIndex;
		BigDecimal defaultBegin=new BigDecimal(1);
		BigDecimal index = lastLayerIndex.subtract(defaultBegin);
		index=index.multiply(max);
		index=index.add(new BigDecimal(lastOffsetIndex));
		return index;
	}

	public static int getOffset(char c) {
		int temp = c;
		if (c <= 57) {
			return temp - 47;
		} else {
			return temp - 86;
		}
	}
}
