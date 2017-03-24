package six.com.crawler.rpc;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 
 *    rpc Server
 */
public interface RpcServer {

	/**
	 * 注册rpc service
	 * @param rpcServiceName
	 * @param rpcService
	 */
	public void register(String rpcServiceName, RpcService rpcService);
	
	
	/**
	 * 通过rpcServiceName 获取 RpcService
	 * @param rpcServiceName
	 * @return
	 */
	public RpcService get(String rpcServiceName);

	/**
	 * 通过rpcServiceName 移除指定 RpcService
	 * @param rpcServiceName
	 */
	public void remove(String rpcServiceName);
}
