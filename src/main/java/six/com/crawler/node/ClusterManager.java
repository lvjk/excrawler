package six.com.crawler.node;

import java.util.List;

import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.rpc.AsyCallback;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 下午1:41:16 集群节点管理类 所有集群服务最基本的依赖
 * 
 *       集群节点管理接口:提供基本的集群和节点的操作
 */

public interface ClusterManager {

	String INIT_PATH = "node_manager_init";

	String getClusterName();

	boolean getClusterEnable();

	/**
	 * 获取当前节点
	 * 
	 * @return 当前节点
	 */
	Node getCurrentNode();

	/**
	 * 设置节点类型
	 * 
	 * @param type
	 */
	void setCurrentNodeType(NodeType type);

	/**
	 * 注册节点变化watcher
	 * 
	 * @param watcher
	 */
	void registerNodeChangeWatcher(NodeChangeWatcher watcher);

	/**
	 * 获取主节点
	 * 
	 * @return 主节点
	 */
	Node getMasterNode();

	/**
	 * 获取所有节点
	 * 
	 * @return
	 */
	List<Node> getAllNodes();

	/**
	 * 获取所有工作节点
	 * 
	 * @return
	 */
	List<Node> getWorkerNodesFromRegister();

	/**
	 * 获取所有工作节点
	 * 
	 * @return
	 */
	List<Node> getWorkerNodesFromLocal();

	/**
	 * 通过节点名获取节点
	 * 
	 * @param nodeName
	 * @return
	 */
	Node getWorkerNode(String nodeName);

	/**
	 * 获取空闲可用节点
	 * 
	 * @param needFresNodes
	 * @return
	 */
	List<Node> getFreeWorkerNodes(int needFresNodes);

	/**
	 * 获取目标节点最新信息
	 * 
	 * @param targetNode
	 * @return
	 */
	Node getNewestNode(Node targetNode);

	/**
	 * 寻找节点服务
	 * 
	 * @param node
	 * @param clz
	 * @return
	 */
	<T> T loolup(Node node, Class<T> clz, AsyCallback asyCallback);
	
	
	/**
	 * 寻找节点服务
	 * 
	 * @param node
	 * @param clz
	 * @return
	 */
	<T> T loolup(Node node, Class<T> clz);

	/**
	 * 注册节点Rpc Service
	 * 
	 * @param tagetOb
	 */
	void registerNodeService(Object tagetOb);

	/**
	 * 移除节点服务
	 * 
	 * @param commandName
	 */
	void remove(String commandName);

	void clearLock();
	
	
	void missWorkerNode(String workerNodeName);
	
	void toMasterNode();

	/**
	 * 根据path 获取一个 分布式读锁
	 * 
	 * @param path
	 * @return
	 */
	DistributedLock getReadLock(String path);

	/**
	 * 根据path获取一个分布式写锁
	 * 
	 * @param path
	 * @return
	 */
	DistributedLock getWriteLock(String path);

}
