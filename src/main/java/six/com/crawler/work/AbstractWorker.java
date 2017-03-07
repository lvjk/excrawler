package six.com.crawler.work;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.StampedLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import six.com.crawler.common.DateFormats;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.WorkerErrMsg;
import six.com.crawler.common.utils.ThreadUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.common.entity.WorkerSnapshot;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月29日 下午8:16:06 类说明
 */
public abstract class AbstractWorker implements Worker {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractWorker.class);

	private AbstractSchedulerManager manager;

	private StampedLock setStateLock = new StampedLock();

	private WorkerSnapshot workerSnapshot;

	private Job job;
	// 工作状态 等待lock obejct
	private Object waitLock = new Object();
	// 最小延迟处理数据频率
	protected long minWorkFrequency;
	// 最大延迟处理数据频率
	protected long maxWorkFrequency;

	private long lastActivityTime;

	private String jobSnapshotId;

	private String currentNodeName;
	// 随机对象 产生随机控制时间
	private static Random randomDownSleep = new Random();

	public void bindManager(AbstractSchedulerManager manager) {
		this.manager = manager;
	}

	public void bindJob(Job job) {
		this.job = job;
	}

	/**
	 * 内部工作方法
	 * 
	 * @return 如果没有处理数据 那么返回false 否则返回true
	 */
	protected abstract void insideWork() throws Exception;

	private String catchException(Exception exception) {
		String msg=null;
		if(null!=exception){
			StringBuilder msgSb=new StringBuilder();
			Throwable throwable=exception;
			while(null!=throwable){
				msgSb.append(throwable.getClass());
				msgSb.append(":");
				msgSb.append(throwable.getMessage());
				msgSb.append("\n");
				StackTraceElement[] stackTraceElements=throwable.getStackTrace();
				if(null!=stackTraceElements){
					for(StackTraceElement stackTraceElement:stackTraceElements){
						msgSb.append("\t\t\t");
						msgSb.append(stackTraceElement.getClassName());
						msgSb.append(".");
						msgSb.append(stackTraceElement.getMethodName());
						msgSb.append("(");
						msgSb.append(stackTraceElement.getFileName());
						msgSb.append(":");
						msgSb.append(stackTraceElement.getLineNumber());
						msgSb.append(")");
						msgSb.append("\n");
					}
				}
				throwable=throwable.getCause();
			}
			msg=msgSb.toString();
		}
		return msg;
	}

	private final void work() {
		LOG.info("start worker:" + getName());
		// 记录job 开始时间
		workerSnapshot.setStartTime(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
		// 只要job 状态不等于 FINISHED 那么会一直循环处理
		try {
			while (true) {
				// 更新job 活动信息
				manager.getJobService().updateWorkSnapshotToRegisterCenter(workerSnapshot, false);
				// 当状态为running时才会正常工作
				if (getState() == WorkerLifecycleState.STARTED) {
					long processTime = System.currentTimeMillis();
					// 内部处理
					try {
						insideWork();
					} catch (Exception e) {
						LOG.error("worker process err", e);
						String msg = catchException(e);
						workerSnapshot.setErrCount(workerSnapshot.getErrCount() + 1);
						WorkerErrMsg errMsg = new WorkerErrMsg();
						errMsg.setJobSnapshotId(jobSnapshotId);
						errMsg.setJobName(job.getName());
						errMsg.setWorkerName(getName());
						errMsg.setStartTime(
								DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
						errMsg.setMsg(msg);
						workerSnapshot.getWorkerErrMsgs().add(errMsg);
						// 通知管理员异常
						getManager().noticeAdminByEmail("worker process err", e.getMessage());
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
					// 当state == WorkerLifecycleState.WAITED 时
				} else if (getState() == WorkerLifecycleState.WAITED) {
					// 通过job向注册中心检查 运行该job的所以worker是否已经全部等待
					if (currentNodeName.equals(job.getHostNode())) {
						boolean isAllWait = getManager().getRegisterCenter().workerIsAllWaited(job.getHostNode(),
								job.getName());
						if (isAllWait) {
							compareAndSetState(WorkerLifecycleState.WAITED, WorkerLifecycleState.FINISHED);
							continue;
						}
					}
					insideWait();
				} else if (getState() == WorkerLifecycleState.SUSPEND) {
					// 否处于等待中
					insideWait();
					// getState() == WorkerLifecycleState.STOPED 时 直接跳出循环
				} else if (getState() == WorkerLifecycleState.STOPED) {
					break;
				} else if (getState() == WorkerLifecycleState.FINISHED) {
					// 通知job worker 管理工作完成
					getManager().finishWorkerByJob(job.getHostNode(), job.getName());
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
			manager.getJobService().updateWorkSnapshotToRegisterCenter(workerSnapshot, true);
			LOG.info("jobWorker [" + getName() + "] is ended");
		}
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
		// 只有设置状态前 state=WorkerLifecycleState.READY 才会调用 work 方法
		if (compareAndSetState(WorkerLifecycleState.READY, WorkerLifecycleState.STARTED)) {
			work();
		}
	}

	@Override
	public final void waited() {
		// 只有设置状态前 state=WorkerLifecycleState.STARTED 才会调用 insideWait 方法
		compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
	}

	@Override
	public final void suspend() {
		// 只有设置状态前 state=WorkerLifecycleState.STARTED
		if (compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.SUSPEND)) {
		}
	}

	@Override
	public final void goOn() {
		// 只有设置状态前 state=WorkerLifecycleState.SUSPEND 才会调用 waitLock.notify() 方法
		if (compareAndSetState(WorkerLifecycleState.SUSPEND, WorkerLifecycleState.STARTED)) {
			insideNotify();
		}
	}

	@Override
	public final void stop() {
		// 一切状态都能设置 stoped只有设置状态前 state=WorkerLifecycleState.SUSPEND 才会调用
		// waitLock.notify() 方法
		WorkerLifecycleState snapshot = getAndSetState(WorkerLifecycleState.STOPED);
		if (snapshot == WorkerLifecycleState.SUSPEND || snapshot == WorkerLifecycleState.WAITED) {
			insideNotify();
		}
	}

	private void insideWait() {
		if ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.WAITED)) {
			synchronized (waitLock) {
				while ((getState() == WorkerLifecycleState.SUSPEND || getState() == WorkerLifecycleState.WAITED)) {
					try {
						waitLock.wait();
					} catch (InterruptedException e) {
						LOG.error("worker wait err", e);
					}
				}
			}
		}
	}

	private void insideNotify() {
		if (getState() == WorkerLifecycleState.STOPED || getState() == WorkerLifecycleState.STARTED) {
			synchronized (waitLock) {
				waitLock.notify();
			}
		}
	}

	/**
	 * 设置 worker状态 ，新至 RunningJobRegisterCenter 并返回之前快照值
	 * 
	 * @param state
	 */
	protected WorkerLifecycleState getAndSetState(WorkerLifecycleState state) {
		long stamp = setStateLock.writeLock();
		WorkerLifecycleState snapshot = this.getState();
		try {
			workerSnapshot.setState(state);
			manager.getRegisterCenter().registerWorker(this);
		} finally {
			setStateLock.unlock(stamp);
		}
		return snapshot;
	}

	protected boolean compareAndSetState(WorkerLifecycleState expect, WorkerLifecycleState update) {
		boolean result = false;
		if (this.getState() == expect) {
			long stamp = setStateLock.writeLock();
			try {
				if (this.getState() == expect) {
					workerSnapshot.setState(update);
					manager.getRegisterCenter().registerWorker(this);
					result = true;
				}
			} finally {
				setStateLock.unlock(stamp);
			}
		}
		return result;
	}

	@Override
	public void init() {
		String jobHostNode = getJob().getHostNode();
		String jobName = getJob().getName();
		MDC.put("jobName", jobName);
		String lockKey = jobName + "_worker_init";
		try {
			getManager().getRedisManager().lock(lockKey);
			JobSnapshot jobSnapshot = getManager().getJobService().getJobSnapshotFromRegisterCenter(jobHostNode,
					jobName);
			String workerName = getManager().getWorkerNameByJob(job);
			this.currentNodeName = manager.getCurrentNode().getName();
			workerSnapshot = new WorkerSnapshot();
			jobSnapshotId = jobSnapshot.getId();
			workerSnapshot.setJobSnapshotId(jobSnapshotId);
			workerSnapshot.setJobName(job.getName());
			workerSnapshot.setJobHostNode(job.getHostNode());
			workerSnapshot.setName(workerName);
			workerSnapshot.setWorkerErrMsgs(new ArrayList<WorkerErrMsg>());
			this.minWorkFrequency = job.getWorkFrequency();
			this.maxWorkFrequency = 2 * minWorkFrequency;
			List<JobParam> jobParameters = manager.getJobService().queryJobParams(jobName);
			job.setParamList(jobParameters);
			initWorker(jobSnapshot);
		} catch (Exception e) {
			throw new RuntimeException("init crawlWorker err", e);
		} finally {
			getManager().getRedisManager().unlock(lockKey);
		}
	}

	@Override
	public final void destroy() {
		LOG.info("start destroy worker:" + getName());
		MDC.remove("jobName");
		insideDestroy();
	}

	protected abstract void insideDestroy();

	protected abstract void initWorker(JobSnapshot jobSnapshot);

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
		return workerSnapshot.getState();
	}

	@Override
	public WorkerSnapshot getWorkerSnapshot() {
		return workerSnapshot;
	}

	@Override
	public boolean isRunning() {
		return getState() == WorkerLifecycleState.STARTED;
	}

	public AbstractSchedulerManager getManager() {
		return manager;
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

	protected abstract void onError(Exception t);

	protected static void writeByte(byte[] data) {
		try {
			FileUtils.writeByteArrayToFile(new File("F:/test.html"), data);
		} catch (IOException e) {

		}
	}

	protected static void writeString(String data) {
		try {
			FileUtils.write(new File("F:/test.html"), data);
		} catch (IOException e) {

		}
	}
}
