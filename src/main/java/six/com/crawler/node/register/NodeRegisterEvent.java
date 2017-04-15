package six.com.crawler.node.register;

import org.apache.curator.framework.CuratorFramework;

import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.node.Node;
import six.com.crawler.node.ClusterManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 上午11:51:40
 */
public abstract class NodeRegisterEvent implements Watcher {

	final static Logger log = LoggerFactory.getLogger(NodeRegisterEvent.class);

	private Node currentNode;

	NodeRegisterEvent(Node currentNode) {
		this.currentNode = currentNode;
	}

	public abstract boolean register(ClusterManager clusterManager, CuratorFramework zKClient);

	public abstract void unRegister(ClusterManager clusterManager, CuratorFramework zKClient);

	Node getCurrentNode() {
		return currentNode;
	}

}
