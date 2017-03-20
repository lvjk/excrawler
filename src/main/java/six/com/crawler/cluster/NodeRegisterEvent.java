package six.com.crawler.cluster;

import org.apache.curator.framework.CuratorFramework;

import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 上午11:51:40
 */
public abstract class NodeRegisterEvent implements Watcher {

	final static Logger log = LoggerFactory.getLogger(NodeRegisterEvent.class);
			
	private Node currentNode;
	

	NodeRegisterEvent(Node currentNode){
		this.currentNode=currentNode;
	}

	public abstract boolean doRegister(ClusterManager clusterManager, CuratorFramework zKClient);

	public boolean register(ClusterManager clusterManager, CuratorFramework zKClient) {
		return doRegister(clusterManager, zKClient);
	}
	
	Node getCurrentNode(){
		return currentNode;
	}

}
