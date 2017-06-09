package six.com.crawler.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.utils.JavaSerializeUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月8日 下午4:19:07
 */
public class ZkNodeRegister implements NodeRegister {

	final static Logger log = LoggerFactory.getLogger(ZkNodeRegister.class);

	private CuratorFramework curatorFramework;
	private String clusterName;

	public ZkNodeRegister(String clusterName, CuratorFramework curatorFramework) {
		this.clusterName = clusterName;
		this.curatorFramework = curatorFramework;
	}

	@Override
	public Node getMaster() {
		Node masterNode = null;
		try {
			List<String> masterNodePaths = curatorFramework.getChildren()
					.forPath(ZooKeeperPathUtils.getMasterNodesPath(clusterName));
			if (null != masterNodePaths & masterNodePaths.size() == 1) {
				String masterNodePath = ZooKeeperPathUtils.getMasterNodePath(clusterName, masterNodePaths.get(0));
				byte[] data = curatorFramework.getData().forPath(masterNodePath);
				masterNode = JavaSerializeUtils.unSerialize(data, Node.class);
			}
		} catch (Exception e) {
			log.error("getMasterNode err", e);
		}
		return masterNode;
	}

	@Override
	public List<Node> getWorkerNodes() {
		List<Node> allNodes = new ArrayList<>();
		try {
			String workerNodesPath = ZooKeeperPathUtils.getWorkerNodesPath(clusterName);
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
	public Node getWorkerNode(String nodeName) {
		Node workerNode = null;
		try {
			byte[] data = curatorFramework.getData()
					.forPath(ZooKeeperPathUtils.getWorkerNodePath(clusterName, nodeName));
			workerNode = JavaSerializeUtils.unSerialize(data, Node.class);
		} catch (Exception e) {
			log.error("getWorkerNode err:" + nodeName, e);
		}
		return workerNode;
	}

	@Override
	public void registerMaster(Node node) {
		String path = ZooKeeperPathUtils.getMasterNodePath(clusterName, node.getName());
		registerWorker(path, node);
	}

	@Override
	public void registerWorker(Node node) {
		String path = ZooKeeperPathUtils.getWorkerNodePath(clusterName, node.getName());
		registerWorker(path, node);
	}

	private void registerWorker(String path, Node node) {
		try {
			if (null == curatorFramework.checkExists().forPath(path)) {
				curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
			}
			byte[] data = JavaSerializeUtils.serialize(node);
			curatorFramework.setData().forPath(path, data);
		} catch (Exception e) {
			log.error("update register info", e);
		}
	}
}
