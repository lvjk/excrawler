package six.com.crawler.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
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
import six.com.crawler.utils.StringCheckUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午9:35:12
 */
@Component
public class StandardClusterManager implements ClusterManager, InitializingBean {

	final static Logger log = LoggerFactory.getLogger(StandardClusterManager.class);

	String NODE_INIT_PATH = "node_manager_init";

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private QQEmailClient emailClient;

	private String clusterName;

	private Node currentNode;

	private Map<String, Node> workerNodes = Maps.newConcurrentMap();

	private Set<NodeChangeWatcher> toMasterNodeWatchers = new HashSet<>();

	private Set<NodeChangeWatcher> missWorkerNodeWatchers = new HashSet<>();

	private CuratorFramework curatorFramework;

	private NettyRpcServer nettyRpcServer;

	private NettyRpcCilent nettyRpcCilent;

	public void afterPropertiesSet() {
		// 初始化当前节点信息
		clusterName = getConfigure().getConfig("cluster.name", null);
		log.info("init cluster'name:" + clusterName);
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
		DistributedLock writeLock = getWriteLock(NODE_INIT_PATH);
		try {
			writeLock.lock();
			/**
			 * 初始化 节点server和client 通信
			 */
			String currentHost = getCurrentNode().getHost();
			int currentPost = getCurrentNode().getTrafficPort();
			nettyRpcServer = new NettyRpcServer(currentHost, currentPost);
			nettyRpcCilent = new NettyRpcCilent();
			registerNodeService(ClusterManager.class, this);
			// 初始化注册当前节点
			register();
		} catch (Exception e) {
			log.error("init clusterManager err", e);
			exit(1);
		} finally {
			writeLock.unLock();
		}
	}

	private void register() throws Exception {
		boolean clusterEnable = getConfigure().getConfig("cluster.enable", false);
		log.info("init cluster'enable:" + clusterEnable);
		if (!clusterEnable) {
			unRegisterMaster();
			unRegisterWorker();
			byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
			String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(),
					getCurrentNode().getName());
			curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(masterNodePath,
					data);
			String workerNodePath = ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(),
					getCurrentNode().getName());
			curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(workerNodePath,
					data);
			getCurrentNode().setType(NodeType.SINGLE);
		} else {
			Node zkMasterNode = getMasterNodeFromRegister();
			if (null == zkMasterNode || zkMasterNode.equals(getCurrentNode())) {
				unRegisterMaster();
				byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
				curatorFramework.create()
						.creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZooKeeperPathUtils
								.getMasterNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName()),
								data);
				getCurrentNode().setType(NodeType.MASTER);
			} else {
				unRegisterWorker();
				byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
				String nodePath = ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(),
						getCurrentNode().getName());
				curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(nodePath,
						data);
				String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(),
						zkMasterNode.getName());
				curatorFramework.checkExists().usingWatcher(new Watcher() {
					@Override
					public void process(WatchedEvent event) {
						if (EventType.NodeDeleted == event.getType()) {
							log.info("missed master node");
							Node masterNode = getMasterNodeFromRegister();
							if (null == masterNode) {
								DistributedLock writeLock = getWriteLock(NODE_INIT_PATH);
								try {
									writeLock.lock();
									masterNode = getMasterNodeFromRegister();
									if (null == masterNode) {
										log.info("register own to master node");
										try {
											register();
											masterChange(getCurrentNode());
										} catch (Exception e) {
											log.error("register node:" + getCurrentNode().getName(), e);
										}
									} else {
										loolup(masterNode, ClusterManager.class).addWorkerNode(getCurrentNode());
										masterChange(masterNode);
									}
								} finally {
									writeLock.unLock();
								}
							} else {
								loolup(masterNode, ClusterManager.class).addWorkerNode(getCurrentNode());
								masterChange(masterNode);
							}
						}
					}
				}).forPath(masterNodePath);
				loolup(zkMasterNode, ClusterManager.class).addWorkerNode(getCurrentNode());
				getCurrentNode().setType(NodeType.WORKER);
			}
		}
	}

	@Override
	public String getNodeName() {
		return currentNode.getName();
	}

	@Override
	public Node getMasterNodeFromRegister() {
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
		return Lists.newArrayList(workerNodes.values());
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
		Objects.requireNonNull(targetNode, "the targetNode must be not null");
		Node newestNode = targetNode;
		try {
			ClusterManager findNodeManager = loolup(targetNode, ClusterManager.class);
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

	@Override
	public <T> T loolup(Node node, Class<T> clz) {
		return nettyRpcCilent.lookupService(node.getHost(), node.getTrafficPort(), clz);
	}

	/**
	 * 基于Rpc Service注解注册
	 * 
	 * @param tagetOb
	 */
	@Override
	public void registerNodeService(Class<?> protocol, Object tagetOb) {
		nettyRpcServer.register(protocol, tagetOb);
	}

	@RpcService()
	@Override
	public Node getCurrentNode() {
		currentNode.setCpu(MyOperatingSystemMXBean.getAvailableProcessors());
		currentNode.setMem(MyOperatingSystemMXBean.freeMemoryPRP());
		return currentNode;
	}

	@Override
	public void registerToMasterNodeWatcher(NodeChangeWatcher watcher) {
		toMasterNodeWatchers.add(watcher);
	}

	@Override
	public void registerMissWorkerNodeWatcher(NodeChangeWatcher watcher) {
		missWorkerNodeWatchers.add(watcher);
	}

	@RpcService()
	@Override
	public void addWorkerNode(Node workerNode) {
		String workNodesPath = ZooKeeperPathUtils.getWorkerNodePath(getClusterName(), workerNode.getName());
		try {
			curatorFramework.checkExists().usingWatcher(new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (EventType.NodeDeleted == event.getType()) {
						String workerNodeName = event.getPath();
						log.info("missed worker node:" + workerNodeName);
						removeWorkerNode(workerNodeName);
					}
				}
			}).forPath(workNodesPath);
			workerNodes.put(workerNode.getName(), workerNode);
		} catch (Exception e) {
			log.error("master node[" + getNodeName() + "] watch worker node[" + workerNode.getName() + "] failed", e);
		}

	}

	@Override
	public void removeWorkerNode(String workerNodeName) {
		if (null != workerNodes.remove(workerNodeName)) {
			for (NodeChangeWatcher watcher : missWorkerNodeWatchers) {
				watcher.onChange(workerNodeName);
			}
		}
	}

	@Override
	public void masterChange(Node master) {
		for (NodeChangeWatcher watcher : toMasterNodeWatchers) {
			watcher.onChange(master.getName());
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
	public String getClusterName() {
		return clusterName;
	}

	@PreDestroy
	public void destroy() {
		if (null != nettyRpcCilent) {
			nettyRpcCilent.destroy();
		}

		if (null != nettyRpcServer) {
			nettyRpcServer.destroy();
		}
		unRegister();
		if (null != curatorFramework) {
			curatorFramework.close();
		}
	}

	/**
	 * 移除注册信息
	 */
	public void unRegister() {
		unRegisterWorker();
		unRegisterMaster();
	}

	protected void unRegisterMaster() {
		String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(),
				getCurrentNode().getName());
		try {
			if (checkIsRegister(masterNodePath)) {
				curatorFramework.delete().forPath(masterNodePath);
			}
		} catch (Exception e) {
			log.error("unRegister master node:" + getCurrentNode().getName(), e);
		}
	}

	protected void unRegisterWorker() {
		String workerNodePath = ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(),
				getCurrentNode().getName());
		try {
			if (checkIsRegister(workerNodePath)) {
				curatorFramework.delete().forPath(workerNodePath);
			}
		} catch (Exception e) {
			log.error("unRegister worker node:" + getCurrentNode().getName(), e);
		}
	}

	public boolean checkIsRegister(String path) {
		boolean isRegister = false;
		try {
			isRegister = null != curatorFramework.checkExists().forPath(path);
		} catch (Exception e) {
			log.error("", e);
		}
		return isRegister;
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
