package six.com.crawler.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.NodeType;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:45:58
 */
public abstract class MasterAbstractSchedulerManager extends AbstractSchedulerManager{

	final static Logger log = LoggerFactory.getLogger(MasterAbstractSchedulerManager.class);
	
	protected final void init() {
		
		doInit();

		getNodeManager().register(ScheduledJobCommand.startWorker, params -> {
			String jobName =(String)params.get("jobName");
			String workerName =(String)params.get("workerName");
			MasterAbstractSchedulerManager.this.startWorker(jobName,workerName);
			return null;
		});

		getNodeManager().register(ScheduledJobCommand.endWorker, params -> {
			String jobName =(String)params.get("jobName");
			String workerName =(String)params.get("workerName");
			MasterAbstractSchedulerManager.this.endWorker(jobName,workerName);
			return null;

		});
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
	
	}

	protected abstract void doInit();

	/**
	 * 开始执行 job's worker
	 * 
	 * @param jobName
	 * @param WorkName
	 */
	public abstract void startWorker(String jobName,String workerName);

	/**
	 * 结束执行job's worker
	 * 
	 * @param jobName
	 * @param WorkName
	 */
	public abstract void endWorker(String jobName,String workerName);

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
