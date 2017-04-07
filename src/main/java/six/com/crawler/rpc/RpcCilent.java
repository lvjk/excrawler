package six.com.crawler.rpc;

import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:14 集群命令 client
 */
public interface RpcCilent {

	/**
	 * 同步执行请求
	 * @param RPCRequest
	 * @return
	 */
	public RpcResponse synExecute(RpcRequest RPCRequest);

	/**
	 * 异步执行请求，请求完成时 ，执行回调callback
	 * @param rpcRequest
	 * @param callback
	 */
	public void asyExecute(RpcRequest rpcRequest, AsyCallback callback);
	/**
	 * 如果callback等于null那么 为同步调用，当callback 不等于null时 为异步调用
	 * 
	 * @param currentHost
	 * @param currentPort
	 * @param targetHost
	 * @param targetPort
	 * @param clz
	 * @param callback
	 * @return
	 */
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz,
			AsyCallback callback);

	public void putWrapperFuture(String requestId,WrapperFuture wrapperFuture);
	
	public WrapperFuture takeWrapperFuture(String requestId);

	public long getCallTimeout();

	public void removeConnection(ClientToServerConnection connection);
}
