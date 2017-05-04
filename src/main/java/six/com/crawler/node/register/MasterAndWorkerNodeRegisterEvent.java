package six.com.crawler.node.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.node.Node;
import six.com.crawler.node.ClusterManager;
import six.com.crawler.node.ZooKeeperPathUtils;
import six.com.crawler.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 下午12:19:52
 */
public class MasterAndWorkerNodeRegisterEvent extends NodeRegisterEvent {

	final static Logger log = LoggerFactory.getLogger(MasterAndWorkerNodeRegisterEvent.class);

	public MasterAndWorkerNodeRegisterEvent(Node currentNode) {
		super(currentNode);
	}

	@Override
	public boolean register(ClusterManager clusterManager, CuratorFramework zKClient) {
		try {
			Node masterNode = clusterManager.getMasterNode();
			if (null == masterNode || masterNode.equals(getCurrentNode())) {
				unRegister(clusterManager, zKClient);
				byte[] data = JavaSerializeUtils.serialize(getCurrentNode());
				zKClient.create()
						.creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZooKeeperPathUtils
								.getMasterNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName()),
								data);
				zKClient.create()
						.creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZooKeeperPathUtils
								.getWorkerNodePath(getCurrentNode().getClusterName(), getCurrentNode().getName()),
								data);
				return true;
			} else {
				log.info("there is a master node:" + masterNode.toString());
				return false;
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}

	@Override
	public void unRegister(ClusterManager clusterManager, CuratorFramework zKClient) {
		String masterNodePath = ZooKeeperPathUtils.getWorkerNodePath(getCurrentNode().getClusterName(),
				getCurrentNode().getName());
		String workerNodePath = ZooKeeperPathUtils.getMasterNodePath(getCurrentNode().getClusterName(),
				getCurrentNode().getName());
		try {
			if (checkIsRegister(zKClient, masterNodePath)) {
				zKClient.delete().forPath(masterNodePath);
			}
			if (checkIsRegister(zKClient, workerNodePath)) {
				zKClient.delete().forPath(workerNodePath);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void process(WatchedEvent arg0) {
		EventType type = arg0.getType();
		if (EventType.NodeDeleted == type || EventType.None == type) {
			log.error("missing master node");
		}
	}

}
