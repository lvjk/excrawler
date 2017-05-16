package six.com.crawler.schedule;

import java.lang.reflect.Method;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import six.com.crawler.entity.Job;
import six.com.crawler.node.NodeType;
import six.com.crawler.schedule.master.MasterAndWorkerSchedulerManager;
import six.com.crawler.schedule.master.MasterSchedulerManager;
import six.com.crawler.schedule.master.MasterStandSchedulerManager;
import six.com.crawler.schedule.worker.WorkerSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月11日 下午2:33:52
 * 
 *       调度器代理，实际调度器根据当前节点属性生成
 * 
 */
@Component
public class WrapperSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(WrapperSchedulerManager.class);

	private AbstractSchedulerManager schedulerManager;

	@Override
	protected void init() {
		if (NodeType.MASTER == getNodeManager().getCurrentNode().getType()) {
			schedulerManager = new MasterSchedulerManager();
		} else if (NodeType.MASTER_WORKER == getNodeManager().getCurrentNode().getType()
				|| NodeType.SINGLE == getNodeManager().getCurrentNode().getType()) {
			AbstractSchedulerManager masterSchedulerManager = new MasterSchedulerManager();
			setBean(masterSchedulerManager);
			masterSchedulerManager.init();
			AbstractSchedulerManager workerSchedulerManager = new WorkerSchedulerManager();
			setBean(workerSchedulerManager);
			workerSchedulerManager.init();
			schedulerManager = new MasterAndWorkerSchedulerManager(masterSchedulerManager, workerSchedulerManager);
		} else if (NodeType.MASTER_STANDBY == getNodeManager().getCurrentNode().getType()) {
			schedulerManager = new MasterStandSchedulerManager();
		} else if (NodeType.WORKER == getNodeManager().getCurrentNode().getType()) {
			schedulerManager = new WorkerSchedulerManager();
		} else {
			schedulerManager = new WorkerSchedulerManager();
		}
		setBean(schedulerManager);
		schedulerManager.init();
	}

	private void setBean(AbstractSchedulerManager abstractSchedulerManager) {
		Method[] methods = abstractSchedulerManager.getClass().getMethods();
		try {
			for (Method method : methods) {
				if (StringUtils.startsWith(method.getName(), "set")) {
					String feild = StringUtils.remove(method.getName(), "set");
					Method getMethod = abstractSchedulerManager.getClass().getMethod("get" + feild);
					method.invoke(abstractSchedulerManager, getMethod.invoke(this, new Object[] {}));
				}
			}
		} catch (Exception e) {
			log.error("init set schedulerManager err", e);
			System.exit(1);
		}
	}

	@Override
	public void repair() {
		schedulerManager.repair();
	}

	@Override
	public void scheduled(Job job) {
		schedulerManager.scheduled(job);
	}

	@Override
	public void cancelScheduled(String jobChainName) {
		schedulerManager.cancelScheduled(jobChainName);
	}

	@Override
	public void execute(DispatchType dispatchType, String jobName) {
		schedulerManager.execute(dispatchType, jobName);
	}

	@Override
	public void suspend(DispatchType dispatchType, String jobName) {
		schedulerManager.suspend(dispatchType, jobName);
	}

	@Override
	public void goOn(DispatchType dispatchType, String jobName) {
		schedulerManager.goOn(dispatchType, jobName);
	}

	@Override
	public void stop(DispatchType dispatchType, String jobName) {
		schedulerManager.stop(dispatchType, jobName);
	}

	@Override
	public void stopAll(DispatchType dispatchType) {
		schedulerManager.stopAll(dispatchType);
	}

	@Override
	public void shutdown() {
		schedulerManager.shutdown();
	}

	/**
	 * 容器结束时调用此销毁方法
	 */
	@PreDestroy
	public void destroy() {
		shutdown();
	}

	@Override
	public void rest(DispatchType dispatchType, String jobName) {
		
	}

	@Override
	public void finish(DispatchType dispatchType, String jobName) {
		
	}
}
