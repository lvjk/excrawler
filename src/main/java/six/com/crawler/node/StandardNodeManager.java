package six.com.crawler.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.MyOperatingSystemMXBean;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.email.QQEmailClient;
import six.com.crawler.entity.Node;
import six.com.crawler.entity.NodeType;
import six.com.crawler.rpc.NettyRpcCilent;
import six.com.crawler.rpc.NettyRpcServer;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.rpc.protocol.RpcRequest;
import six.com.crawler.rpc.protocol.RpcResponse;
import six.com.crawler.utils.JavaSerializeUtils;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午9:35:12 
*/
@Component
public class StandardNodeManager implements NodeManager,InitializingBean{


	final static Logger log = LoggerFactory.getLogger(StandardNodeManager.class);

	public final int executeCommandRetryCount = 3;

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private QQEmailClient emailClient;

	// 当前节点
	private Node currentNode;

	private CuratorFramework zKClient;

	private NettyRpcServer nettyRpcServer;

	private NettyRpcCilent nettyRpcCilent;

	/**
	 * 分布式锁保证每个节点都是有序的初始化注册
	 */
	public void afterPropertiesSet() {
		// 初始化当前节点信息
		initCurrentNode();
		String lockKey = "cluster_manager_init";
		try {
			// 判断是否是单机模式
			if (getCurrentNode().getType() != NodeType.SINGLE) {
				redisManager.lock(lockKey);
				// 初始化zKClient
				initZKClient();
				// 初始化注册当前节点
				initRegisterNode();
			}
		} catch (Exception e) {
			log.error("init clusterManager err", e);
			System.exit(1);
		} finally {
			redisManager.unlock(lockKey);
		}
		/**
		 * 初始化 节点server和client 通信
		 */
		String localHost = getCurrentNode().getHost();
		int trafficPort = getCurrentNode().getTrafficPort();
		nettyRpcServer = new NettyRpcServer(localHost, trafficPort);
		nettyRpcCilent = new NettyRpcCilent();
		register("getCurrentNode", ob -> getCurrentNode());
	}

	protected void initZKClient() {
		String connectString = getConfigure().getConfig("zookeeper.host", null);
		// 检查 nodeName是否有注册过。如果注册过那么检查 ip 和端口是相同
		if (StringUtils.isBlank(connectString)) {
			log.error("the zookeeper's host is blank");
			System.exit(1);
		}
		zKClient = CuratorFrameworkFactory.newClient(connectString, new RetryPolicy() {
			@Override
			public boolean allowRetry(int arg0, long arg1, RetrySleeper arg2) {
				return false;
			}
		});
		zKClient.start();
		try {
			zKClient.blockUntilConnected();
		} catch (InterruptedException e) {
			log.error("connect zooKeeper[" + connectString + "] err", e);
			System.exit(1);
		}
		try {
			Stat stat = zKClient.checkExists().forPath(ZooKeeperPathUtils.getRootPath());
			if (null == stat) {
				zKClient.create().withMode(CreateMode.PERSISTENT).forPath(ZooKeeperPathUtils.getRootPath());
			}
			stat = zKClient.checkExists().forPath(ZooKeeperPathUtils.getMasterNodesPath());
			if (null == stat) {
				zKClient.create().withMode(CreateMode.PERSISTENT).forPath(ZooKeeperPathUtils.getMasterNodesPath());
			}

			stat = zKClient.checkExists().forPath(ZooKeeperPathUtils.getMasterStandbyNodesPath());
			if (null == stat) {
				zKClient.create().withMode(CreateMode.PERSISTENT)
						.forPath(ZooKeeperPathUtils.getMasterStandbyNodesPath());
			}
		} catch (Exception e) {
			log.error("init zooKeeper's persistent path err", e);
			System.exit(1);
		}
	}

	protected void initRegisterNode() {
		NodeRegisterEvent event = NodeRegisterEventFactory.createNodeRegisterEvent(currentNode);
		if (!event.register(this, zKClient)) {
			log.error("register node[" + currentNode.getName() + "] to zooKeeper failed");
			System.exit(1);
		}
	}

	private void initCurrentNode() {
		// 获取节点配置
		String host = getConfigure().getHost();
		int port = getConfigure().getPort();
		int trafficPort = getConfigure().getConfig("node.trafficPort", 8180);
		int runningWorkerMaxSize = getConfigure().getConfig("worker.running.max.size", 20);
		String nodeName = getConfigure().getConfig("node.name", null);
		// 检查节点名字是否设置
		if (StringUtils.isBlank(nodeName)) {
			log.error("don't set node's name ,please set it ,then to start");
			System.exit(1);
		}
		// 检查节点类型
		NodeType nodeType = getConfigure().getNodeType();
		currentNode = new Node();
		currentNode.setType(nodeType);
		currentNode.setName(nodeName);
		currentNode.setHost(host);
		currentNode.setPort(port);
		currentNode.setTrafficPort(trafficPort);
		currentNode.setRunningWorkerMaxSize(runningWorkerMaxSize);
		log.info("the node[" + nodeName + "] type:" + nodeType);
	}

	/**
	 * 获取主节点
	 * @return
	 */
	public Node getMasterNode() {
		Node masterNode = null;
		if (getCurrentNode().getType() != NodeType.SINGLE) {
			try {
				List<String> masterNodePaths = zKClient.getChildren().forPath(ZooKeeperPathUtils.getMasterNodesPath());
				if (null != masterNodePaths & masterNodePaths.size() == 1) {
					String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(masterNodePaths.get(0));
					byte[] data = zKClient.getData().forPath(masterNodePath);
					masterNode = JavaSerializeUtils.unSerialize(data, Node.class);
				}
			} catch (Exception e) {
				log.error("getMasterNode err", e);
			}
		} else {
			masterNode = getCurrentNode();
		}
		return masterNode;
	}

	/**
	 * 获取所有工作节点
	 * @return
	 */
	public List<Node> getWorkerNodes() {
		List<Node> allNodes = new ArrayList<>();
		if (getCurrentNode().getType() != NodeType.SINGLE) {
			try {
				String workerNodesPath = ZooKeeperPathUtils.getWorkerNodesPath();
				List<String> workerNodePaths = zKClient.getChildren().forPath(workerNodesPath);
				for (String workerNodePath : workerNodePaths) {
					byte[] data = zKClient.getData().forPath(workerNodesPath + "/" + workerNodePath);
					Node workerNode = JavaSerializeUtils.unSerialize(data, Node.class);
					allNodes.add(workerNode);
				}
			} catch (Exception e) {
				log.error("getWorkerNodes err", e);
			}
		} else {
			allNodes.add(currentNode);
		}
		return allNodes;
	}

	/**
	 * 通过节点名获取节点
	 * @param nodeName
	 * @return
	 */
	public Node getWorkerNode(String nodeName) {
		Node workerNode = null;
		if (getCurrentNode().getType() != NodeType.SINGLE) {
			try {
				byte[] data = zKClient.getData().forPath(ZooKeeperPathUtils.getWorkerNodePath(nodeName));
				workerNode = JavaSerializeUtils.unSerialize(data, Node.class);
			} catch (Exception e) {
				log.error("getWorkerNode err:"+nodeName, e);
			}
		} else {
			workerNode = currentNode;
		}
		return workerNode;
	}

	/**
	 * 获取空闲可用节点
	 * @param needFresNodes
	 * @return
	 */
	public List<Node> getFreeWorkerNodes(int needFresNodes) {
		List<Node> freeNodes = new ArrayList<>(needFresNodes);
		List<Node> allWorkerNodes = getWorkerNodes();
		for (Node workerNode : allWorkerNodes) {
			if (workerNode.getType() != NodeType.MASTER) {
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
		}
		return freeNodes;
	}

	/**
	 * 获取目标节点最新信息
	 * @param targetNode
	 * @return
	 */
	public Node getNewestNode(Node targetNode) {
		Node newestNode = (Node) execute(targetNode, "getCurrentNode", null);
		return newestNode;
	}

	/**
	 * 调用节点服务
	 * @param node
	 * @param commandName
	 * @param param
	 * @return
	 */
	public Object execute(Node node, String commandName,Map<String,Object> params) {
		String id =getRequestId(node, commandName);
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setId(id);
		rpcRequest.setCommand(commandName);
		rpcRequest.setCallHost(node.getHost());
		rpcRequest.setCallPort(node.getTrafficPort());
		rpcRequest.setOriginHost(getCurrentNode().getHost());
		rpcRequest.setParams(params);
		RpcResponse rpcResponse = nettyRpcCilent.execute(rpcRequest);
		return rpcResponse.getResult();
	}
	
	/**
	 * 生成请求Id
	 * @param node
	 * @param commandName
	 * @return
	 */
	private String getRequestId(Node node, String commandName){
		String id = getCurrentNode().getHost() + "@" + node.getHost() + ":" + node.getTrafficPort() + "/" + commandName
				+ "/" + System.currentTimeMillis();
		return id;
	}

	/**
	 * 注册节点服务
	 * @param rpcServiceName
	 * @param rpcService
	 */
	public void register(String rpcServiceName, RpcService rpcService) {
		nettyRpcServer.register(rpcServiceName, rpcService);
		log.info("register nodeCommand:" + rpcServiceName);
	}

	/**
	 * 移除节点服务
	 * @param commandName
	 */
	public void remove(String commandName) {
		nettyRpcServer.remove(commandName);
		log.info("remove nodeCommand:" + commandName);
	}

	public Node getCurrentNode() {
		currentNode.setCpu(MyOperatingSystemMXBean.getAvailableProcessors());
		currentNode.setMem(MyOperatingSystemMXBean.freeMemoryPRP());
		return currentNode;
	}
	
	@PreDestroy
	public void destroy() {
		if (null != nettyRpcCilent) {
			nettyRpcCilent.destroy();
		}

		if (null != nettyRpcServer) {
			nettyRpcServer.destroy();
		}
		if (null != zKClient) {
			zKClient.close();
		}
	}

	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public QQEmailClient getEmailClient() {
		return emailClient;
	}

	public void setEmailClient(QQEmailClient emailClient) {
		this.emailClient = emailClient;
	}

}
