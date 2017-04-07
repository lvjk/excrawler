package six.com.crawler.work;

import java.util.Random;
import java.util.concurrent.locks.StampedLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import six.com.crawler.common.DateFormats;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.worker.WorkerSchedulerManager;
import six.com.crawler.utils.ExceptionUtils;
import six.com.crawler.utils.ThreadUtils;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月29日 下午8:16:06 类说明
 */
public abstract class AbstractWorker implements Worker {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractWorker.class);

	// 用来lock 读写 状态
	private final StampedLock setStateLock = new StampedLock();
	// 用来lock Condition.await() 和condition.signalAll();
	private final ReentrantLock reentrantLock = new ReentrantLock();
	// 用来Condition.await() 和condition.signalAll();
	private final Condition condition = reentrantLock.newCondition();

	private WorkerSchedulerManager manager;

	private Job job;

	private JobSnapshot jobSnapshot;

	private volatile WorkerLifecycleState state = WorkerLifecycleState.READY;// 状态

	private volatile WorkerSnapshot workerSnapshot;

	// 最小延迟处理数据频率
	protected long minWorkFrequency;
	// 最大延迟处理数据频率
	protected long maxWorkFrequency;

	private long lastActivityTime;

	// 随机对象 产生随机控制时间
	private static Random randomDownSleep = new Random();

	public void bindManager(WorkerSchedulerManager manager) {
		this.manager = manager;
	}

	public void bindWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		this.workerSnapshot = workerSnapshot;
	}

	public void bindJobSnapshot(JobSnapshot jobSnapshot) {
		this.jobSnapshot = jobSnapshot;
	}

	public void bindJob(Job job) {
		this.job = job;
	}

	@Override
	public void init() {
		String jobName = getJob().getName();
		MDC.put("jobName", jobName);
		String lockKey = jobName + "_worker_init";
		try {
			getManager().getRedisManager().lock(lockKey);
			this.minWorkFrequency = job.getWorkFrequency();
			this.maxWorkFrequency = 2 * minWorkFrequency;
			initWorker(jobSnapshot);
		} catch (Exception e) {
			throw new RuntimeException("init crawlWorker err", e);
		} finally {
			getManager().getRedisManager().unlock(lockKey);
		}
	}

	protected abstract void initWorker(JobSnapshot jobSnapshot);

	private final void work() {
		LOG.info("start worker:" + getName());
		// 记录job 开始时间
		workerSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		// 只要job 状态不等于 FINISHED 那么会一直循环处理
		try {
			while (true) {
				// 更新job 活动信息
				manager.updateWorkSnapshotAndReport(workerSnapshot, false);
				// 当状态为running时才会正常工作
				if (getState() == WorkerLifecycleState.STARTED) {
					long processTime = System.currentTimeMillis();
					// 内部处理
					try {
						insideWork();
					} catch (Exception e) {
						LOG.error("worker process err", e);
						String msg = ExceptionUtils.getExceptionMsg(e);
						workerSnapshot.setErrCount(workerSnapshot.getErrCount() + 1);
						WorkerErrMsg errMsg = new WorkerErrMsg();
						errMsg.setJobSnapshotId(workerSnapshot.getJobSnapshotId());
						errMsg.setJobName(job.getName());
						errMsg.setWorkerName(getName());
						errMsg.setStartTime(
								DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
						errMsg.setMsg(msg);
						workerSnapshot.getWorkerErrMsgs().add(errMsg);
						// 通知管理员异常
						//getManager().getEmailClient().sendMailToAdmin("worker process err",msg);
						onError(e);
					}
					// 频率控制
					frequencyControl();
					// 统计次数加1
					workerSnapshot.setTotalProcessCount(workerSnapshot.getTotalProcessCount() + 1);
					processTime = System.currentTimeMillis() - processTime;
					workerSnapshot.setTotalProcessTime((int) (workerSnapshot.getTotalProcessTime() + processTime));
					if (processTime > workerSnapshot.getMaxProcessTime()) {
						workerSnapshot.setMaxProcessTime((int) processTime);
					}
					if (processTime < workerSnapshot.getMinProcessTime()) {
						workerSnapshot.setMinProcessTime((int) processTime);
					}
					workerSnapshot.setAvgProcessTime(
							workerSnapshot.getTotalProcessTime() / workerSnapshot.getTotalProcessCount());
					// 当state == WorkerLifecycleState.WAITED 时
				} else if (getState() == WorkerLifecycleState.WAITED) {
					// 通过job向注册中心检查 运行该job的所以worker是否已经全部等待
					boolean isAllWait = getManager().workerIsAllWaited(job.getName());
					if (isAllWait) {
						compareAndSetState(WorkerLifecycleState.WAITED, WorkerLifecycleState.FINISHED);
						continue;
					}
					signalWait();
				} else if (getState() == WorkerLifecycleState.SUSPEND) {
					signalWait();
				} else if (getState() == WorkerLifecycleState.STOPED) {
					break;
				} else if (getState() == WorkerLifecycleState.FINISHED) {
					break;
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			workerSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
			if (workerSnapshot.getTotalProcessCount() > 0) {
				workerSnapshot.setAvgProcessTime(
						workerSnapshot.getTotalProcessTime() / workerSnapshot.getTotalProcessCount());
			}
			manager.updateWorkSnapshotAndReport(workerSnapshot, true);
			LOG.info("jobWorker [" + getName() + "] is ended");
		}
	}

	/**
	 * 内部工作方法
	 * 
	 * @return 如果没有处理数据 那么返回false 否则返回true
	 */
	protected abstract void insideWork() throws Exception;

	protected abstract void onError(Exception t);

	/**
	 * 频率控制
	 */
	protected void frequencyControl() {
		long sleep = System.currentTimeMillis() - lastActivityTime;
		if (sleep < minWorkFrequency) {
			// 生成隨機 時間 避免服務器 識別出固定規律
			long tempTime = (long) (randomDownSleep.nextDouble() * maxWorkFrequency) + sleep;
			ThreadUtils.sleep(tempTime);
		}
		lastActivityTime = System.currentTimeMillis();
	}

	@Override
	public final void start() {
		// 只有设置状态前 state=WorkerLifecycleState.READY 才会调用 work 方法
		if (compareAndSetState(WorkerLifecycleState.READY, WorkerLifecycleState.STARTED)) {
			work();
		}
	}

	@Override
	public final void waited() {
		// 只有设置状态前 state=WorkerLifecycleState.STARTED 才会设置 WAITED
		compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
	}

	@Override
	public final void suspend() {
		// 只有设置状态前 state=WorkerLifecycleState.STARTED 才会设置 SUSPEND
		if (compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.SUSPEND)) {
			LOG.info("suspend worker:" + getName());
		}
	}

	@Override
	public final void goOn() {
		// 只有设置状态前 state=WorkerLifecycleState.SUSPEND 才会调用 waitLock.notify() 方法
		if (compareAndSetState(WorkerLifecycleState.SUSPEND, WorkerLifecycleState.STARTED)) {
			signalRun();
			LOG.info("goOn worker:" + getName());
		}
	}

	@Override
	public final void stop() {
		// 一切状态都能设置 stoped只有设置状态前 state=WorkerLifecycleState.SUSPEND 才会调用
		WorkerLifecycleState snapshot = getAndSetState(WorkerLifecycleState.STOPED);
		if (snapshot == WorkerLifecycleState.SUSPEND || snapshot == WorkerLifecycleState.WAITED) {
			signalRun();
		}
		LOG.info("stop worker:" + getName());
	}

	private void signalWait() {
		if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.WAITED)) {
			reentrantLock.lock();
			try {
				if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.WAITED)) {
					condition.await();
				}
			} catch (InterruptedException e) {
				LOG.error("worker wait err", e);
			} finally {
				reentrantLock.unlock();
			}
		}
	}

	private void signalRun() {
		if (getState() == WorkerLifecycleState.STARTED || getState() == WorkerLifecycleState.STOPED
				|| getState() == WorkerLifecycleState.FINISHED) {
			reentrantLock.lock();
			try {
				if (getState() == WorkerLifecycleState.STARTED || getState() == WorkerLifecycleState.STOPED
						|| getState() == WorkerLifecycleState.FINISHED) {
					condition.signalAll();
				}
			} finally {
				reentrantLock.unlock();
			}
		}
	}

	/**
	 * 获取 worker name
	 * 
	 * @return
	 */
	public String getName() {
		return workerSnapshot.getName();
	}

	@Override
	public WorkerLifecycleState getState() {
		return state;
	}

	/**
	 * 设置 worker状态 ，新至 RunningJobRegisterCenter 并返回之前快照值
	 * 
	 * @param state
	 */
	protected WorkerLifecycleState getAndSetState(WorkerLifecycleState updateState) {
		long stamp = setStateLock.writeLock();
		WorkerLifecycleState snapshot = this.getState();
		try {
			this.state = updateState;
			workerSnapshot.setState(updateState);
			manager.updateWorkerSnapshot(workerSnapshot);
		} finally {
			setStateLock.unlock(stamp);
		}
		return snapshot;
	}

	protected boolean compareAndSetState(WorkerLifecycleState expectState, WorkerLifecycleState updateState) {
		boolean result = false;
		if (this.getState() == expectState) {
			long stamp = setStateLock.writeLock();
			try {
				if (this.getState() == expectState) {
					this.state = updateState;
					workerSnapshot.setState(updateState);
					manager.updateWorkerSnapshot(workerSnapshot);
					result = true;
				}
			} finally {
				setStateLock.unlock(stamp);
			}
		}
		return result;
	}

	@Override
	public WorkerSnapshot getWorkerSnapshot() {
		return workerSnapshot;
	}

	@Override
	public boolean isRunning() {
		return getState() == WorkerLifecycleState.STARTED;
	}

	public WorkerSchedulerManager getManager() {
		return manager;
	}

	public JobSnapshot getJobSnapshot() {
		return jobSnapshot;
	}

	@Override
	public Job getJob() {
		return job;
	}

	public long getWorkFrequency() {
		return minWorkFrequency;
	}

	public long getLastActivityTime() {
		return lastActivityTime;
	}

	public boolean equals(Object anObject) {
		if (null != anObject && anObject instanceof Worker) {
			Worker worker = (Worker) anObject;
			if (getName().equals(worker.getName())) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		String name = getName();
		int hash = name.hashCode();
		return hash;
	}

	@Override
	public final void destroy() {
		LOG.info("start destroy worker:" + getName());
		MDC.remove("jobName");
		insideDestroy();
	}

	protected abstract void insideDestroy();

}
