package six.com.crawler.work;

import java.util.Random;
import java.util.concurrent.locks.StampedLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import six.com.crawler.common.DateFormats;
import six.com.crawler.configure.SpiderConfigure;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.schedule.worker.WorkerSchedulerManager;
import six.com.crawler.utils.ExceptionUtils;
import six.com.crawler.utils.ThreadUtils;
import six.com.crawler.work.space.WorkSpace;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月29日 下午8:16:06 类说明
 */
public abstract class AbstractWorker<T extends WorkSpaceData> implements Worker<T> {

	private final static Logger log = LoggerFactory.getLogger(AbstractWorker.class);

	private final static long REST_TIME = 2000;
	// 用来lock 读写 状态
	private final StampedLock setStateLock = new StampedLock();
	// 用来lock Condition.await() 和condition.signalAll();
	private final ReentrantLock reentrantLock = new ReentrantLock();
	// 用来Condition.await() 和condition.signalAll();
	private final Condition condition = reentrantLock.newCondition();

	private SpiderConfigure configure;

	private DistributedLock distributedLock;

	private WorkerSchedulerManager manager;

	private Job job;

	private WorkSpace<T> workSpace;

	private Class<T> workSpaceDataClz;

	private volatile WorkerLifecycleState state = WorkerLifecycleState.READY;// 状态

	private volatile WorkerSnapshot workerSnapshot;

	// 最小延迟处理数据频率
	protected long minWorkFrequency;
	// 最大延迟处理数据频率
	protected long maxWorkFrequency;

	private long lastActivityTime;

	// 随机对象 产生随机控制时间
	private static Random randomDownSleep = new Random();

	public AbstractWorker(Class<T> workSpaceDataClz) {
		this.workSpaceDataClz = workSpaceDataClz;
	}

	public void bindConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	public void bindManager(WorkerSchedulerManager manager) {
		this.manager = manager;
	}

	public void bindJob(Job job) {
		this.job = job;
	}

	public void bindWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		this.workerSnapshot = workerSnapshot;
	}

	private void init() {
		String jobName = getJob().getName();
		String path = "job_" + jobName + "_" + getWorkerSnapshot().getJobSnapshotId() + "_worker";
		distributedLock = getManager().getNodeManager().getWriteLock(path);
		MDC.put("jobName", jobName);
		try {
			distributedLock.lock();
			JobSnapshot jobSnapshot = getJobSnapshot();
			this.minWorkFrequency = job.getWorkFrequency();
			this.maxWorkFrequency = 2 * minWorkFrequency;
			String workSpaceName = getJob().getWorkSpaceName();
			workSpace = getManager().getWorkSpaceManager().newWorkSpace(
					StringUtils.isBlank(workSpaceName) ? getJob().getName() : workSpaceName, workSpaceDataClz);
			initWorker(jobSnapshot);
		} catch (Exception e) {
			destroy();
			throw new RuntimeException("init crawlWorker err", e);
		} finally {
			distributedLock.unLock();
		}
	}

	protected abstract void initWorker(JobSnapshot jobSnapshot);

	private final void work() {
		workerSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		log.info("start init:" + getName());
		try {
			init();
		} catch (Exception e) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			log.error("init worker err:" + getName(), e);
			throw new RuntimeException(e);
		}
		log.info("end init:" + getName());
		log.info("start work:" + getName());
		try {
			while (true) {
				// 更新worker 工作信息
				manager.updateWorkSnapshotAndReport(workerSnapshot, false);
				// 运行状态时会从队列里获取数据然后进行处理，如果没有获取到数据那么状态改为wait
				if (getState() == WorkerLifecycleState.STARTED) {
					T workData = null;
					try {
						workData = getWorkSpace().pull();
					} catch (Exception e) {
						log.error("get data from workSpace err", e);
					}
					if (null != workData) {
						doStart(workData);
					} else {
						compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
					}
					// 休息状态时会从队列里获取数据然后进行处理，如果获取到数据那么状态改为start,否则休息默认时间
				} else if (getState() == WorkerLifecycleState.REST) {
					if (!getWorkSpace().doingIsEmpty()) {
						compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STARTED);
					}
					signalWait(REST_TIME);
					// wait状态时会询问管理者是否end，然后休息默认时间
				} else if (getState() == WorkerLifecycleState.WAITED) {
					try {
						manager.askEnd(getJob().getName(), getName());
					} catch (Exception e) {
						log.error("worker[" + getName() + "] ask manager is end", e);
					}
					signalWait(REST_TIME);
					// suspend状态时会直接休息
				} else if (getState() == WorkerLifecycleState.SUSPEND) {
					signalWait(0);
					// stop状态时会break
				} else if (getState() == WorkerLifecycleState.STOPED) {
					break;
					// finish状态时会break
				} else if (getState() == WorkerLifecycleState.FINISHED) {
					break;
				}

			}
		} catch (Exception e) {
			log.error("unkown job[" + getJob().getName() + "]'s work err:" + getName(), e);
			throw new RuntimeException(e);
		} finally {
			workerSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
			if (workerSnapshot.getTotalProcessCount() > 0) {
				workerSnapshot.setAvgProcessTime(
						workerSnapshot.getTotalProcessTime() / workerSnapshot.getTotalProcessCount());
			}
			manager.updateWorkSnapshotAndReport(workerSnapshot, true);
			log.info("start work:" + getName());
		}
	}

	private void doStart(T workData) {
		long processTime = System.currentTimeMillis();
		try {
			insideWork(workData);
		} catch (Exception e) {
			log.error("worker process err", e);
			// 记录异常信息
			String msg = ExceptionUtils.getExceptionMsg(e);
			workerSnapshot.setErrCount(workerSnapshot.getErrCount() + 1);
			WorkerErrMsg errMsg = new WorkerErrMsg();
			errMsg.setJobSnapshotId(workerSnapshot.getJobSnapshotId());
			errMsg.setJobName(job.getName());
			errMsg.setWorkerName(getName());
			errMsg.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
			errMsg.setMsg(msg);
			// 添加异常信息至缓存
			workerSnapshot.getWorkerErrMsgs().add(errMsg);
			// 通知管理员异常
			getManager().getEmailClient().sendMailToAdmin("worker err", msg);
			onError(e, workData);
		}
		// 统计次数加1
		workerSnapshot.setTotalProcessCount(workerSnapshot.getTotalProcessCount() + 1);
		processTime = System.currentTimeMillis() - processTime;
		workerSnapshot.setTotalProcessTime((int) (workerSnapshot.getTotalProcessTime() + processTime));
		if (processTime > workerSnapshot.getMaxProcessTime()) {
			workerSnapshot.setMaxProcessTime((int) processTime);
		} else if (processTime < workerSnapshot.getMinProcessTime()) {
			workerSnapshot.setMinProcessTime((int) processTime);
		}
		workerSnapshot.setAvgProcessTime(workerSnapshot.getTotalProcessTime() / workerSnapshot.getTotalProcessCount());
		// 频率控制
		frequencyControl();

	}

	/**
	 * 内部工作方法
	 * 
	 * @return 如果没有处理数据 那么返回false 否则返回true
	 */
	protected abstract void insideWork(T workerData) throws Exception;

	protected abstract void onError(Exception t, T workerData);

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
	public final void rest() {
		// 只有设置状态前 state=WorkerLifecycleState.STARTED 才会设置 WAITED
		compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
	}

	@Override
	public final void suspend() {
		// 只有设置状态前 state=WorkerLifecycleState.STARTED 才会设置 SUSPEND
		if (compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.SUSPEND)) {
			log.info("suspend worker:" + getName());
		}
	}

	@Override
	public final void goOn() {
		// 只有设置状态前 state=WorkerLifecycleState.SUSPEND 才会调用 waitLock.notify() 方法
		if (compareAndSetState(WorkerLifecycleState.SUSPEND, WorkerLifecycleState.STARTED)
				|| compareAndSetState(WorkerLifecycleState.WAITED, WorkerLifecycleState.STARTED)) {
			signalRun();
			log.info("goOn worker:" + getName());
		}
	}

	@Override
	public final void stop() {
		WorkerLifecycleState snapshot = getAndSetState(WorkerLifecycleState.STOPED);
		if (snapshot == WorkerLifecycleState.SUSPEND || snapshot == WorkerLifecycleState.WAITED) {
			signalRun();
		}
		log.info("stop worker:" + getName());
	}

	@Override
	public void finish() {
		WorkerLifecycleState snapshot = getAndSetState(WorkerLifecycleState.FINISHED);
		if (snapshot == WorkerLifecycleState.SUSPEND || snapshot == WorkerLifecycleState.WAITED) {
			signalRun();
		}
		log.info("finished worker:" + getName());
	}

	private void signalWait(long restTime) {
		if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.WAITED)) {
			reentrantLock.lock();
			try {
				if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.WAITED)) {
					if (restTime > 0) {
						condition.await(restTime, TimeUnit.MILLISECONDS);
					} else {
						condition.await();
					}
				}
			} catch (InterruptedException e) {
				log.error("worker wait err", e);
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
	@Override
	public String getName() {
		return workerSnapshot.getName();
	}

	@Override
	public SpiderConfigure getConfigure() {
		return configure;
	}

	@Override
	public WorkSpace<T> getWorkSpace() {
		return workSpace;
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
			manager.getScheduleCache().updateWorkerSnapshot(workerSnapshot);
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
					manager.getScheduleCache().updateWorkerSnapshot(workerSnapshot);
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
		JobSnapshot jobSnapshot = getManager().getScheduleCache().getJobSnapshot(getJob().getName());
		return jobSnapshot;
	}

	@Override
	public Job getJob() {
		return job;
	}

	@Override
	public long getWorkFrequency() {
		return minWorkFrequency;
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
	}

	public boolean equals(Object anObject) {
		if (null != anObject && anObject instanceof Worker) {
			Worker<?> worker = (Worker<?>) anObject;
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
		log.info("start destroy worker:" + getName());
		if (null != workSpace) {
			try {
				workSpace.close();
			} catch (Exception e) {
				log.error("workSpace[" + workSpace.getName() + "] close", e);
			}
		}
		try {
			insideDestroy();
		} catch (Exception e) {
			log.error("worker[" + getName() + "] insideDestroy", e);
		}
		MDC.remove("jobName");
	}

	protected abstract void insideDestroy();

}
