package six.com.crawler.rpc.service;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import six.com.crawler.rpc.AsyCallback;
import six.com.crawler.rpc.RpcCilent;
import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午5:56:21
 */
public class WrapperClientService implements MethodInterceptor {

	private RpcCilent rpcCilent;
	private String targetHost;
	private int targetPort;
	private AsyCallback asyCallback;
	private static Object emptyObject = new Object();
	private static AtomicInteger requestIndex = new AtomicInteger(0);

	public WrapperClientService(RpcCilent rpcCilent, String targetHost, int targetPort, AsyCallback asyCallback) {
		this.rpcCilent = rpcCilent;
		this.targetHost = targetHost;
		this.targetPort = targetPort;
		this.asyCallback = asyCallback;
	}

	@Override
	public Object intercept(Object arg0, Method method, Object[] args, MethodProxy arg3) throws Throwable {
		String requestId = createRequestId(targetHost, targetPort, method.getName());
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setId(requestId);
		rpcRequest.setCommand(method.getName());
		rpcRequest.setCallHost(targetHost);
		rpcRequest.setCallPort(targetPort);
		rpcRequest.setParams(args);
		RpcResponse rpcResponse = null;
		if (null == asyCallback) {
			rpcResponse = rpcCilent.synExecute(rpcRequest);
			return rpcResponse.getResult();
		} else {
			rpcCilent.asyExecute(rpcRequest, asyCallback);
			return emptyObject;
		}
	}

	/**
	 * 生成请求id
	 * @param targetHost
	 * @param targetPort
	 * @param serviceName
	 * @return
	 */
	private static String createRequestId(String targetHost, int targetPort, String serviceName) {
		String requestId = "@" + targetHost + ":" + targetPort + "/" + serviceName + "/" + System.currentTimeMillis()
				+ "/" + requestIndex.incrementAndGet();
		return requestId;
	}
}
