package six.com.crawler.admin.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.admin.service.ClusterManagerService;
import six.com.crawler.node.Node;
import six.com.crawler.node.ClusterManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午9:33:55
 */

@Service
public class ClusterManagerServiceImpl implements ClusterManagerService {

	final static Logger LOG = LoggerFactory.getLogger(ClusterManagerServiceImpl.class);

	@Autowired
	private ClusterManager clusterManager;

	public Node getCurrentNode() {
		return clusterManager.getCurrentNode();
	}

	@Override
	public List<Node> getClusterInfo() {
		List<Node> allNodes = new ArrayList<>();
		Node masterNode = clusterManager.getMasterNode();
		if (null != masterNode) {
			Node newestNode = clusterManager.getNewestNode(masterNode);
			allNodes.add(newestNode);
		}
		List<Node> workerNodes = clusterManager.getWorkerNodesFromRegister();
		if (null != workerNodes) {
			for (Node workerNode : workerNodes) {
				if (!allNodes.contains(workerNode)) {
					Node newestNode = clusterManager.getNewestNode(workerNode);
					allNodes.add(newestNode);
				}
			}
		}
		return allNodes;
	}

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

}
