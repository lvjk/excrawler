package six.com.crawler.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.entity.Node;
import six.com.crawler.entity.NodeType;
import six.com.crawler.node.NodeManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 上午11:31:34
 */
@Component
public class MasterSchedulerManagerRepair implements InitializingBean {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManagerRepair.class);
	@Autowired
	private NodeManager clusterManager;

	@Autowired
	private MasterSchedulerManager masterSchedulerManager;

	public MasterSchedulerManager getMasterSchedulerManager() {
		return masterSchedulerManager;
	}

	public void setMasterSchedulerManager(MasterSchedulerManager masterSchedulerManager) {
		this.masterSchedulerManager = masterSchedulerManager;
	}

	public NodeManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(NodeManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Node currentNode = getClusterManager().getCurrentNode();
		if (NodeType.MASTER == currentNode.getType() || NodeType.MASTER_WORKER == currentNode.getType()) {
			try {
				masterSchedulerManager.stopAll();
			} catch (Exception e) {
				log.error("master node stop all err", e);
			}
			try {
				masterSchedulerManager.repair();
			} catch (Exception e) {
				log.error("master node repair err", e);
			}
		}
	}
}
