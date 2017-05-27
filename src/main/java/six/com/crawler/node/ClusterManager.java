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

	String getClusterName();
	
	String getNodeName();

	Node getCurrentNode();

	Node getMasterNodeFromRegister();


	List<Node> getWorkerNodesFromLocal();
	
	List<Node> getWorkerNodesFromRegister();

	Node getWorkerNode(String nodeName);

	List<Node> getFreeWorkerNodes(int needFresNodes);

	Node getNewestNode(Node targetNode);

	void addWorkerNode(Node workerNode);
	
	void removeWorkerNode(String workerNodeName);

	void masterChange(Node master);

	/**
	 * 注册节点变化watcher
	 * 
	 * @param watcher
	 */
	void registerToMasterNodeWatcher(NodeChangeWatcher watcher);
	
	/**
	 * 注册节点变化watcher
	 * 
	 * @param watcher
	 */
	void registerMissWorkerNodeWatcher(NodeChangeWatcher watcher);

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
