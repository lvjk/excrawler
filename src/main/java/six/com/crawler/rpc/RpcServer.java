package six.com.crawler.rpc;

import six.com.crawler.rpc.service.WrapperServerService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 rpc Server
 */
public interface RpcServer {


	/**
	 * 基于Rpc Service注解注册
	 * 
	 * @param tagetOb
	 */
	public void register(Object tagetOb);

	/**
	 * 通过rpcServiceName 获取 RpcService
	 * 
	 * @param rpcServiceName
	 * @return
	 */
	public WrapperServerService get(String rpcServiceName);

	/**
	 * 通过rpcServiceName 移除指定 RpcService
	 * 
	 * @param rpcServiceName
	 */
	public void remove(String rpcServiceName);
}
