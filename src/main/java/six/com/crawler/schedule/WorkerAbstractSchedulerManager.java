package six.com.crawler.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.node.NodeCommand;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 下午8:54:32
 */
public abstract class WorkerAbstractSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(WorkerAbstractSchedulerManager.class);

	protected final void init() {
		doInit();
		getNodeManager().register(ScheduledJobCommand.execute, new NodeCommand() {
			@Override
			public Object execute(Object param) {
				String jobName = (String) param;
				WorkerAbstractSchedulerManager.this.execute(jobName);
				return null;
			}
		});
		getNodeManager().register(ScheduledJobCommand.suspend, new NodeCommand() {
			@Override
			public Object execute(Object param) {
				String jobName = (String) param;
				WorkerAbstractSchedulerManager.this.suspend(jobName);
				return null;
			}
		});
		getNodeManager().register(ScheduledJobCommand.goOn, new NodeCommand() {
			@Override
			public Object execute(Object param) {
				String jobName = (String) param;
				WorkerAbstractSchedulerManager.this.goOn(jobName);
				return null;
			}
		});
		getNodeManager().register(ScheduledJobCommand.stop, new NodeCommand() {
			@Override
			public Object execute(Object param) {
				String jobName = (String) param;
				WorkerAbstractSchedulerManager.this.stop(jobName);
				return null;
			}
		});

		getNodeManager().register(ScheduledJobCommand.stopAll, new NodeCommand() {
			@Override
			public Object execute(Object param) {
				WorkerAbstractSchedulerManager.this.stopAll();
				return null;
			}
		});
	}

	protected abstract void doInit();
}
