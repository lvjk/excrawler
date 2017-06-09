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

	/**
	 * 获取当前集群名
	 * @return
	 */
	String getClusterName();
	
	/**
	 * 获取当前节点名
	 * @return
	 */
	String getNodeName();
	
	/**
	 * 获取当前节点
	 * @return
	 */
	Node getCurrentNode();
	
	/**
	 * 获取当前集群主节点
	 * @return
	 */
	Node getMaster();
	
	/**
	 * 获取所有工作节点
	 * @return
	 */
	List<Node> getWorkerNodes();
	
	/**
	 * 获取指定工作节点
	 * @param nodeName
	 * @return
	 */
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
	void registerNodeService(Class<?> protocol,Object tagetOb);

	/**
	 * 根据path 获取一个 分布式读锁
	 * 
	 * @param path
	 * @return
	 */
	DistributedLock getDistributedLock(String path);


}
