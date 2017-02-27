package six.com.crawler.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.exception.RedisException;
import six.com.crawler.common.utils.ThreadUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月7日 下午12:03:12
 * 
 *       redis 失败重试 helper
 * 
 */
public class RedisRetryHelper {

	private final static Logger LOG = LoggerFactory.getLogger(RedisRetryHelper.class);
	// redis 每次尝试 睡眠时间 10s
	private final static int interval = 10 * 1000;
	// redis 最大尝试次数
	private final static int retryMaxCount = 3;

	/**
	 * 执行 redisCommand 成功直接返回，如果失败异常那么重试 retryMaxCount=3次，每次间隔10s
	 * 
	 * @param redisCommand
	 * @return
	 */
	public static <T> T execute(RedisCommand<T> redisCommand) {
		int retryCount = 0;
		Exception exception = null;
		while (retryCount < retryMaxCount) {
			try {
				return redisCommand.execute();
			} catch (Exception e) {
				retryCount++;
				exception = e;
				LOG.error("retry operation redis count:" + retryCount, e);
				ThreadUtils.sleep(interval);
			}
		}
		throw new RedisException("operation redis err", exception);
	}
}
