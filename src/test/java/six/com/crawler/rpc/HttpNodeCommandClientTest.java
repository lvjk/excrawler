package six.com.crawler.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import six.com.crawler.rpc.NettyRpcCilent;
import six.com.crawler.schedule.JobWorkerThreadFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午1:45:23
 */
public class HttpNodeCommandClientTest {
	
	static volatile long allTime = 0;
	static volatile int index = 0;
	
	public static void main(String[] a) throws InterruptedException {
		NettyRpcCilent client = new NettyRpcCilent();
		Map<String, Object> params = new HashMap<>();
		String targetHost = "192.168.12.80";
		int targetPort = 8180;
		params.put("jobName", "test");
		TestService testService = client.lookupService(targetHost, targetPort, TestService.class,null);
		String name = "six";
		int requestCount = 1000;
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		CountDownLatch cdl=new CountDownLatch(requestCount);
		for (int i = 0; i < requestCount; i++) {
			executor.execute(()->{
				try {
					long startTime = System.currentTimeMillis();
					String result = testService.say(name +"-"+index++);
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					allTime += totalTime;
					System.out.println("result:" + result + "|消耗时间:" + totalTime);
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					cdl.countDown();
				}
			});
		}
		cdl.await();
		System.out.println("总消耗时间:" + allTime);
		System.out.println("平均消耗时间:" + allTime/index);
		client.destroy();
		executor.shutdown();
	}
}
