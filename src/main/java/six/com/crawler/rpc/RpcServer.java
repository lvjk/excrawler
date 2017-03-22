package six.com.crawler.rpc;

import six.com.crawler.node.NodeCommand;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 集群命令 Server
 */
public interface RpcServer {

	/**
	 * 注册节点命令
	 * 
	 * @param nodeCommand
	 */
	public void register(String commandName, NodeCommand nodeCommand);
	
	
	public NodeCommand get(String commandName);

	/**
	 * 移除节点命令
	 * 
	 * @param nodeCommand
	 */
	public void remove(String commandName);
}
