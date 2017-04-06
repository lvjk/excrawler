package six.com.crawler.rpc;

import java.util.HashMap;
import java.util.Map;

import six.com.crawler.rpc.NettyRpcCilent;
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
		int requestCount=10000;
		long allTime=0;
		Map<String,Object> params=new HashMap<>();
		params.put("jobName","test");
		for (int i = 0; i <requestCount; i++) {
			String id = "192.168.12.80@192.168.12.80:" + 8180 + "/test/" + System.currentTimeMillis();
			RpcRequest rpcRequest = new RpcRequest();
			rpcRequest.setId(id);
			rpcRequest.setOriginHost("192.168.12.80");
			rpcRequest.setCallHost("192.168.12.80");
			rpcRequest.setCallPort(8180);
			rpcRequest.setCommand("test");
			rpcRequest.setParams(params);
			
			try {
				long startTime=System.currentTimeMillis();
				RpcResponse rpcResponse = client.execute(rpcRequest);
				long endTime=System.currentTimeMillis();
				long totalTime=endTime-startTime;
				allTime+=totalTime;
				if (null != rpcResponse) {
					System.out.println("result:"+rpcResponse.getResult()+"|消耗时间:"+totalTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("总消耗时间:"+allTime);
		client.destroy();
	}
}
