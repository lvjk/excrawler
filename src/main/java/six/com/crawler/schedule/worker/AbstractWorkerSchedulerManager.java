package six.com.crawler.schedule.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Job;
import six.com.crawler.rpc.RpcService;
import six.com.crawler.schedule.AbstractSchedulerManager;

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
		getNodeManager().register(this);
	}

	protected abstract void doInit();
	
	@Override
	public final void repair() {}

	@Override
	public final void scheduled(Job job) {}

	@Override
	public final void cancelScheduled(String jobChainName) {}

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
