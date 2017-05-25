package six.com.crawler.node;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 上午11:51:40
 */
public class NodeRegister {

	final static Logger log = LoggerFactory.getLogger(NodeRegister.class);

	private ClusterManager clusterManager;

	private CuratorFramework zKClient;

	private Node currentNode;

	NodeRegister(ClusterManager clusterManager, CuratorFramework zKClient, Node currentNode) {
		this.clusterManager = clusterManager;
		this.zKClient = zKClient;
		this.currentNode = currentNode;
	}

	/**
	 * 注册节点
	 * 
	 * @throws Exception
	 */
	public void register() throws Exception {
		if (!getClusterManager().getClusterEnable()) {
			unRegisterMaster();
			unRegisterWorker();
			byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
			String masterNodePath=ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName());
			getZKClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(masterNodePath,data);
			String workerNodePath=ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName());
			getZKClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(workerNodePath,data);
			getClusterManager().setCurrentNodeType(NodeType.SINGLE);
		} else {
			Node masterNode = getClusterManager().getMasterNode();
			if (null == masterNode || masterNode.equals(getCurrentNode())) {
				unRegisterMaster();
				byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
				getZKClient().create()
						.creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZooKeeperPathUtils
								.getMasterNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName()),
								data);
				String workNodesPath=ZooKeeperPathUtils.getWorkerNodesPath(getCurrentNode().getClusterName());
				getZKClient().checkExists().usingWatcher(new Watcher() {
					@Override
					public void process(WatchedEvent event) {
						if (EventType.NodeDeleted == event.getType()) {
							String workerNodeName=event.getPath();
							log.info("missed worker node:"+workerNodeName);
							//触发主节点丢失工作节点事件
						}
					}
				}).forPath(workNodesPath);
				getClusterManager().setCurrentNodeType(NodeType.MASTER);
			} else {
				unRegisterWorker();
				byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
				String nodePath = ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(),
						getCurrentNode().getName());
				getZKClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(nodePath, data);
				String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(),
						masterNode.getName());
				getZKClient().checkExists().usingWatcher(new Watcher() {
					@Override
					public void process(WatchedEvent event) {
						if (EventType.NodeDeleted == event.getType()) {
							log.info("missed master node");
							Node masterNode = getClusterManager().getMasterNode();
							if (null == masterNode) {
								DistributedLock writeLock = getClusterManager().getWriteLock(ClusterManager.INIT_PATH);
								try {
									writeLock.lock();
									masterNode = getClusterManager().getMasterNode();
									if (null == masterNode) {
										log.info("register own to master node");
										try {
											register();
										} catch (Exception e) {
											log.error("register node:" + getClusterManager().getCurrentNode().getName(),
													e);
										}
									}
								} finally {
									writeLock.unLock();
								}
							}
						}
					}
				}).forPath(masterNodePath);
				getClusterManager().setCurrentNodeType(NodeType.WORKER);
			}
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
		String masterNodePath=ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName());
		try {
			if (checkIsRegister(masterNodePath)) {
				getZKClient().delete().forPath(masterNodePath);
			}
		} catch (Exception e) {
			log.error("unRegister master node:" + getCurrentNode().getName(), e);
		}
	}

	protected void unRegisterWorker() {
		String workerNodePath = ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(),getCurrentNode().getName());
		try {
			if (checkIsRegister(workerNodePath)) {
				getZKClient().delete().forPath(workerNodePath);
			}
		} catch (Exception e) {
			log.error("unRegister worker node:" + getCurrentNode().getName(), e);
		}
	}

	public boolean checkIsRegister(String path) {
		boolean isRegister = false;
		try {
			isRegister = null != zKClient.checkExists().forPath(path);
		} catch (Exception e) {
			log.error("", e);
		}
		return isRegister;
	}

	ClusterManager getClusterManager() {
		return clusterManager;
	}

	CuratorFramework getZKClient() {
		return zKClient;
	}

	Node getCurrentNode() {
		return currentNode;
	}

}
