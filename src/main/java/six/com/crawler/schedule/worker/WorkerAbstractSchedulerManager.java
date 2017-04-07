package six.com.crawler.schedule.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 下午8:54:32
 */
public abstract class WorkerAbstractSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(WorkerAbstractSchedulerManager.class);

	protected final void init() {
		doInit();
		getNodeManager().register(this);
	}

	protected abstract void doInit();
	
	@RpcService(name = "execute")
	public abstract void execute(String jobName);
	
	@RpcService(name = "suspend")
	public abstract void suspend(String jobName);
	
	@RpcService(name = "goOn")
	public abstract void goOn(String jobName);
	
	@RpcService(name = "stop")
	public abstract void stop(String jobName);
	
	@RpcService(name = "stopAll")
	public abstract void stopAll();
}
