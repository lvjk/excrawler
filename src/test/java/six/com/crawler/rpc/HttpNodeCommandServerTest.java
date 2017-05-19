package six.com.crawler.rpc;

import six.com.crawler.rpc.NettyRpcServer;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午1:41:50
 */
public class HttpNodeCommandServerTest {

	public static void main(String[] a) throws InterruptedException {
		NettyRpcServer server = new NettyRpcServer("192.168.12.27", 8180);
		server.register(new TestServiceImpl());
		synchronized (server) {
			server.wait();
		}
	}
}
