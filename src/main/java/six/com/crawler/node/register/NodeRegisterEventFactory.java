package six.com.crawler.node.register;

import six.com.crawler.node.Node;
import six.com.crawler.node.NodeType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 下午12:16:39
 */
public class NodeRegisterEventFactory {

	public static NodeRegisterEvent createNodeRegisterEvent(Node node) {
		NodeRegisterEvent nodeRegisterEvent = null;
		if (null != node) {
			if (NodeType.MASTER == node.getType()) {
				nodeRegisterEvent = new MasterNodeRegisterEvent(node);
			} else if (NodeType.MASTER_STANDBY == node.getType()) {
				nodeRegisterEvent = new MasterStandbyNodeRegisterEvent(node);
			} else if (NodeType.WORKER == node.getType()) {
				nodeRegisterEvent = new WorkerNodeRegisterEvent(node);
			} else if (NodeType.MASTER_WORKER == node.getType()) {
				nodeRegisterEvent = new MasterAndWorkerNodeRegisterEvent(node);
			} else {
				nodeRegisterEvent = new WorkerNodeRegisterEvent(node);
			}
		}
		return nodeRegisterEvent;
	}
}
