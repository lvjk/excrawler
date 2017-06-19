package six.com.crawler.work;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

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
import six.com.crawler.work.exception.WorkerException;
import six.com.crawler.work.exception.WorkerInitException;
import six.com.crawler.work.exception.WorkerOtherException;
import six.com.crawler.work.space.WorkSpace;
import six.com.crawler.work.space.WorkSpaceData;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月29日 下午8:16:06 类说明
 * 
 *          job's worker 抽象基础类，实现了基本了的流程控制
 */
public abstract class AbstractWorker<T extends WorkSpaceData> implements Worker<T> {

	private final static Logger log = LoggerFactory.getLogger(AbstractWorker.class);

	// 工作默认rest状态下 休息时间
	private final static int DEFAULT_REST_TIME = 2000;
	// 当前work线程
	private Thread workThread;
	// 用来lock 读写 状态
	private final StampedLock setStateLock = new StampedLock();
	// 用来lock Condition.await() 和condition.signalAll();
	private final ReentrantLock reentrantLock = new ReentrantLock();
	// 用来Condition.await() 和condition.signalAll();
	private final Condition condition = reentrantLock.newCondition();
	// 环境配置类
	private SpiderConfigure configure;
	// 集群分布式锁
	private DistributedLock distributedLock;
	// worker的上级调度管理者
	private WorkerSchedulerManager manager;
	// worker的上级job
	private Job job;
	//JobSnapshotId
	private String jobSnapshotId;
	// worker的工作空间
	private WorkSpace<T> workSpace;
	// worker处理的数据class
	private Class<T> workSpaceDataClz;
	// worker状态
	private volatile WorkerLifecycleState state = WorkerLifecycleState.READY;// 状态
	// worker快照
	private volatile WorkerSnapshot workerSnapshot;
	// 休息状态下休眠指定时间
	private long restWaitTime;
	// 最小延迟处理数据频率
	protected long minWorkFrequency;
	// 最大延迟处理数据频率
	protected long maxWorkFrequency;
	// 工作线程上次活动时间记录
	private long lastActivityTime;
	// 随机对象 产生随机控制时间
	private static Random randomDownSleep = new Random();

	/**
	 * 内部初始化方法，用于实现业务相关初始化工作
	 * 
	 * @param jobSnapshot
	 */
	protected abstract void initWorker(JobSnapshot jobSnapshot);

	/**
	 * 内部工作方法,用于实现相关业务
	 * 
	 * @param workerData
	 * @throws Exception
	 */
	protected abstract void insideWork(T workerData) throws WorkerException;

	/**
	 * 内部异常处理方法，用来处理业务相关异常
	 * 
	 * @param t
	 * @param workerData
	 */
	protected abstract void onError(Exception t, T workerData);

	/**
	 * 内部业务销毁方法，在工作结束后调用
	 */
	protected abstract void insideDestroy();

	public AbstractWorker(Class<T> workSpaceDataClz) {
		this.workSpaceDataClz = workSpaceDataClz;
	}

	@Override
	public void bindConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}

	@Override
	public void bindManager(WorkerSchedulerManager manager) {
		this.manager = manager;
	}

	@Override
	public void bindJob(Job job) {
		this.job = job;
	}
	
	@Override
	public void bindJobSnapshotId(String jobSnapshotId){
		this.jobSnapshotId=jobSnapshotId;
	}

	@Override
	public void bindWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		this.workerSnapshot = workerSnapshot;
	}

	private void init() {
		String jobName = getJob().getName();
		MDC.put("jobName", jobName);
		String path = "job_" + jobName + "_" + jobSnapshotId + "_worker";
		distributedLock = getManager().getClusterManager().getDistributedLock(path);
		try {
			distributedLock.lock();
			restWaitTime = (long) job.getParamInt(CrawlerJobParamKeys.REST_WAIT_TIME, DEFAULT_REST_TIME);
			JobSnapshot jobSnapshot = getJobSnapshot();
			this.minWorkFrequency = job.getWorkFrequency();
			this.maxWorkFrequency = 2 * minWorkFrequency;
			String workSpaceName = getJob().getWorkSpaceName();
			workSpace = getManager().getWorkSpaceManager().newWorkSpace(
					StringUtils.isBlank(workSpaceName) ? getJob().getName() : workSpaceName, workSpaceDataClz);
			initWorker(jobSnapshot);//
		} catch (Exception e) {
			throw new WorkerInitException("job[" + getJob().getName() + "]'s work[" + getName() + "] init err", e);
		} finally {
			distributedLock.unLock();
		}
	}

	/**
	 * 工作流程处理中所有不可控异常都会设置为stop退出当前处理
	 */
	private final void work() {
		workThread = Thread.currentThread();
		workerSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		log.info("start init:" + getName());
		try {
			init();
		} catch (WorkerException e) {
			getAndSetState(WorkerLifecycleState.STOPED);
			String errMsg = "job[" + getJob().getName() + "]'s work[" + getName() + "] init err";
			log.error(errMsg, e);
			doErr(new WorkerInitException(errMsg, e));
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
						String errMsg = "get data from workSpace:" + job.getWorkSpaceName();
						log.error(errMsg, e);
						doErr(new WorkerOtherException(errMsg, e));
						continue;
					}
					if (null != workData) {
						doStart(workData);
					} else {
						// wait状态只有worker自身获取不到数据时设置
						compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
					}
					// 休息状态时会检查工作队列是否为空，如果不为空那么状态改为start,否则休息默认时间
				} else if (getState() == WorkerLifecycleState.REST) {
					if (!getWorkSpace().doingIsEmpty()) {
						compareAndSetState(WorkerLifecycleState.REST, WorkerLifecycleState.STARTED);
					} else {
						signalWait(restWaitTime);
						manager.askEnd(getJob().getName(), getName());
					}
					// wait状态时会询问管理者是否end，然后休息
				} else if (getState() == WorkerLifecycleState.WAITED) {
					manager.askEnd(getJob().getName(), getName());
					signalWait(0);
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
			getAndSetState(WorkerLifecycleState.STOPED);
			String errMsg = "job[" + getJob().getName() + "]'s work[" + getName() + "] unkown err and will stop";
			log.error(errMsg, e);
			doErr(new WorkerOtherException(errMsg, e));
		}
		workerSnapshot.setEndTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		if (workerSnapshot.getTotalProcessCount() > 0) {
			workerSnapshot
					.setAvgProcessTime(workerSnapshot.getTotalProcessTime() / workerSnapshot.getTotalProcessCount());
		}
		manager.updateWorkSnapshotAndReport(workerSnapshot, true);
		log.info("start work:" + getName());
	}

	private void doStart(T workData) {
		long processTime = System.currentTimeMillis();
		try {
			insideWork(workData);
			getWorkSpace().ack(workData);
		} catch (WorkerException e) {
			log.error("worker process err", e);
			doErr(e);
			onError(e, workData);
		} catch (Exception e) {
			log.error("worker process err", e);
			doErr(new WorkerOtherException(e));
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

	private void doErr(WorkerException e) {
		String msg = ExceptionUtils.getExceptionMsg(e);
		workerSnapshot.setErrCount(workerSnapshot.getErrCount() + 1);
		WorkerErrMsg errMsg = new WorkerErrMsg();
		errMsg.setJobSnapshotId(workerSnapshot.getJobSnapshotId());
		errMsg.setJobName(job.getName());
		errMsg.setWorkerName(getName());
		errMsg.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		errMsg.setType(e.getType());
		errMsg.setMsg(msg);
		workerSnapshot.getWorkerErrMsgs().add(errMsg);
	}

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
		if (compareAndSetState(WorkerLifecycleState.READY, WorkerLifecycleState.STARTED)) {
			work();
		}
	}

	@Override
	public final void rest() {
		if(compareAndSetState(WorkerLifecycleState.WAITED, WorkerLifecycleState.REST)){
			signalRun();
			log.info("worker will rest:" + getName());
		}
		
	}

	@Override
	public final void suspend() {
		if (compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.SUSPEND)) {
			log.info("worker will suspend:" + getName());
		}
	}

	@Override
	public final void goOn() {
		if (compareAndSetState(WorkerLifecycleState.SUSPEND, WorkerLifecycleState.STARTED)
				|| compareAndSetState(WorkerLifecycleState.WAITED, WorkerLifecycleState.STARTED)) {
			signalRun();
			log.info("worker will goOn:" + getName());
		}
	}

	@Override
	public final void stop() {
		WorkerLifecycleState snapshot = getAndSetState(WorkerLifecycleState.STOPED);
		// 如果当前状态属于暂停或者等待下那么signalRun
		if (snapshot == WorkerLifecycleState.SUSPEND || snapshot == WorkerLifecycleState.WAITED) {
			signalRun();
		} else {
			// 调用当前线程interrupt,并免因为io网络等其他原因导致阻塞无法stop
			workThread.interrupt();
		}
		log.info("worker will stop:" + getName());
	}

	@Override
	public void finish() {
		WorkerLifecycleState snapshot = getAndSetState(WorkerLifecycleState.FINISHED);
		if (snapshot == WorkerLifecycleState.SUSPEND || snapshot == WorkerLifecycleState.WAITED) {
			signalRun();
		}
		log.info("worker will finish:" + getName());
	}

	/**
	 * 控制工作线程wait,restTime=0时为一直等待直到被唤醒,大于0为等待指定时间后恢复运行
	 * 
	 * @param restTime
	 */
	private void signalWait(long restTime) {
		if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.REST
				|| getState() == WorkerLifecycleState.WAITED)) {
			reentrantLock.lock();
			try {
				if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.REST
						|| getState() == WorkerLifecycleState.WAITED)) {
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

	/**
	 * 通知工作线程恢复运行
	 */
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

	@Override
	public String getName() {
		return workerSnapshot.getName();
	}
	
	
	@Override
	public String getJobSnapshotId(){
		return jobSnapshotId;
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

	/**
	 * 比较是否等于预期状态，然后继续update
	 * 
	 * @param expectState
	 * @param updateState
	 * @return
	 */
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

	@Override
	public int hashCode() {
		String name = getName();
		int hash = name.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object anObject) {
		if (null != anObject && anObject instanceof Worker) {
			Worker<?> worker = (Worker<?>) anObject;
			if (getName().equals(worker.getName())) {
				return true;
			}
		}
		return false;
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
}
