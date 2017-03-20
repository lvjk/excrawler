package six.com.crawler.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.entity.Node;
import six.com.crawler.node.NodeManager;
import six.com.crawler.service.ClusterService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午9:33:55
 */

@Service
public class ClusterServiceImpl implements ClusterService {

	final static Logger LOG = LoggerFactory.getLogger(ClusterServiceImpl.class);

	@Autowired
	private NodeManager clusterManager;

	public Node getCurrentNode(){
		return clusterManager.getCurrentNode();
	}
	
	@Override
	public List<Node> getClusterInfo() {
		List<Node> allNodes=new ArrayList<>();
		Node masterNode=clusterManager.getMasterNode();
		if(null!=masterNode){
			allNodes.add(masterNode);
		}
		List<Node> workerNodes = clusterManager.getWorkerNodes();
		if(null!=workerNodes){
			for(Node workerNode:workerNodes){
				if(!allNodes.contains(workerNode)){
					allNodes.add(workerNode);
				}
			}
		}
		return allNodes;
	}


	public NodeManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(NodeManager clusterManager) {
		this.clusterManager = clusterManager;
	}

}
