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
import org.apache.curator.shaded.com.google.common.collect.Maps;
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
import six.com.crawler.node.lock.LocalDistributedLock;
import six.com.crawler.node.lock.ZkDistributedLock;
import six.com.crawler.rpc.AsyCallback;
import six.com.crawler.rpc.NettyRpcCilent;
import six.com.crawler.rpc.NettyRpcServer;
import six.com.crawler.rpc.RpcService;

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

	private boolean clusterEnable;

	private String clusterName;

	private Node currentNode;

	private Node localMasterNode;

	private Map<String, Node> localWorkerNodes = Maps.newConcurrentMap();

	private Set<NodeChangeWatcher> toMasterNodeWatchers = new HashSet<>();

	private Set<NodeChangeWatcher> missWorkerNodeWatchers = new HashSet<>();

	private NodeRegister nodeRegister;

	private CuratorFramework curatorFramework;

	private NettyRpcServer nettyRpcServer;

	private NettyRpcCilent nettyRpcCilent;

	private Thread nodeKeepliveThread;

	private long nodeKeepliveInterval;

	private long retryGetNodeInterval;

	private long retryGetNodeCount;

	public void afterPropertiesSet() {
		// 初始化当前节点信息
		clusterName = getConfigure().getConfig("cluster.name", null);
		log.info("init cluster'name:" + clusterName);
		// 检查集群名字是否设置
		if (StringUtils.isBlank(clusterName)) {
			log.error("don't set cluster's name ,please set it ,then to start");
			exit(1);
		}
		nodeKeepliveInterval = getConfigure().getConfig("node.keeplive.interval", 3000);

		retryGetNodeInterval = getConfigure().getConfig("node.retry.get.interval", 4000);
		retryGetNodeCount = getConfigure().getConfig("node.retry.get.count", 3);

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

		/**
		 * 初始化 节点server和client 通信
		 */
		String currentHost = getCurrentNode().getHost();
		int currentPost = getCurrentNode().getTrafficPort();
		nettyRpcServer = new NettyRpcServer(currentHost, currentPost);
		nettyRpcCilent = new NettyRpcCilent();
		registerNodeService(ClusterManager.class, this);

		clusterEnable = getConfigure().getConfig("cluster.enable", false);
		log.info("init cluster'enable:" + clusterEnable);
		if (clusterEnable) {
			// 初始化curatorFramework
			String connectString = getConfigure().getConfig("zookeeper.host", null);
			if (StringUtils.isBlank(connectString)) {
				log.error("the zookeeper's host is blank");
				exit(1);
			}
			connectString = StringUtils.replace(connectString, ";", ",");
			curatorFramework = CuratorFrameworkHelper.newCuratorFramework(connectString, clusterName,
					new RetryPolicy() {
						@Override
						public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
							return false;
						}
					});
			nodeRegister = new ZkNodeRegister(clusterName, curatorFramework);
			DistributedLock writeLock = getDistributedLock(NODE_INIT_PATH);
			try {
				
				writeLock.lock();
				electionMaster();
				register();
				nodeKeepliveThread = new Thread(() -> {
					while (true) {
						try {
							if (NodeType.MASTER == getCurrentNode().getType()) {
								nodeRegister.registerMaster(getCurrentNode());
							} else {
								nodeRegister.registerWorker(getCurrentNode());
							}
							Thread.sleep(nodeKeepliveInterval);
						} catch (Exception e) {
						}
					}
				}, "node-keeplive-thread");
				nodeKeepliveThread.setDaemon(true);
				nodeKeepliveThread.start();
			} catch (Exception e) {
				log.error("init clusterManager err", e);
				exit(1);
			} finally {
				writeLock.unLock();
			}
		} else {
			getCurrentNode().setType(NodeType.SINGLE);
			nodeRegister = new LocalNodeRegister();
			nodeRegister.registerMaster(getCurrentNode());
			nodeRegister.registerWorker(getCurrentNode());
		}
	}

	/**
	 * 基于zookeeper 抢占式选举主节点
	 * 
	 * @return
	 */
	private Node electionMaster() {
		Node electionMaster = null;
		Node zkMasterNode = nodeRegister.getMaster();
		if (null == zkMasterNode || zkMasterNode.equals(getCurrentNode())) {
			getCurrentNode().setType(NodeType.MASTER);
			electionMaster = getCurrentNode();
		} else {
			getCurrentNode().setType(NodeType.WORKER);
			electionMaster = zkMasterNode;
		}
		this.localMasterNode = electionMaster;
		return electionMaster;
	}

	/**
	 * 这里存在当集群正常启动后，出现网络异常时节点与zookeeper链接异常后，节点注册的信息会被移除，当
	 * 网络恢复后需要有一个机制保障节点能够从新注册进zookeeper
	 * 
	 * @throws Exception
	 */
	private void register() throws Exception {
		if (NodeType.MASTER == getCurrentNode().getType()) {
			nodeRegister.registerMaster(getCurrentNode());
		} else {
			Node zkMasterNode = nodeRegister.getMaster();
			nodeRegister.registerWorker(getCurrentNode());
			loolup(zkMasterNode, ClusterManager.class).addWorkerNode(getCurrentNode());
			String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(clusterName, zkMasterNode.getName());
			watcherMaster(masterNodePath, null);
		}
	}

	@Override
	public String getClusterName() {
		return clusterName;
	}

	@Override
	public String getNodeName() {
		return currentNode.getName();
	}

	@Override
	public Node getMaster() {
		Node masterNode = null;
		int retryCount = 0;
		do {
			masterNode = nodeRegister.getMaster();
			if (null != localMasterNode && null == masterNode && retryCount++ < retryGetNodeCount) {
				try {
					Thread.sleep(retryGetNodeInterval);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		} while (true);
		return masterNode;
	}

	@Override
	public Node getWorkerNode(String nodeName) {
		Node workerNode = null;
		int retryCount = 0;
		do {
			workerNode = nodeRegister.getWorkerNode(nodeName);
			if (localWorkerNodes.containsKey(nodeName) && null == workerNode && retryCount++ < retryGetNodeCount) {
				try {
					Thread.sleep(retryGetNodeInterval);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		} while (true);
		return workerNode;
	}

	@Override
	public List<Node> getWorkerNodes() {
		List<Node> allNodes = null;
		int retryCount = 0;
		do {
			allNodes = nodeRegister.getWorkerNodes();
			if ((localWorkerNodes.size() > 0 && (null == allNodes || allNodes.isEmpty()))
					&& retryCount++ < retryGetNodeCount) {
				try {
					Thread.sleep(retryGetNodeInterval);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		} while (true);
		return allNodes;
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
		List<Node> allWorkerNodes = getWorkerNodes();
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

	@RpcService
	@Override
	public void addWorkerNode(Node workerNode) {
		String workerNodePath = ZooKeeperPathUtils.getWorkerNodePath(getClusterName(), workerNode.getName());
		watcherWorkerNode(workerNodePath, null);
		localWorkerNodes.put(workerNode.getName(), workerNode);

	}

	private void watcherMaster(String masterNodePath, Watcher watcher) throws Exception {
		curatorFramework.checkExists().usingWatcher(null != watcher ? watcher : new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (EventType.NodeDeleted == event.getType()) {
					log.info("missed master node");
					Node masterNode = null;
					DistributedLock writeLock = getDistributedLock(NODE_INIT_PATH);
					try {
						writeLock.lock();
						masterNode = electionMaster();
						if (null == masterNode) {
							throw new RuntimeException("did not election master");
						}
						if (getCurrentNode().equals(masterNode)) {
							try {
								unRegisterWorker();
								register();
								masterChange(masterNode);
							} catch (Exception e) {
								log.error("register node:" + getCurrentNode().getName(), e);
							}
						} else {
							loolup(masterNode, ClusterManager.class).addWorkerNode(getCurrentNode());
							String newMasterNodePath = ZooKeeperPathUtils.getMasterNodePath(clusterName,
									masterNode.getName());
							try {
								watcherMaster(newMasterNodePath, this);
							} catch (Exception e) {
								log.error("worker node[" + getNodeName() + "] watch master node[" + newMasterNodePath
										+ "] failed", e);
							}
						}
					} finally {
						writeLock.unLock();
					}
				} else {
					try {
						watcherMaster(event.getPath(), this);
					} catch (Exception e) {
						log.error(
								"worker node[" + getNodeName() + "] watch master node[" + event.getPath() + "] failed",
								e);
					}
				}
			}
		}).forPath(masterNodePath);
	}

	/**
	 * 监控工作节点
	 * 
	 * @param workerNodePath
	 */
	private void watcherWorkerNode(String workerNodePath, Watcher watcher) {
		try {
			curatorFramework.checkExists().usingWatcher(null != watcher ? watcher : new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (EventType.NodeDeleted == event.getType()) {
						String workerNodeName = ZooKeeperPathUtils.getWorkerNodeName(getClusterName(), event.getPath());
						log.info("missed worker node:" + workerNodeName);
						removeWorkerNode(workerNodeName);
					} else {
						try {
							watcherWorkerNode(event.getPath(), this);
						} catch (Exception e) {
							log.error("master node[" + getNodeName() + "] watch worker node[" + event.getPath()
									+ "] failed", e);
						}
					}
				}
			}).forPath(workerNodePath);
		} catch (Exception e) {
			log.error("master node[" + getNodeName() + "] watch worker node[" + workerNodePath + "] failed", e);
		}
	}

	@Override
	public void removeWorkerNode(String workerNodeName) {
		if (null != localWorkerNodes.remove(workerNodeName)) {
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
	public DistributedLock getDistributedLock(String path) {
		StringCheckUtils.checkStrBlank(path, "path must be not blank");
		path = ZooKeeperPathUtils.getDistributedLockPath(getClusterName(), path);
		DistributedLock writeLock = null;
		if (clusterEnable) {
			InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(curatorFramework, path);
			writeLock = new ZkDistributedLock(path, interProcessReadWriteLock);
		} else {
			writeLock = new LocalDistributedLock(path);
		}
		return writeLock;
	}

	/**
	 * 移除注册信息
	 */
	public void unRegister() {
		String path = null;
		if (NodeType.MASTER == getCurrentNode().getType()) {
			path = ZooKeeperPathUtils.getMasterNodePath(getClusterName(), getNodeName());
		} else {
			path = ZooKeeperPathUtils.getWorkerNodePath(getClusterName(), getNodeName());
		}
		try {
			if (checkIsRegister(path)) {
				curatorFramework.delete().forPath(path);
			}
		} catch (Exception e) {
			log.error("unRegister node:" + getCurrentNode().getName(), e);
		}
	}

	private void unRegisterWorker() {
		String path = ZooKeeperPathUtils.getWorkerNodePath(getClusterName(), getNodeName());
		try {
			if (checkIsRegister(path)) {
				curatorFramework.delete().forPath(path);
			}
		} catch (Exception e) {
			log.error("unRegister node:" + getCurrentNode().getName(), e);
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
