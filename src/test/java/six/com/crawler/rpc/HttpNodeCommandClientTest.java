package six.com.crawler.rpc;

import java.util.HashMap;
import java.util.Map;

import six.com.crawler.rpc.NettyRpcCilent;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午1:45:23
 */
public class HttpNodeCommandClientTest {
	public static void main(String[] a) throws InterruptedException {
		NettyRpcCilent client = new NettyRpcCilent();
		int requestCount = 10000;
		long allTime = 0;
		Map<String, Object> params = new HashMap<>();
		String targetHost = "192.168.12.80";
		int targetPort = 8180;
		params.put("jobName", "test");
		TestService testService = client.lookupService(targetHost, targetPort, TestService.class,null);
		String name = "six";
		for (int i = 0; i < requestCount; i++) {
			try {
				long startTime = System.currentTimeMillis();
				String result = testService.say(name + i);
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				allTime += totalTime;
				System.out.println("result:" + result + "|消耗时间:" + totalTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("总消耗时间:" + allTime);
		client.destroy();
	}
}
