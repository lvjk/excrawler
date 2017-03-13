package six.com.crawler.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esotericsoftware.minlog.Log;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.entity.DoneInfo;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.QueueInfo;
import six.com.crawler.common.service.WorkQueueService;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月8日 下午3:27:27
 */

@Service
public class WorkQueueServiceImpl implements WorkQueueService {

	@Autowired
	private RedisManager redisManager;

	public Map<String,Object> getQueueInfo(String queueName,String queueCursor) {
		Map<String,Object> resultMap=new HashMap<>();
		String queueKey = RedisWorkQueue.PRE_QUEUE_KEY + queueName;
		List<Page> list=new ArrayList<>();
		queueCursor=redisManager.hscan(queueKey, queueCursor, list, Page.class);
		resultMap.put("queueCursor", queueCursor);
		resultMap.put("list",list);
		return resultMap;
	}

	/**
	 * 默认查10条数据
	 */
	public List<Page> getErrQueueInfo(String queueName, int index) {
		String errQueuekey = RedisWorkQueue.PRE_ERR_QUEUE_KEY + queueName;
		List<Page> list = redisManager.lrange(errQueuekey, index, index + 10, Page.class);
		return list;
	}

	@Override
	public String cleanQueue(String queueName) {
		String proxyQueuekey = RedisWorkQueue.PRE_PROXY_QUEUE_KEY + queueName;
		String realQueuekey = RedisWorkQueue.PRE_QUEUE_KEY + queueName;
		String errQueuekey = RedisWorkQueue.PRE_ERR_QUEUE_KEY + queueName;
		redisManager.lock(realQueuekey);
		String msg = "";
		try {
			redisManager.del(proxyQueuekey);
			redisManager.del(realQueuekey);
			redisManager.del(errQueuekey);
			msg = "clean queue[" + queueName + "] succeed";
			Log.info(msg);
		} finally {
			redisManager.unlock(realQueuekey);
		}
		return msg;
	}

	@Override
	public String repairQueue(String queueName) {
		String proxyQueuekey = RedisWorkQueue.PRE_PROXY_QUEUE_KEY + queueName;
		String realQueuekey = RedisWorkQueue.PRE_QUEUE_KEY + queueName;
		redisManager.lock(realQueuekey);
		try {
			int proxyQueueLlen = redisManager.llen(proxyQueuekey);
			int queueKeyLlen = redisManager.hllen(realQueuekey);
			if (queueKeyLlen != proxyQueueLlen) {
				redisManager.del(proxyQueuekey);
				Map<String, Page> findMap = redisManager.hgetAll(realQueuekey, Page.class);
				if (null != findMap) {
					for (String key : findMap.keySet()) {
						redisManager.rpush(proxyQueuekey, key);
					}
				}
			}
		} finally {
			redisManager.unlock(realQueuekey);
		}
		return "repair queue[" + queueName + "] succeed";
	}

	@Override
	public List<DoneInfo> getQueueDones() {
		Set<String> proxyQueuekeys = redisManager.keys(RedisWorkQueue.PRE_DONE_DUPLICATE_KEY + "*");
		List<DoneInfo> doneInfos = new ArrayList<>();
		DoneInfo tempDoneInfo = null;
		for (String tempKey : proxyQueuekeys) {
			int size = redisManager.hllen(tempKey);
			String queueName = StringUtils.remove(tempKey, RedisWorkQueue.PRE_DONE_DUPLICATE_KEY);
			tempDoneInfo = new DoneInfo();
			tempDoneInfo.setQueueName(queueName);
			tempDoneInfo.setSize(size);
			doneInfos.add(tempDoneInfo);
		}
		return doneInfos;
	}

	@Override
	public String cleanQueueDones(String queueName) {
		String doneKey = RedisWorkQueue.PRE_DONE_DUPLICATE_KEY + queueName;
		redisManager.del(doneKey);
		return "clean doneQueue[" + queueName + "] succeed";
	}

	public QueueInfo getQueueInfos(String queueName) {
		QueueInfo tempQueueInfo = new QueueInfo();
		tempQueueInfo.setQueueName(queueName);
		String tempProxyKey = RedisWorkQueue.PRE_PROXY_QUEUE_KEY + queueName;
		int proxySize = redisManager.llen(tempProxyKey);
		tempQueueInfo.setProxyQueueSize(proxySize);

		String tempRealKey = RedisWorkQueue.PRE_QUEUE_KEY + queueName;
		int realSize = redisManager.hllen(tempRealKey);
		tempQueueInfo.setRealQueueSize(realSize);

		String tempErrKey = RedisWorkQueue.PRE_ERR_QUEUE_KEY + queueName;
		int errSize = redisManager.hllen(tempErrKey);
		tempQueueInfo.setErrQueueCount(errSize);

		return tempQueueInfo;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

}
