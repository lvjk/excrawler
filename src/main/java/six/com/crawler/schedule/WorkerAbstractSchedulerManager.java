package six.com.crawler.schedule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.entity.WorkerErrMsg;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.work.Worker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月13日 上午11:42:09
 * 
 *       工作节点调度管理 （只能被master调度 ）
 * 
 */
public abstract class WorkerAbstractSchedulerManager extends AbstractSchedulerManager {

	final static Logger log = LoggerFactory.getLogger(WorkerAbstractSchedulerManager.class);

	/**
	 * 检查注册运行job 的workers 是否全部wait
	 * 
	 * @param crawlerWorker
	 * @param job
	 * @return
	 */
	public abstract boolean workerIsAllWaited(String jobName);

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
					workerSnapshot.setLocalNode(getClusterManager().getCurrentNode().getName());
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
}
