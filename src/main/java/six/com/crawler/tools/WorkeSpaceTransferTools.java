package six.com.crawler.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import six.com.crawler.dao.EnhanceJedisCluster;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Page;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.redis.RedisUtils;
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
			EnhanceJedisCluster doRedis = RedisUtils.newJedis(redisConnection);
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
}
