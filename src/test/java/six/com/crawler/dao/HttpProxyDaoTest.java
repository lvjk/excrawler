package six.com.crawler.dao;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.BaseTest;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.work.downer.HttpProxyPool;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月19日 上午9:56:34
 */
public class HttpProxyDaoTest extends BaseTest {

	protected final static Logger log = LoggerFactory.getLogger(HttpProxyDaoTest.class);

	@Test
	public void test() {
		//getAll();
		//moveRedisToDb();
	}

	protected void getAll() {
		List<HttpProxy> list = httpProxyDao.getAll();
		for (HttpProxy httpProxy : list) {
			log.info("http代理:" + httpProxy.toString());
		}
		log.info("http代理总数:" + list.size());
	}

	protected void moveRedisToDb() {
		Map<String, HttpProxy> map = redisManager.hgetAll(HttpProxyPool.REDIS_HTTP_PROXY_POOL, HttpProxy.class);
		for (HttpProxy httpProxy : map.values()) {
			try {
				httpProxyDao.save(httpProxy);
				log.info("保存http代理:" + httpProxy.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
