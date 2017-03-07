package six.com.crawler.downer;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Request;
import six.com.crawler.BaseTest;
import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.HttpProxyType;
import six.com.crawler.common.http.HttpConstant;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpProxyPool;
import six.com.crawler.common.http.HttpResult;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午2:13:09
 */
public class HttpPorxyServiceTest extends BaseTest {

	protected final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceTest.class);
	final String url = "http://androidguy.blog.51cto.com/974126/214448";
	final int loopCount = 5;
	long proxyTime;
	long noProxyTime;

	@Test
	public void capacityTest() {
		HttpProxyPool httpProxyPool=httpPorxyService.buildHttpProxyPool("HttpPorxyServiceTest", HttpProxyType.ENABLE_MANY, 0);
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		new Thread(() -> {
			long start = System.currentTimeMillis();
			for (int i = 0; i < loopCount; i++) {
				HttpProxy httpProxy = httpProxyPool.getHttpProxy();
				Request request = httpClient.buildRequest(url, null, HttpMethod.GET, HttpConstant.headMap, null, null,
						httpProxy);
				try {
					HttpResult httpResult = httpClient.executeRequest(request);
					LOG.info("capacityTest proxy request:" + httpResult.getCode());
				} catch (Exception e) {
					LOG.error("capacityTest executeRequest httpResult err:" + request.url(), e);
				}
			}
			long end = System.currentTimeMillis();
			proxyTime = end - start;
			countDownLatch.countDown();
		}).start();
		new Thread(() -> {
			long start = System.currentTimeMillis();
			for (int i = 0; i < loopCount; i++) {
				Request request = httpClient.buildRequest(url, null, HttpMethod.GET, HttpConstant.headMap, null, null,
						null);
				try {
					HttpResult httpResult = httpClient.executeRequest(request);
					LOG.info("capacityTest noproxy request:" + httpResult.getCode());
				} catch (Exception e) {
					LOG.error("capacityTest executeRequest httpResult err:" + request.url(), e);
				}
			}
			long end = System.currentTimeMillis();
			noProxyTime = end - start;
			countDownLatch.countDown();
		}).start();
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			LOG.error("capacityTest countDownLatch await err", e);
		}
		LOG.info("capacityTest proxyTime:" + proxyTime);
		LOG.info("capacityTest noProxyTime:" + noProxyTime);
		LOG.info("capacityTest noProxyTime-proxyTime:" + (noProxyTime - proxyTime));
	}

	@Test
	public void useTest() {
		HttpProxyPool httpProxyPool=httpPorxyService.buildHttpProxyPool("HttpPorxyServiceTest", HttpProxyType.ENABLE_MANY, 0);
		for (int i = 0; i < loopCount; i++) {
			HttpProxy httpProxy = httpProxyPool.getHttpProxy();
			LOG.info("useTest use httpProxy:" + httpProxy.toString());
			Request request = httpClient.buildRequest(url, null, HttpMethod.GET, HttpConstant.headMap, null, null,
					httpProxy);
			try {
				HttpResult httpResult = httpClient.executeRequest(request);
				LOG.info("useTest proxy request:" + httpResult.getCode());
			} catch (Exception e) {
				LOG.error("useTest executeRequest httpResult err:" + request.url(), e);
			}
		}
	}

}
