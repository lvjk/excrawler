package six.com.crawler.schedule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Site;
import six.com.crawler.entity.WorkerErrMsg;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.work.Worker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:33:37
 */
@Component
public class WorkerSchedulerManager extends WorkerAbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(MasterSchedulerManager.class);

	private final static int SAVE_ERR_MSG_MAX = 20;

	private Map<String, Map<String, Worker>> localJobWorkersMap = new ConcurrentHashMap<String, Map<String, Worker>>();

	private AtomicInteger runningWroker = new AtomicInteger(0);

	private int workerRunningMaxSize;

	private ExecutorService executor;

	protected void doInit() {
		workerRunningMaxSize = getNodeManager().getCurrentNode().getRunningJobMaxSize();
		executor = Executors.newFixedThreadPool(workerRunningMaxSize, new JobWorkerThreadFactory());

	}

	/**
	 * 执行 worker
	 * 
	 * @param crawlerWorker
	 */
	private void executeWorker(Worker worker) {
		try {
			worker.init();
			startWorker(worker);
			worker.start();
		} catch (Exception e) {
			log.error("execute jobWorker {" + worker.getName() + "} err", e);
		} finally {
			endWorer(worker);
		}

	}

	/**
	 * 由内部守护线程循环读取等待被执行队列worker 执行此 调度执行job
	 * 
	 * 方法需要加分布式锁保证job的每个worker都是顺序被执行
	 * 
	 * @param job
	 */
	public synchronized void execute(String jobName) {
		String key = "workerSchedulerManager_doExecute_" + jobName;
		Job job = getJobDao().query(jobName);
		if (null != job) {
			log.info("worker node[" + getNodeManager().getCurrentNode().getName() + "] execute job[" + job.getName()
					+ "]");
			getRedisManager().lock(key);
			try {
				log.info("buiild job[" + job.getName() + "] worker");
				List<JobParam> jobParams = getJobParamDao().queryJobParams(job.getName());
				job.setParamList(jobParams);
				JobSnapshot jobSnapshot = getJobSnapshot(job.getName());
				if (null == jobSnapshot || jobSnapshot.getId() == null) {
					throw new RuntimeException("the job's jobSnapshot is not be init");
				}
				Worker worker = buildJobWorker(job, jobSnapshot);
				executor.execute(() -> {
					log.info("start execute job[" + job.getName() + "]'s worker[" + worker.getName() + "]");
					executeWorker(worker);
				});
				log.info("the job[" + job.getName() + "] is be executed by worker[" + worker.getName() + "]");
			} finally {
				getRedisManager().unlock(key);
			}
		}
	}

	/**
	 * 工作节点被动暂停任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void suspend(String jobName) {
		Map<String, Worker> workers = getWorkers(jobName);
		for (Worker worker : workers.values()) {
			worker.suspend();
		}
	}

	/**
	 * 工作节点被动继续任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void goOn(String jobName) {
		Map<String, Worker> workers = getWorkers(jobName);
		for (Worker worker : workers.values()) {
			worker.goOn();
		}
	}

	/**
	 * 工作节点被动停止任务
	 * 
	 * @param job
	 * @return
	 */
	public synchronized void stop(String jobName) {
		Map<String, Worker> workers = getWorkers(jobName);
		for (Worker worker : workers.values()) {
			worker.stop();
		}
	}

	public synchronized void stopAll() {
		// 然后获取当前节点有关的job worker 然后调用stop
		List<Worker> list = getLocalWorkers();
		for (Worker worker : list) {
			worker.stop();
		}
	}

	public int getRunningWorkerCount() {
		return runningWroker.get();
	}

	/**
	 * 容器结束时调用此销毁方法
	 */
	@PreDestroy
	public void destroy() {
		// 然后获取当前节点有关的job worker 然后调用stop
		stopAll();
		// 然后shut down worker线程池
		executor.shutdown();
	}

	private void startWorker( Worker worker) {
		String jobName=worker.getJob().getName();
		String workerName=worker.getName();
		WorkerSnapshot workerSnapshot = worker.getWorkerSnapshot();
		localJobWorkersMap.computeIfAbsent(worker.getJob().getName(), mapKey -> new ConcurrentHashMap<String, Worker>())
				.put(worker.getName(), worker);
		updateWorkerSnapshot(workerSnapshot);
		getNodeManager().getCurrentNode().setRunningJobSize(runningWroker.incrementAndGet());
		Map<String,Object> params=new HashMap<>();
		params.put("jobName",jobName);
		params.put("workerName",workerName);
		try {
			getNodeManager().execute(getNodeManager().getMasterNode(), ScheduledJobCommand.startWorker, params);
		} catch (Exception e) {
			log.error("notice master node job[" + jobName + "]'s worker[" + workerName + "] is started err", e);
		}
		
	}

	/**
	 * worker结束时调用
	 * 
	 * @param worker
	 * @param jobName
	 */
	public void endWorer(Worker worker) {
		String jobName=worker.getJob().getName();
		String workerName=worker.getName();
		worker.destroy();
		Map<String, Worker> jobWorkerMap = localJobWorkersMap.get(jobName);
		jobWorkerMap.remove(workerName);
		if (jobWorkerMap.size() == 0) {
			localJobWorkersMap.remove(jobName);
		}
		getNodeManager().getCurrentNode().setRunningJobSize(runningWroker.decrementAndGet());
		Map<String,Object> params=new HashMap<>();
		params.put("jobName",jobName);
		params.put("workerName",workerName);
		try {
			getNodeManager().execute(getNodeManager().getMasterNode(), ScheduledJobCommand.endWorker, params);
		} catch (Exception e) {
			log.error("notice master node job[" + jobName + "]'s worker[" + workerName + "] is end err", e);
		}
	}

	public Map<String, Worker> getWorkers(String jobName) {
		Map<String, Worker> jobsWorkerMap = localJobWorkersMap.get(jobName);
		return jobsWorkerMap;
	}

	public List<Worker> getLocalWorkers() {
		List<Worker> result = new ArrayList<>();
		Collection<Map<String, Worker>> all = localJobWorkersMap.values();
		for (Map<String, Worker> map : all) {
			result.addAll(map.values());
		}
		return result;
	}

	/**
	 * 通知master 调度中心 worker运行结束 任务
	 * 
	 * @param worker
	 * @param jobName
	 */

	// 构建 job worker
	protected Worker buildJobWorker(Job job, JobSnapshot jobSnapshot) {
		Worker newJobWorker = null;
		String workerClass = job.getWorkerClass();
		// 判断是否是htmljob 如果是那么调用 htmlJobWorkerBuilder 构建worker
		Class<?> clz = null;
		Constructor<?> constructor = null;
		try {
			clz = Class.forName(workerClass);
		} catch (ClassNotFoundException e) {
			log.error("ClassNotFoundException  err:" + workerClass, e);
		}
		if (null != clz) {
			try {
				constructor = clz.getConstructor();
			} catch (NoSuchMethodException e) {
				log.error("NoSuchMethodException getConstructor err:" + clz, e);
			} catch (SecurityException e) {
				log.error("SecurityException err" + clz, e);
			}
			if (null != constructor) {
				try {
					newJobWorker = (Worker) constructor.newInstance();
					String workerName = getWorkerNameByJob(job);
					WorkerSnapshot workerSnapshot = new WorkerSnapshot();
					workerSnapshot.setJobSnapshotId(jobSnapshot.getId());
					workerSnapshot.setJobName(job.getName());
					workerSnapshot.setLocalNode(getNodeManager().getCurrentNode().getName());
					workerSnapshot.setName(workerName);
					workerSnapshot.setWorkerErrMsgs(new ArrayList<WorkerErrMsg>());

					newJobWorker.bindWorkerSnapshot(workerSnapshot);
					newJobWorker.bindManager(this);
					newJobWorker.bindJobSnapshot(jobSnapshot);
					newJobWorker.bindJob(job);

				} catch (InstantiationException e) {
					log.error("InstantiationException  err:" + workerClass, e);
				} catch (IllegalAccessException e) {
					log.error("IllegalAccessException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				} catch (IllegalArgumentException e) {
					log.error("IllegalArgumentException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				} catch (InvocationTargetException e) {
					log.error("InvocationTargetException  err:" + workerClass.concat("|")
							.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()),
							e);
				}
			}
		}
		return newJobWorker;
	}

	public void updateWorkSnapshot(WorkerSnapshot workerSnapshot, boolean isSaveErrMsg) {
		List<WorkerErrMsg> errMsgs = workerSnapshot.getWorkerErrMsgs();
		if (null != errMsgs && ((isSaveErrMsg && errMsgs.size() > 0) || errMsgs.size() >= SAVE_ERR_MSG_MAX)) {
			getWorkerErrMsgDao().batchSave(errMsgs);
			errMsgs.clear();
		}
		updateWorkerSnapshot(workerSnapshot);
	}
}
