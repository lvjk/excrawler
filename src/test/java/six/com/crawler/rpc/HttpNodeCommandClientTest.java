package six.com.crawler.rpc;

import six.com.crawler.rpc.NettyRpcCilent;
import six.com.crawler.rpc.protocol.RpcProtocol;
import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;
import six.com.crawler.utils.ThreadUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午1:45:23
 */
public class HttpNodeCommandClientTest {
	public static void main(String[] a) throws InterruptedException {
		NettyRpcCilent client = new NettyRpcCilent();
		for (int i = 0; i < 2; i++) {
			String id = "192.168.12.80@192.168.12.80:" + 8180 + "/test/" + System.currentTimeMillis();
			RpcRequest rpcRequest = new RpcRequest();
			rpcRequest.setId(id);
			rpcRequest.setType(RpcProtocol.REQUEST);
			rpcRequest.setOriginHost("192.168.12.80");
			rpcRequest.setCallHost("192.168.12.80");
			rpcRequest.setCallPort(8180);
			rpcRequest.setCommand("test");
			rpcRequest.setParam("test");
			try {
				RpcResponse rpcResponse = client.execute(rpcRequest);
				if (null != rpcResponse) {
					System.out.println(rpcResponse.getResult());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ThreadUtils.sleep(1000000000);
		client.destroy();
	}
}
