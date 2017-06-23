package six.com.crawler.schedule.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.com.crawler.entity.Job;
import six.com.crawler.rpc.AsyCallback;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.TriggerType;
import six.com.crawler.schedule.master.AbstractMasterSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 下午8:54:32
 * 
 *       worker调度管理 ：用来接收 master调度命令，控制worker的运行状态
 * 
 */
public abstract class AbstractWorkerSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(AbstractWorkerSchedulerManager.class);

	protected final void init() {
		doInit();
		getClusterManager().registerNodeService(AbstractWorkerSchedulerManager.class, this);
	}

	protected abstract void doInit();

	protected String getOperationJobLockPath(String jobName) {
		String path = "workerSchedulerManager_operation_" + jobName;
		return path;
	}

	@Override
	public final void repair() {
	}

	@Override
	public final void scheduled(Job job) {
	}

	@Override
	public final void cancelScheduled(String jobChainName) {
	}

	/**
	 * 获取到的MasterSchedulerManager调用方法都会异步回调
	 * 
	 * @param asyCallback
	 * @return
	 */
	public AbstractMasterSchedulerManager getMasterSchedulerManager(AsyCallback asyCallback) {
		AbstractMasterSchedulerManager masterSchedulerManager = getClusterManager()
				.loolup(getClusterManager().getMaster(), AbstractMasterSchedulerManager.class, asyCallback);
		return masterSchedulerManager;
	}

	/**
	 * 获取到的MasterSchedulerManager调用方法都会同步
	 * 
	 * @return
	 */
	public AbstractMasterSchedulerManager getMasterSchedulerManager() {
		AbstractMasterSchedulerManager masterSchedulerManager = getClusterManager()
				.loolup(getClusterManager().getMaster(), AbstractMasterSchedulerManager.class);
		return masterSchedulerManager;
	}

	@RpcService()
	public abstract String execute(TriggerType dispatchType, String jobName);

	@RpcService()
	public abstract void suspend(TriggerType dispatchType, String jobName);

	@RpcService()
	public abstract void rest(TriggerType dispatchType, String jobName);

	@RpcService()
	public abstract void goOn(TriggerType dispatchType, String jobName);

	@RpcService()
	public abstract void stop(TriggerType dispatchType, String jobName);

	@RpcService()
	public abstract void finish(TriggerType dispatchType, String jobName);

	@RpcService()
	public abstract void stopAll(TriggerType dispatchType);

	public abstract void askEnd(String jobName, String workerName);

}
