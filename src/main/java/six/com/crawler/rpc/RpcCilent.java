package six.com.crawler.rpc;

import six.com.crawler.rpc.handler.ClientToServerConnection;
import six.com.crawler.rpc.protocol.RpcRequest;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:14 集群命令 client
 */
public interface RpcCilent {

	/**
	 * 通过 命令名 node 称获取对应的 NodeCommand
	 * 
	 * @param nodeCommandName
	 *            命令名称
	 * @param node
	 *            提供命令的节点
	 * @return
	 */
	public Object execute(RpcRequest RPCRequest);
	
	public WrapperFuture getRequest(String requestId);
	
	public long getCallTimeout();
	
	public void removeConnection(ClientToServerConnection connection);
}
