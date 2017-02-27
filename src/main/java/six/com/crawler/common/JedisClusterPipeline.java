package six.com.crawler.common;

import java.io.Closeable;
import java.io.IOException;

import redis.clients.jedis.Client;
import redis.clients.jedis.PipelineBase;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月23日 下午12:17:42
 */
public class JedisClusterPipeline extends PipelineBase implements Closeable {

	@Override
	public void close() throws IOException {

	}

	@Override
	protected Client getClient(String key) {
		return null;
	}

	@Override
	protected Client getClient(byte[] key) {

        return null;
	}

}
