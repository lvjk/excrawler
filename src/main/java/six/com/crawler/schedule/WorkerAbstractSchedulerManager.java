package six.com.crawler.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 下午8:54:32
 */
public abstract class WorkerAbstractSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(WorkerAbstractSchedulerManager.class);

	protected final void init() {
		doInit();
		getNodeManager().register(ScheduledJobCommand.execute, params-> {
			String jobName =(String)params.get("jobName");
			WorkerAbstractSchedulerManager.this.execute(jobName);
			return null;
		});
		getNodeManager().register(ScheduledJobCommand.suspend,params-> {
			String jobName =(String)params.get("jobName");
			WorkerAbstractSchedulerManager.this.suspend(jobName);
			return null;
		});
		getNodeManager().register(ScheduledJobCommand.goOn,params-> {
			String jobName =(String)params.get("jobName");
			WorkerAbstractSchedulerManager.this.goOn(jobName);
			return null;
		});
		getNodeManager().register(ScheduledJobCommand.stop,params->{
			String jobName =(String)params.get("jobName");
			WorkerAbstractSchedulerManager.this.stop(jobName);
			return null;
		});

		getNodeManager().register(ScheduledJobCommand.stopAll,parma->{
			WorkerAbstractSchedulerManager.this.stopAll();
			return null;
		});
	}

	protected abstract void doInit();
}
