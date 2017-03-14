package six.com.crawler.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.configure.SpiderConfigure;
import six.com.crawler.common.email.QQEmailClient;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.NodeType;
import six.com.crawler.common.http.HttpClient;
import six.com.crawler.common.utils.ThreadUtils;
import six.com.crawler.schedule.Constants;
import six.com.crawler.schedule.MasterAbstractSchedulerManager;
import six.com.crawler.schedule.RegisterCenter;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 下午1:41:16
 */
@Component
public class ClusterManager implements InitializingBean {

	final static Logger log = LoggerFactory.getLogger(ClusterManager.class);

	private final String REGISTER_NODE_LOCK = "redis_register_node";

	public final int executeCommandRetryCount = 3;

	@Autowired
	private SpiderConfigure configure;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private MasterAbstractSchedulerManager masterAbstractSchedulerManager;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private RegisterCenter registerCenter;

	@Autowired
	private QQEmailClient emailClient;

	// 当前节点
	private Node currentNode;

	// 节点心跳线程
	private Thread heartbeatThread;

	public void afterPropertiesSet() {
		// 初始化当前节点信息
		initCurrentNode();
		// 初始化复位节点
		initResetMasterNode();
		// 初始化注册当前节点
		initRegisterNode();
		// 初始节点心跳注册
		initHeatbeatToMaster();
	}

	private void initCurrentNode() {
		// 获取节点配置
		String host = getConfigure().getHost();
		int port = getConfigure().getPort();
		int runningJobMaxSize = getConfigure().getConfig("worker.running.max.size", 20);
		String nodeName = getConfigure().getConfig("node.name", null);
		// 检查节点名字是否设置
		if (StringUtils.isBlank(nodeName)) {
			log.error("don't set node's name ,please set it ,then to start");
			System.exit(1);
		}
		Node existedNode = getRegisterCenter().getNode(nodeName);
		// 检查 nodeName是否有注册过。如果注册过那么检查 ip 和端口是相同
		if (null != existedNode && !(existedNode.getHost().equals(host) && existedNode.getPort() == port)) {
			log.error("the " + nodeName + "[" + host + ":" + port + "]has been executed");
			System.exit(1);
		}
		// 检查节点类型
		NodeType nodeType = getConfigure().getNodeType();
		currentNode = new Node();
		currentNode.setType(nodeType);
		currentNode.setName(nodeName);
		currentNode.setHost(host);
		currentNode.setPort(port);
		currentNode.setRunningJobMaxSize(runningJobMaxSize);
	}

	/**
	 * 因为redis注册中心节点数据的有效期是 Constants.REDIS_REGISTER_CENTER_HEARTBEAT 秒
	 * 所以每次心跳更新时间应该要小于 有效期 所以
	 * sleeptime=Constants.REDIS_REGISTER_CENTER_HEARTBEAT*1000-1000;
	 */
	private void initHeatbeatToMaster() {
		// 如果是工作节点，那么初始化 跟master节点的心跳
		if (getCurrentNode().getType() == NodeType.WORKER || getCurrentNode().getType() == NodeType.MASTER_WORKER) {
			// 初始化 心跳线程
			heartbeatThread = new Thread(() -> {
				log.info("running heartbeat thread");
				long sleepTime = Constants.REDIS_REGISTER_CENTER_HEARTBEAT * 1000 - 1000;
				while (true) {
					try {
						Node masterNode = getMasterNode();
						if (null == masterNode) {
							emailClient.sendMailToAdmin("missing master node", "missing master node");
							log.error("missing master node");
						}
						getRegisterCenter().registerNode(currentNode, Constants.REDIS_REGISTER_CENTER_HEARTBEAT);
						ThreadUtils.sleep(sleepTime);
						log.debug("the node[" + currentNode.getName() + "] heartbeat");
					} catch (Exception e) {
						log.error("the node[" + currentNode.getName() + "] heartbeat err", e);
					}
				}
			}, "Heartbeat-Thread");
			heartbeatThread.setDaemon(true);
			// 启动心跳线程
			heartbeatThread.start();
		}
	}

	public Node getMasterNode() {
		return getRegisterCenter().getMasterNode();
	}

	/**
	 * 节点注册
	 * 
	 * @param node
	 */
	private void initResetMasterNode() {
		if (NodeType.MASTER == currentNode.getType() || NodeType.MASTER_WORKER == currentNode.getType()) {
			try {
				masterAbstractSchedulerManager.stopAll();
				getRegisterCenter().repair();
			} catch (Exception e) {
				log.error("init reset master node", e);
				getEmailClient().sendMailToAdmin("init reset master node", "init reset master node");
				System.exit(1);
			}
		}
	}

	/**
	 * 节点注册
	 * 
	 * @param node
	 */
	private void initRegisterNode() {

		log.info("the node[" + currentNode.getName() + "] type:" + getConfigure().getNodeType());
		try {
			getRedisManager().lock(REGISTER_NODE_LOCK);
			Node masterNode = getMasterNode();
			if (NodeType.MASTER == currentNode.getType()) {
				if (null != masterNode && !masterNode.equals(currentNode)) {
					log.error("there are many masternode[" + currentNode.getName() + "," + masterNode.getName() + "]");
					System.exit(1);
				}
				getRegisterCenter().registerMasterNode(currentNode);
			} else if (NodeType.MASTER_STANDBY == currentNode.getType()) {

			} // 如果是主节点备份节点或者工作节点 ，
			else if (NodeType.WORKER == currentNode.getType()) {
				// 检查主节点是否启动
				if (null == masterNode) {
					log.error("please first start the masterNode");
					System.exit(1);
				}
				getRegisterCenter().registerNode(currentNode, Constants.REDIS_REGISTER_CENTER_HEARTBEAT);
			} // 如果是主节点
				// 如果是主节点兼工作节点 ，
			else if (NodeType.MASTER_WORKER == currentNode.getType()) {
				if (null != masterNode && !masterNode.equals(currentNode)) {
					log.error("there are many masternode[" + currentNode.getName() + "," + masterNode.getName() + "]");
					System.exit(1);
				}
				getRegisterCenter().registerMasterNode(currentNode);
				getRegisterCenter().registerNode(currentNode, Constants.REDIS_REGISTER_CENTER_HEARTBEAT);
			} else {
				log.error("unkown node type:" + currentNode.getType());
				System.exit(1);
			}
		} catch (Exception e) {
			log.error("registerNode err", e);
			System.exit(1);
		} finally {
			getRedisManager().unlock(REGISTER_NODE_LOCK);
		}
	}

	public List<Node> getAllNodes() {
		List<Node> allNodes = registerCenter.getNodes();
		return allNodes;
	}

	public Node getNode(String nodeName) {
		Node node = registerCenter.getNode(nodeName);
		return node;
	}

	public List<Node> getFreeNodes(int needFresNodes) {
		List<Node> freeNodes = new ArrayList<>();
		List<Node> allNodes = registerCenter.getNodes();
		for (Node node : allNodes) {
			if (isValid(node)) {
				freeNodes.add(node);
				if (freeNodes.size() >= needFresNodes) {
					break;
				}
			}
		}
		return freeNodes;
	}

	private boolean isValid(Node node) {
		if (node.getType() != NodeType.MASTER && node.getRunningJobSize() < node.getRunningJobMaxSize()) {
			return true;
		}
		return false;
	}


	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public MasterAbstractSchedulerManager getMasterAbstractSchedulerManager() {
		return masterAbstractSchedulerManager;
	}

	public void setMasterAbstractSchedulerManager(MasterAbstractSchedulerManager masterAbstractSchedulerManager) {
		this.masterAbstractSchedulerManager = masterAbstractSchedulerManager;
	}

	public Node getCurrentNode() {
		return currentNode;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public RegisterCenter getRegisterCenter() {
		return registerCenter;
	}

	public void setRegisterCenter(RegisterCenter registerCenter) {
		this.registerCenter = registerCenter;
	}

	public QQEmailClient getEmailClient() {
		return emailClient;
	}

	public void setEmailClient(QQEmailClient emailClient) {
		this.emailClient = emailClient;
	}

}
