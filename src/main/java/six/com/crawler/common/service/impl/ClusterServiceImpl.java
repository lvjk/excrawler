package six.com.crawler.common.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.cluster.ClusterManager;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.service.ClusterService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午9:33:55
 */

@Service
public class ClusterServiceImpl implements ClusterService {

	final static Logger LOG = LoggerFactory.getLogger(ClusterServiceImpl.class);

	@Autowired
	private ClusterManager clusterManager;

	@Override
	public List<Node> getClusterInfo() {
		List<Node> allNodes = clusterManager.getAllNodes();
		allNodes.add(0, clusterManager.getMasterNode());
		return allNodes;
	}

	public Node getNode(String nodeName) {
		return clusterManager.getNode(nodeName);
	}

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

}
