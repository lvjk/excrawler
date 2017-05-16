package six.com.crawler.schedule.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.node.NodeType;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.DispatchType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:45:58
 */
public abstract class AbstractMasterSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(AbstractMasterSchedulerManager.class);

	protected final void init() {
		doInit();
		/**
		 * 如果当前节点是master那么需要修复
		 */
		if (NodeType.MASTER == getNodeManager().getCurrentNode().getType()
				|| NodeType.MASTER_WORKER == getNodeManager().getCurrentNode().getType()) {
			try {
				stopAll(DispatchType.newDispatchTypeByManual());
			} catch (Exception e) {
				log.error("master node stop all err", e);
			}
			try {
				repair();
			} catch (Exception e) {
				log.error("master node repair err", e);
			}
		}

		getNodeManager().register(this);

	}

	protected static String getOperationJobLockPath(String jobName) {
		String path = "masterSchedulerManager_operation_" + jobName;
		return path;
	}

	protected abstract void doInit();

	@RpcService(name = "endWorker")
	public abstract void endWorker(DispatchType dispatchType, String jobName);
	
	
	@RpcService(name = "askEnd")
	public abstract void askEnd(DispatchType dispatchType, String jobName);

}
