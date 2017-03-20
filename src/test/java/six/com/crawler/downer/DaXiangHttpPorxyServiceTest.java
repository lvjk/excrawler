package six.com.crawler.downer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
 * @date 创建时间：2017年3月3日 下午1:00:23
 */
public class DaXiangHttpPorxyServiceTest extends BaseTest {

	protected final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceTest.class);
	private ExecutorService executor = Executors.newFixedThreadPool(200);
	final String url = "http://ip.url.cn/";
	final int loopCount = 5;
	long proxyTime;
	long noProxyTime;
	AtomicInteger count = new AtomicInteger(0);
	AtomicInteger errCount = new AtomicInteger(0);
	AtomicLong totalTime = new AtomicLong(0);

	@Test
	public void capacityTest() {
//		String fileName = "C:/Users/38134/Downloads/下载.txt";
//		int allCountHttpProxy = 0;
//		try {
//			List<String> httpProxyList = FileUtils.readLines(new File(fileName));
//			Set<String> httpProxySet = new HashSet<>();
//			for (String httpProxyStr : httpProxyList) {
//				httpProxySet.add(httpProxyStr);
//			}
//			allCountHttpProxy = httpProxySet.size();
//			final CountDownLatch countDownLatch = new CountDownLatch(allCountHttpProxy);
//			for (final String httpProxyStr : httpProxySet) {
//				executor.execute(() -> {
//					HttpProxy httpProxy = new HttpProxy();
//					String[] httpProxyStrs = StringUtils.split(httpProxyStr, ":");
//					httpProxy.setHost(httpProxyStrs[0]);
//					httpProxy.setPort(Integer.valueOf(httpProxyStrs[1]));
//					Request request = httpClient.buildRequest(url, null, HttpMethod.GET, HttpConstant.headMap, null,
//							null, httpProxy);
//					try {
//						long start = System.currentTimeMillis();
//						HttpResult httpResult = httpClient.executeRequest(request);
//						String html = httpClient.getHtml(httpResult, ContentType.HTML);
//						String requestIp = Jsoup.parse(html).select("span[id=ip]").text();
//						long end = System.currentTimeMillis();
//						count.getAndIncrement();
//						long time = end - start;
//						totalTime.getAndAdd(time);
//						System.out.println("有效代理[" + httpProxy.toString() + "]请求时间:" + time + "|是否匿名"
//								+ StringUtils.contains(httpProxy.toString(), requestIp));
//					} catch (Exception e) {
//						System.out.println("无效代理[" + httpProxy.toString() + "]:" + errCount.incrementAndGet());
//					} finally {
//						countDownLatch.countDown();
//					}
//				});
//			}
//			try {
//				countDownLatch.await();
//				executor.shutdown();
//			} catch (InterruptedException e) {
//				LOG.error("capacityTest countDownLatch await err", e);
//			}
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		System.out.println("有效代理数 :" + count);
//		System.out.println("无效代理数 :" + (allCountHttpProxy - count.get()));
//		System.out.println("平均每个代理请求时间 :" + (totalTime.get() / count.get()));
	}
}
