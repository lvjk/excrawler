package six.com.crawler.schedule;

import java.util.Set;

import six.com.crawler.node.ClusterManager;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月4日 下午5:20:37
 */
public class ScheduleDispatchTypeIntercept {

	private ClusterManager clusterManager;

	public ScheduleDispatchTypeIntercept(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	/**
	 * Schedule 调度操作前置拦截
	 * 
	 * @param dispatchType
	 *            等于null不做任何处理
	 * @param passDispatchTypeNames
	 *            通过的 dispatchTypeNames,如果dispatchTypeNames等于null，那么放行任意名称
	 * @param operationJobLockPath
	 *            操作的分布式锁path，如果等于null那么不使用锁
	 * @param schedulerProcess
	 *            通过后被调用的操作
	 * @return
	 */
	public <T> T intercept(TriggerType dispatchType, Set<String> passDispatchTypeNames, String operationJobLockPath,
			SchedulerProcess<T> schedulerProcess) {
		T resullt = null;
		if (null != dispatchType && (null == passDispatchTypeNames
				|| (null != passDispatchTypeNames && passDispatchTypeNames.contains(dispatchType.getName())))) {
			if (null != operationJobLockPath) {
				DistributedLock distributedLock = clusterManager.getDistributedLock(operationJobLockPath);
				try {
					distributedLock.lock();
					resullt = schedulerProcess.process();
				} finally {
					distributedLock.unLock();
				}
			} else {
				resullt = schedulerProcess.process();
			}
		}
		return resullt;
	}
}
