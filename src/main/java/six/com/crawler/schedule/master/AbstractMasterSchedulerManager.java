package six.com.crawler.schedule.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.DispatchType;
import six.com.crawler.schedule.worker.AbstractWorkerSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:45:58
 * 
 *       主节点调度管理抽象类
 */
public abstract class AbstractMasterSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(AbstractMasterSchedulerManager.class);

	@Autowired
	private AbstractWorkerSchedulerManager workerSchedulerManager;
	

	protected final void init() {
		doInit();
		getClusterManager().registerNodeService(AbstractMasterSchedulerManager.class,this);
	}

	protected abstract void doInit();

	@RpcService()
	public abstract void execute(DispatchType dispatchType, String jobName);
	/**
	 * 当job's worker结束后,由工作节点调度管理类调用,通知job's worker 结束工作
	 * 
	 * @param dispatchType
	 * @param jobName
	 */
	@RpcService()
	public abstract void endWorker(DispatchType dispatchType, String jobName);
	

	@RpcService()
	public abstract void finish(DispatchType dispatchType, String jobName);
	
	@RpcService()
	public abstract void stop(DispatchType dispatchType, String jobName);

	/**
	 * 当job's worker工作时从工作空间获取不到处理数据,由工作节点调度管理类调用,询问当前job's worker是否结束
	 * 
	 * @param dispatchType
	 * @param jobName
	 */
	@RpcService()
	public abstract void askEnd(DispatchType dispatchType, String jobName);
	
	
	public AbstractWorkerSchedulerManager getWorkerSchedulerManager() {
		return workerSchedulerManager;
	}

	public void setWorkerSchedulerManager(AbstractWorkerSchedulerManager workerSchedulerManager) {
		this.workerSchedulerManager = workerSchedulerManager;
	}

}
