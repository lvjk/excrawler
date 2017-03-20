package six.com.crawler.downer;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Request;
import six.com.crawler.BaseTest;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.http.HttpConstant;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.http.HttpResult;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月3日 下午1:55:10
 */
public class AbuHttpPorxyServiceTest extends BaseTest {

	protected final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceTest.class);
	private ExecutorService executor;
	final String url = "http://ip.url.cn/";
	final int loopCount = 5;
	long proxyTime;
	long noProxyTime;
	AtomicInteger count = new AtomicInteger(0);
	AtomicInteger errCount = new AtomicInteger(0);
	AtomicLong totalTime = new AtomicLong(0);

	@Test
	public void capacityTest() {

//		int allCountHttpProxy = 50;
//		executor = Executors.newFixedThreadPool(2);
//		final CountDownLatch countDownLatch = new CountDownLatch(allCountHttpProxy);
//		// 代理服务器
//		final String ProxyHost = "proxy.abuyun.com";
//		final Integer ProxyPort = 9020;
//		final String ProxyUser = "HM62C9280W4PA14D";
//		final String ProxyPass = "E94CD4A325BA8645";
//
//		Set<String> httpProxySet = new ConcurrentHashSet<>();
//		HttpProxy proxy = new HttpProxy();
//		proxy.setType(2);
//		proxy.setHost(ProxyHost);
//		proxy.setPort(ProxyPort);
//		proxy.setUserName(ProxyUser);
//		proxy.setPassword(ProxyPass);
//
//		jobWorkerManager.getHttpPorxyService().addHttpProxy(proxy);
//
//		for (int i = 0; i < allCountHttpProxy; i++) {
//			executor.execute(() -> {
//				try {
//					Request request = httpClient.buildRequest(url, null, HttpMethod.GET, HttpConstant.headMap, null,
//							null, proxy);
//					long start = System.currentTimeMillis();
//					HttpResult httpResult = httpClient.executeRequest(request);
//					String html = httpClient.getHtml(httpResult, ContentType.HTML);
//					String requestIp = Jsoup.parse(html).select("span[id=ip]").text();
//					long end = System.currentTimeMillis();
//					count.getAndIncrement();
//					long time = end - start;
//					totalTime.getAndAdd(time);
//					httpProxySet.add(StringUtils.trim(requestIp));
//					System.out.println("有效请求[" + requestIp + "]请求时间:" + time + "|代理总数:" + httpProxySet.size());
//				} catch (Exception e) {
//					System.out.println("无效请求:" + errCount.incrementAndGet());
//				} finally {
//					countDownLatch.countDown();
//				}
//			});
//
//		}
//		try {
//			countDownLatch.await();
//			executor.shutdown();
//			System.out.println("有效请求数 :" + count);
//			System.out.println("无效请求数 :" + (allCountHttpProxy - count.get()));
//			System.out.println("平均每个代理请求时间 :" + (totalTime.get() / count.get()==0?1:count.get()));
//			System.out.println("请求数/代理数:" + count + "/" + httpProxySet.size());
//		} catch (Exception e) {
//			LOG.error("capacityTest countDownLatch await err", e);
//		}
	}

}
