package six.com.crawler.node;

import java.util.List;

import six.com.crawler.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 下午1:41:16 集群节点管理类 所有集群服务最基本的依赖
 * 
 *       集群节点管理接口:提供基本的集群和节点的操作
 */

public interface NodeManager {

	/**
	 * 获取当前节点
	 * 
	 * @return 当前节点
	 */
	public Node getCurrentNode();

	/**
	 * 获取主节点
	 * 
	 * @return 主节点
	 */
	public Node getMasterNode();

	/**
	 * 获取所有工作节点
	 * 
	 * @return
	 */
	public List<Node> getWorkerNodes();

	/**
	 * 通过节点名获取节点
	 * 
	 * @param nodeName
	 * @return
	 */
	public Node getWorkerNode(String nodeName);

	/**
	 * 获取空闲可用节点
	 * 
	 * @param needFresNodes
	 * @return
	 */
	public List<Node> getFreeWorkerNodes(int needFresNodes);

	/**
	 * 获取目标节点最新信息
	 * 
	 * @param targetNode
	 * @return
	 */
	public Node getNewestNode(Node targetNode);

	/**
	 * 寻找节点服务
	 * @param node
	 * @param clz
	 * @return
	 */
	public <T> T loolup(Node node, Class<T> clz);

	/**
	 * 基于Rpc Service注解注册
	 * 
	 * @param tagetOb
	 */
	public void register(Object tagetOb);

	/**
	 * 移除节点服务
	 * 
	 * @param commandName
	 */
	public void remove(String commandName);

}
