package six.com.crawler.schedule.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import six.com.crawler.entity.Job;
import six.com.crawler.rpc.AsyCallback;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.DispatchType;
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
		getClusterManager().registerNodeService(this);
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
		AbstractMasterSchedulerManager masterSchedulerManager = getClusterManager().loolup(
				getClusterManager().getMasterNodeFromRegister(), AbstractMasterSchedulerManager.class, asyCallback);
		return masterSchedulerManager;
	}

	/**
	 * 获取到的MasterSchedulerManager调用方法都会同步
	 * 
	 * @return
	 */
	public AbstractMasterSchedulerManager getMasterSchedulerManager() {
		AbstractMasterSchedulerManager masterSchedulerManager = getClusterManager()
				.loolup(getClusterManager().getMasterNodeFromRegister(), AbstractMasterSchedulerManager.class);
		return masterSchedulerManager;
	}

	@RpcService(name = "execute")
	public abstract void execute(DispatchType dispatchType, String jobName);

	@RpcService(name = "suspend")
	public abstract void suspend(DispatchType dispatchType, String jobName);

	@RpcService(name = "rest")
	public abstract void rest(DispatchType dispatchType, String jobName);

	@RpcService(name = "goOn")
	public abstract void goOn(DispatchType dispatchType, String jobName);

	@RpcService(name = "stop")
	public abstract void stop(DispatchType dispatchType, String jobName);

	@RpcService(name = "finish")
	public abstract void finish(DispatchType dispatchType, String jobName);

	@RpcService(name = "stopAll")
	public abstract void stopAll(DispatchType dispatchType);

	public abstract void askEnd(String jobName, String workerName);

}
