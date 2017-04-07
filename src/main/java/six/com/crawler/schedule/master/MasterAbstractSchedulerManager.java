package six.com.crawler.schedule.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.NodeType;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:45:58
 */
public abstract class MasterAbstractSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterAbstractSchedulerManager.class);

	protected final void init() {

		doInit();

		/**
		 * 如果当前节点是master那么需要修复
		 */
		if (NodeType.MASTER == getNodeManager().getCurrentNode().getType()
				|| NodeType.MASTER_WORKER == getNodeManager().getCurrentNode().getType()) {
			try {
				stopAll();
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

	protected abstract void doInit();

	/**
	 * 开始执行 job's worker
	 * 
	 * @param jobName
	 * @param WorkName
	 */
	@RpcService(name = "startWorker")
	public abstract void startWorker(String jobName, String workerName);

	/**
	 * 结束执行job's worker
	 * 
	 * @param jobName
	 * @param WorkName
	 */
	@RpcService(name = "endWorker")
	public abstract void endWorker(String jobName, String workerName);

	public abstract void repair();

	/**
	 * 向调度器 调度job
	 * 
	 * @param job
	 */
	public abstract void scheduled(Job job);

	/**
	 * 取消调度
	 * 
	 * @param job
	 */
	public abstract void cancelScheduled(String jobChainName);

}
