package six.com.crawler.schedule.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月8日 上午9:45:43
 */
@Component
public class RedisWorkerPlugsManager extends AbstractWorkerPlugsManager {

	private static final String REDIS_KEY = "spider_redis_worker_plugs";

	@Autowired
	private RedisManager redisManager;

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	@Override
	protected boolean savePlugClassToCache(Class<?> clz) {
		redisManager.hset(REDIS_KEY, clz.getName(), clz);
		return true;
	}

	@Override
	protected Class<?> findFromCache(String workerClassName) {
		Class<?> clz = redisManager.hget(REDIS_KEY, workerClassName, Class.class);
		return clz;
	}

}
