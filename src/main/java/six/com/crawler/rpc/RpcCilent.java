package six.com.crawler.rpc;

import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:14 rpc服务调用 客户端
 */
public interface RpcCilent {

	/**
	 * 同步执行请求
	 * 
	 * @param RPCRequest
	 * @return
	 */
	public RpcResponse synExecute(RpcRequest RPCRequest);

	/**
	 * 异步执行请求，请求完成时 ，执行回调callback
	 * 
	 * @param rpcRequest
	 * @param callback
	 */
	public void asyExecute(RpcRequest rpcRequest, AsyCallback callback);

	/**
	 * 如果callback等于null那么 为同步调用，当callback 不等于null时 为异步调用
	 * @param targetHost
	 * @param targetPort
	 * @param clz
	 * @param callback
	 * @return
	 */
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, AsyCallback callback);

	/**
	 * 将执行的request 的 WrapperFuture 存放 缓存
	 * 
	 * @param requestId
	 * @param wrapperFuture
	 */
	public void putWrapperFuture(String requestId, WrapperFuture wrapperFuture);

	/**
	 * 通过 requestId 获取 对应 wrapperFuture，并从缓存中移除
	 * 
	 * @param requestId
	 * @return
	 */
	public WrapperFuture takeWrapperFuture(String requestId);

	/**
	 * 获取call 超时时间
	 * 
	 * @return
	 */
	public long getCallTimeout();

	/**
	 * 从缓存中移除链接
	 * 
	 * @param connection
	 */
	public void removeConnection(ClientToServerConnection connection);
}
