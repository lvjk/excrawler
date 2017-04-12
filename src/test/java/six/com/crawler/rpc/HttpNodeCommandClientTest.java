package six.com.crawler.rpc;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import six.com.crawler.rpc.NettyRpcCilent;

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
		String targetHost = "192.168.12.80";
		int targetPort = 8180;
		int requestCount = 1;
		CountDownLatch cdl=new CountDownLatch(requestCount);
		TestService testService = client.lookupService(targetHost, targetPort, TestService.class,result->{
			System.out.println("result:" + result);
		});
		String name = "six";
		
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		for (int i = 0; i < requestCount; i++) {
			executor.execute(()->{
				try {
					long startTime = System.currentTimeMillis();
					Object result=testService.say(name +"-"+index++);
					System.out.println(result);
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					allTime += totalTime;
					System.out.println("消耗时间:" + totalTime);
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
