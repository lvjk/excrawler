package six.com.crawler.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import six.com.crawler.common.MyOperatingSystemMXBean;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.email.QQEmailClient;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.node.lock.DistributedReadLock;
import six.com.crawler.node.lock.DistributedWriteLock;
import six.com.crawler.rpc.AsyCallback;
import six.com.crawler.rpc.NettyRpcCilent;
import six.com.crawler.rpc.NettyRpcServer;
import six.com.crawler.rpc.RpcService;

import six.com.crawler.utils.JavaSerializeUtils;
import six.com.crawler.utils.ObjectCheckUtils;
import six.com.crawler.utils.StringCheckUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午9:35:12
 */
@Component
public class StandardClusterManager implements ClusterManager, InitializingBean {

	final static Logger log = LoggerFactory.getLogger(StandardClusterManager.class);

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private QQEmailClient emailClient;

	private String clusterName;

	private boolean clusterEnable;

	// 当前节点
	private Node currentNode;

	private CuratorFramework curatorFramework;

	private NettyRpcServer nettyRpcServer;

	private NettyRpcCilent nettyRpcCilent;

	private NodeRegister nodeRegisterEvent;

	private Set<NodeChangeWatcher> nodeTypeChangeWatchers = new HashSet<>();

	public void afterPropertiesSet() {
		// 初始化当前节点信息
		clusterName = getConfigure().getConfig("cluster.name", null);
		log.info("init cluster'name:" + clusterName);
		clusterEnable = getConfigure().getConfig("cluster.enable", false);
		log.info("init cluster'enable:" + clusterEnable);
		// 检查集群名字是否设置
		if (StringUtils.isBlank(clusterName)) {
			log.error("don't set cluster's name ,please set it ,then to start");
			exit(1);
		}
		String host = getConfigure().getHost();
		int port = getConfigure().getPort();
		int trafficPort = getConfigure().getConfig("node.trafficPort", 8180);
		int runningWorkerMaxSize = getConfigure().getConfig("worker.running.max.size", 20);
		String nodeName = getConfigure().getConfig("node.name", null);
		// 检查节点名字是否设置
		if (StringUtils.isBlank(nodeName)) {
			log.error("don't set node's name ,please set it ,then to start");
			exit(1);
		}
		currentNode = new Node();
		currentNode.setClusterName(clusterName);
		currentNode.setName(nodeName);
		currentNode.setHost(host);
		currentNode.setPort(port);
		currentNode.setTrafficPort(trafficPort);
		currentNode.setRunningWorkerMaxSize(runningWorkerMaxSize);
		log.info("init currentNode:" + currentNode.toString());
		// 初始化curatorFramework
		String connectString = getConfigure().getConfig("zookeeper.host", null);
		if (StringUtils.isBlank(connectString)) {
			log.error("the zookeeper's host is blank");
			exit(1);
		}
		// 因为redis 链接串是以;
		// 拼接的，例如172.18.84.44:2181;172.18.84.45:2181;172.18.84.45:2181
		connectString = StringUtils.replace(connectString, ";", ",");
		curatorFramework = new CuratorFrameworkHelper().newCuratorFramework(connectString, getClusterName(),
				new RetryPolicy() {
					@Override
					public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
						return false;
					}
				});
		if (null == curatorFramework) {
			log.error("init curatorFramework err");
			exit(1);
		}
		DistributedLock writeLock = getWriteLock(ClusterManager.INIT_PATH);
		try {
			writeLock.lock();
			/**
			 * 初始化 节点server和client 通信
			 */
			nettyRpcServer = new NettyRpcServer(getCurrentNode().getHost(), getCurrentNode().getTrafficPort());
			nettyRpcCilent = new NettyRpcCilent();
			registerNodeService(this);

			// 初始化注册当前节点
			nodeRegisterEvent = new NodeRegister(this, curatorFramework, currentNode);
			nodeRegisterEvent.register();
		} catch (Exception e) {
			log.error("init clusterManager err", e);
			exit(1);
		} finally {
			writeLock.unLock();
		}
	}

	/**
	 * 获取主节点
	 * 
	 * @return
	 */
	@Override
	public Node getMasterNode() {
		Node masterNode = null;
		try {
			List<String> masterNodePaths = curatorFramework.getChildren()
					.forPath(ZooKeeperPathUtils.getMasterNodesPath(getClusterName()));
			if (null != masterNodePaths & masterNodePaths.size() == 1) {
				String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(getClusterName(), masterNodePaths.get(0));
				byte[] data = curatorFramework.getData().forPath(masterNodePath);
				masterNode = JavaSerializeUtils.unSerialize(data, Node.class);
			}
		} catch (Exception e) {
			log.error("getMasterNode err", e);
		}
		return masterNode;
	}

	@Override
	public List<Node> getAllNodes() {
		return Collections.emptyList();
	}

	/**
	 * 获取所有工作节点
	 * 
	 * @return
	 */
	@Override
	public List<Node> getWorkerNodesFromRegister() {
		List<Node> allNodes = new ArrayList<>();
		try {
			String workerNodesPath = ZooKeeperPathUtils.getWorkerNodesPath(getClusterName());
			List<String> workerNodePaths = curatorFramework.getChildren().forPath(workerNodesPath);
			for (String workerNodePath : workerNodePaths) {
				byte[] data = curatorFramework.getData().forPath(workerNodesPath + "/" + workerNodePath);
				Node workerNode = JavaSerializeUtils.unSerialize(data, Node.class);
				allNodes.add(workerNode);
			}
		} catch (Exception e) {
			log.error("getWorkerNodes err", e);
		}
		return allNodes;
	}

	@Override
	public List<Node> getWorkerNodesFromLocal() {
		return null;
	}

	/**
	 * 通过节点名获取节点
	 * 
	 * @param nodeName
	 * @return
	 */
	@Override
	public Node getWorkerNode(String nodeName) {
		Node workerNode = null;
		try {
			byte[] data = curatorFramework.getData()
					.forPath(ZooKeeperPathUtils.getWorkerNodePath(getClusterName(), nodeName));
			workerNode = JavaSerializeUtils.unSerialize(data, Node.class);
		} catch (Exception e) {
			log.error("getWorkerNode err:" + nodeName, e);
		}
		return workerNode;
	}

	/**
	 * 获取空闲可用节点
	 * 
	 * @param needFresNodes
	 * @return
	 */
	@Override
	public List<Node> getFreeWorkerNodes(int needFresNodes) {
		List<Node> freeNodes = new ArrayList<>(needFresNodes);
		List<Node> allWorkerNodes = getWorkerNodesFromRegister();
		for (Node workerNode : allWorkerNodes) {
			Node newestNode = null;
			if (!getCurrentNode().equals(workerNode)) {
				newestNode = getNewestNode(workerNode);
			} else {
				newestNode = getCurrentNode();
			}
			if (newestNode.getRunningWorkerSize() < newestNode.getRunningWorkerMaxSize()) {
				freeNodes.add(workerNode);
				if (freeNodes.size() >= needFresNodes) {
					break;
				}
			}
		}
		return freeNodes;
	}

	@Override
	public Node getNewestNode(Node targetNode) {
		ObjectCheckUtils.checkNotNull(targetNode, "targetNode");
		Node newestNode = targetNode;
		try {
			ClusterManager findNodeManager = loolup(targetNode, ClusterManager.class, null);
			newestNode = findNodeManager.getCurrentNode();
		} catch (Exception e) {
			log.error("get newest node:" + targetNode.getName(), e);
		}
		return newestNode;
	}

	@Override
	public <T> T loolup(Node node, Class<T> clz, AsyCallback asyCallback) {
		return nettyRpcCilent.lookupService(node.getHost(), node.getTrafficPort(), clz, asyCallback);
	}

	/**
	 * 基于Rpc Service注解注册
	 * 
	 * @param tagetOb
	 */
	@Override
	public void registerNodeService(Object tagetOb) {
		nettyRpcServer.register(tagetOb);
	}

	/**
	 * 移除节点服务
	 * 
	 * @param commandName
	 */
	@Override
	public void remove(String commandName) {
		nettyRpcServer.remove(commandName);
		log.info("remove nodeCommand:" + commandName);
	}

	@RpcService(name = "getCurrentNode")
	@Override
	public Node getCurrentNode() {
		currentNode.setCpu(MyOperatingSystemMXBean.getAvailableProcessors());
		currentNode.setMem(MyOperatingSystemMXBean.freeMemoryPRP());
		return currentNode;
	}

	@Override
	public void setCurrentNodeType(NodeType type) {
		if (type != currentNode.getType()) {
			currentNode.setType(type);
			if (NodeType.MASTER == type) {
				for (NodeChangeWatcher watcher : nodeTypeChangeWatchers) {
					watcher.onChange(NodeChangeEvent.TO_MASTER);
				}
			}
		}
	}

	@Override
	public void registerNodeChangeWatcher(NodeChangeWatcher watcher) {
		nodeTypeChangeWatchers.add(watcher);
	}

	@Override
	public void clearLock() {
		String path = ZooKeeperPathUtils.getDistributedLocksPath(getClusterName());
		try {
			List<String> childrens = curatorFramework.getChildren().forPath(path);
			if (null != childrens) {
				for (String children : childrens) {
					children = ZooKeeperPathUtils.getDistributedLockPath(getClusterName(), children);
					curatorFramework.delete().forPath(children);
				}
			}
		} catch (Exception e) {
			log.error("clearLock getChildren", e);
		}
	}

	@Override
	public DistributedLock getReadLock(String path) {
		StringCheckUtils.checkStrBlank(path, "path must be not blank");
		path = ZooKeeperPathUtils.getDistributedLockPath(getClusterName(), path);
		InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(curatorFramework, path);
		DistributedLock readLock = new DistributedReadLock(path, interProcessReadWriteLock);
		return readLock;
	}

	@Override
	public DistributedLock getWriteLock(String path) {
		StringCheckUtils.checkStrBlank(path, "path must be not blank");
		path = ZooKeeperPathUtils.getDistributedLockPath(getClusterName(), path);
		InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(curatorFramework, path);
		DistributedLock writeLock = new DistributedWriteLock(path, interProcessReadWriteLock);
		return writeLock;
	}

	@Override
	public boolean getClusterEnable() {
		return clusterEnable;
	}

	@Override
	public String getClusterName() {
		return clusterName;
	}

	@PreDestroy
	public void destroy() {

		if (null != nodeRegisterEvent) {
			nodeRegisterEvent.unRegister();
		}

		if (null != nettyRpcCilent) {
			nettyRpcCilent.destroy();
		}

		if (null != nettyRpcServer) {
			nettyRpcServer.destroy();
		}
		if (null != curatorFramework) {
			curatorFramework.close();
		}
	}

	private void exit(int status) {
		destroy();
		System.exit(status);
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public CuratorFramework getCuratorFramework() {
		return curatorFramework;
	}

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public QQEmailClient getEmailClient() {
		return emailClient;
	}

	public void setEmailClient(QQEmailClient emailClient) {
		this.emailClient = emailClient;
	}

}
