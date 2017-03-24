package six.com.crawler.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.api.ResponseMsg;
import six.com.crawler.dao.ExtractItemDao;
import six.com.crawler.dao.JobDao;
import six.com.crawler.dao.JobParamDao;
import six.com.crawler.dao.JobSnapshotDao;
import six.com.crawler.dao.RedisManager;
import six.com.crawler.dao.WorkerErrMsgDao;
import six.com.crawler.dao.WorkerSnapshotDao;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobProfile;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.JobSnapshotState;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.MasterAbstractSchedulerManager;
import six.com.crawler.service.JobService;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午4:06:47
 */
@Service
public class JobServiceImpl implements JobService {

	final static Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

	@Autowired
	private JobDao jobDao;

	@Autowired
	private JobParamDao jobParamDao;

	@Autowired
	private JobSnapshotDao jobSnapshotDao;

	@Autowired
	private WorkerSnapshotDao workerSnapshotDao;

	@Autowired
	private WorkerErrMsgDao workerErrMsgDao;

	@Autowired
	private MasterAbstractSchedulerManager scheduleManager;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private ExtractItemDao extractItemDao;

	static final String JOB_SERVICE_OPERATION_PRE = "JobService.operation.";

	public List<Job> queryIsScheduled() {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("isScheduled", 1);
		List<Job> result = jobDao.queryByParam(parameters);
		return result;
	}

	public void queryJobs(ResponseMsg<PageQuery<Job>> responseMsg, String jobName, int pageIndex, int pageSize) {
		pageSize = pageSize <= 0 || pageSize > 50 ? 15 : pageSize;
		PageQuery<Job> pageQuery = new PageQuery<>();
		List<Job> jobs = null;
		int totalSize = 0;
		pageIndex = pageIndex * pageSize;
		// 如果查询jobName==* 那么默认查询 本地节点任务
		if ("*".equals(jobName)) {
			jobs = new ArrayList<>();
			// 优先获取运行中的任务
			List<JobSnapshot> jobSnapshots = scheduleManager.getJobSnapshots();
			int end = pageIndex + pageSize;
			if (jobSnapshots.size() > 0) {
				if (jobSnapshots.size() > end) {
					jobSnapshots = jobSnapshots.subList(pageIndex, end);
				} else if (jobSnapshots.size() > pageIndex && jobSnapshots.size() < end) {
					jobSnapshots = jobSnapshots.subList(pageIndex, jobSnapshots.size());
				} else {
					jobSnapshots = Collections.emptyList();
				}
				totalSize = jobSnapshots.size();
				Job job = null;
				for (JobSnapshot jobSnapshot : jobSnapshots) {
					job = jobDao.query(jobSnapshot.getName());
					jobs.add(job);
				}
			}
			// 如果查询的运行job小于分页数量那么 再查询数据库补充 pageSize
			if (jobs.size() < pageSize) {
				jobName = "";
				List<Job> queryJobs = jobDao.pageQuery(jobName, pageIndex, pageSize);
				if (queryJobs.size() > 0) {
					totalSize = queryJobs.get(0).getTotalSize();
				}
				for (Job job : queryJobs) {
					if (!jobs.contains(job)) {
						jobs.add(job);
						if (jobs.size() >= pageSize) {
							break;
						}
					}
				}
			}
		} else {
			jobs = jobDao.pageQuery(jobName, pageIndex, pageSize);
			if (jobs.size() > 0) {
				totalSize = jobs.get(0).getTotalSize();
			}
		}
		sort(jobs);
		pageQuery.setTotalSize(totalSize);
		pageQuery.setTotalPage(totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1);
		pageQuery.setList(jobs);
		pageQuery.setPageIndex(pageIndex);
		pageQuery.setPageSize(pageSize);
		responseMsg.setData(pageQuery);
	}

	@Override
	public Job get(String jobName) {
		Job job = jobDao.query(jobName);
		return job;
	}

	@Override
	public List<Job> query(Map<String, Object> parameters) {
		List<Job> result = jobDao.queryByParam(parameters);
		return result;
	}

	@Override
	public Map<String, Object> queryJobInfo(String jobName) {
		Map<String, Object> result = new HashMap<>();
		// 1查询任务
		Job job = jobDao.query(jobName);
		// 2查询任务解析组件
		List<ExtractItem> paserItems = extractItemDao.query(jobName);
		// 3查询任务参数
		List<JobParam> jobParameters = jobParamDao.queryJobParams(jobName);
		result.put("job", job);
		result.put("paserItems", paserItems);
		result.put("jobParameters", jobParameters);
		return result;
	}

	@Override
	public List<JobParam> queryJobParams(String jobName) {
		List<JobParam> result = jobParamDao.queryJobParams(jobName);
		return result;
	}

	@Transactional
	public void reportJobSnapshot(JobSnapshot jobSnapshot) {
		if (null != jobSnapshot) {
			jobSnapshotDao.update(jobSnapshot);
			List<WorkerSnapshot> workerSnapshots = jobSnapshot.getWorkerSnapshots();
			if (null != workerSnapshots) {
				workerSnapshotDao.batchSave(workerSnapshots);
				for (WorkerSnapshot workerSnapshot : workerSnapshots) {
					if (null != workerSnapshot.getWorkerErrMsgs() && workerSnapshot.getWorkerErrMsgs().size() > 0) {
						workerErrMsgDao.batchSave(workerSnapshot.getWorkerErrMsgs());
					}

				}
			}
		}
	}

	public JobSnapshot queryLastJobSnapshotFromHistory(String excludeJobSnapshotId, String jobName) {
		JobSnapshot lastJobSnapshot = jobSnapshotDao.queryLast(jobName);
		return lastJobSnapshot;
	}

	@Override
	public List<JobSnapshot> queryJobSnapshotsFromHistory(String jobName) {
		List<JobSnapshot> result = jobSnapshotDao.query(jobName);
		return result;
	}

	public List<JobSnapshot> getJobSnapshotFromRegisterCenter(List<Map<String, String>> list) {
		List<JobSnapshot> result = null;
		if (null != list) {
			result = new ArrayList<JobSnapshot>();
			String jobName = null;
			String queueName = null;
			for (Map<String, String> map : list) {
				jobName = map.get("name");
				queueName = map.get("queueName");
				JobSnapshot jobSnapshot = null;
				jobSnapshot = scheduleManager.getJobSnapshot(jobName);
				if (null == jobSnapshot) {
					jobSnapshot = new JobSnapshot(jobName);
				} else {
					List<WorkerSnapshot> workerSnapshots = scheduleManager.getWorkerSnapshots(jobName);
					scheduleManager.totalWorkerSnapshot(jobSnapshot, workerSnapshots);
				}
				String tempProxyKey = RedisWorkQueue.PRE_PROXY_QUEUE_KEY + queueName;
				int proxySize = redisManager.llen(tempProxyKey);

				String tempRealKey = RedisWorkQueue.PRE_QUEUE_KEY + queueName;
				int realSize = redisManager.hllen(tempRealKey);

				String tempErrKey = RedisWorkQueue.PRE_ERR_QUEUE_KEY + queueName;
				int errSize = redisManager.llen(tempErrKey);

				jobSnapshot.setQueueName(queueName);
				jobSnapshot.setProxyQueueCount(proxySize);
				jobSnapshot.setRealQueueCount(realSize);
				jobSnapshot.setErrQueueCount(errSize);
				result.add(jobSnapshot);
			}
		} else {
			result = Collections.emptyList();
		}
		return result;
	}

	public JobSnapshot getJobSnapshotFromRegisterCenter1(String jobName) {
		JobSnapshot jobSnapshot = scheduleManager.getJobSnapshot(jobName);
		if (null != jobSnapshot) {
			// 判断任务是否运行过
			if (jobSnapshot.getEnumStatus() == JobSnapshotState.EXECUTING
					|| jobSnapshot.getEnumStatus() == JobSnapshotState.SUSPEND
					|| jobSnapshot.getEnumStatus() == JobSnapshotState.STOP
					|| jobSnapshot.getEnumStatus() == JobSnapshotState.FINISHED) {
				List<WorkerSnapshot> workerSnapshots = getWorkSnapshotsFromRegisterCenter(jobName);
				// totalWorkerSnapshot(jobSnapshot, workerSnapshots);
				jobSnapshot.setWorkerSnapshots(workerSnapshots);
			}
		}
		return jobSnapshot;
	}

	public void saveJobSnapshot(JobSnapshot jobSnapshot) {
		jobSnapshotDao.save(jobSnapshot);
	}

	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		jobSnapshotDao.update(jobSnapshot);
	}

	@Override
	public ResponseEntity<InputStreamResource> download(String param) {
		JobProfile profile = new JobProfile();
		Job job = get(param);
		if (null != job) {
			List<JobParam> jobParams = queryJobParams(job.getName());
			job.setParamList(jobParams);
			List<ExtractItem> extractItems = queryExtractItems(job.getName());
			profile.setJob(job);
			profile.setExtractItems(extractItems);
		}
		String xml = "";
		try {
			xml = JobProfile.buildXml(profile);
		} catch (Exception e) {
			log.error("downloadProfile err:" + param, e);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", param + "_job.xml");
		byte[] bytes = xml.getBytes();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return ResponseEntity.ok().headers(headers).contentLength(bytes.length)
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(new InputStreamResource(inputStream));
	}

	@Override
	public String upload(MultipartFile multipartFile) {
		String msg = null;
		if (null != multipartFile && !multipartFile.isEmpty()) {
			try {
				byte[] buffer = multipartFile.getBytes();
				String jobProfileXml = new String(buffer);
				JobProfile profile = JobProfile.buildJobProfile(jobProfileXml);
				Job job = profile.getJob();
				// 删除job参数数据
				jobParamDao.delJobParams(job.getName());
				// 删除job抽取项
				extractItemDao.del(job.getName());
				// 删除job
				jobDao.del(job.getName());
				jobDao.save(job);
				jobParamDao.batchSave(job.getParamList());
				extractItemDao.batchSave(profile.getExtractItems());
				msg = "uploadJobProfile[" + multipartFile.getName() + "] succeed";
			} catch (Exception e) {
				msg = "uploadJobProfile[" + multipartFile.getName() + "] err";
				log.error(msg, e);
			}
		} else {
			msg = "uploadJobProfile is empty";
		}
		return msg;
	}

	@Override
	public List<WorkerSnapshot> getWorkSnapshotsFromRegisterCenter(String jobName) {
		List<WorkerSnapshot> result = scheduleManager.getWorkerSnapshots(jobName);
		return result;
	}

	@Override
	public List<ExtractItem> queryExtractItems(String jobName) {
		List<ExtractItem> result = extractItemDao.query(jobName);
		return result;
	}

	public void updateIsScheduled(ResponseMsg<Integer> responseMsg, int version, String name, int isScheduled) {
		String msg = null;
		if (!StringUtils.isBlank(name)) {
			if (0 == isScheduled || 1 == isScheduled) {
				int newVersion = version + 1;
				boolean updateResult = jobDao.updateIsScheduled(version, newVersion, name, isScheduled) == 1;
				if (updateResult) {
					if (1 == isScheduled) {
						Job job = get(name);
						scheduleManager.scheduled(job);
						msg = "schedule job[" + name + "] succeed";
					} else {
						scheduleManager.cancelScheduled(name);
						msg = "cancel schedule job[" + name + "] succeed";
					}
					responseMsg.setData(newVersion);
					responseMsg.setIsOk(1);
				}
			} else {
				msg = "the job[" + name + "]'s isScheduled must be 0 or 1";
			}
		} else {
			msg = "the job's name must not be blank";
		}
		log.info(msg);
		responseMsg.setMsg(msg);
	}

	public void updateCronTrigger(ResponseMsg<Integer> responseMsg, int version, String name, String cronTrigger) {
		String msg = null;
		responseMsg.setIsOk(0);
		if (!StringUtils.isBlank(name)) {
			if (!StringUtils.isBlank(cronTrigger)) {
				try {
					CronScheduleBuilder.cronSchedule(cronTrigger);
				} catch (Exception e) {
					msg = "update job[" + name + "]'s cronTrigger[" + cronTrigger + "] is invalid";
					log.error(msg, e);
				}
				if (null == msg) {
					int newVersion = version + 1;
					try {
						int result = jobDao.updateCronTrigger(version, newVersion, name, cronTrigger);
						if (1 == result) {
							msg = "update job[" + name + "]'s cronTrigger[" + cronTrigger + "] succeed";
							responseMsg.setData(newVersion);
							responseMsg.setIsOk(1);
						} else {
							msg = "update job[" + name + "]'s cronTrigger[" + cronTrigger + "] failed";
						}
					} catch (Exception e) {
						msg = "update job[" + name + "]'s cronTrigger[" + cronTrigger + "] system exception";
						log.error(msg, e);
					}
				}
			} else {
				msg = "cronTrigger must not be blank";
			}
		} else {
			msg = "job's name must not be blank";
		}
		log.info(msg);
		responseMsg.setMsg(msg);
	}

	public void updateNextJobName(ResponseMsg<Integer> responseMsg, int version, String name, String nextJobName) {
		String msg = null;
		responseMsg.setIsOk(0);
		if (!StringUtils.isBlank(name)) {
			if (!StringUtils.equals(name, nextJobName)) {
				int newVersion = version + 1;
				try {
					int result = jobDao.updateNextJobName(version, newVersion, name, nextJobName);
					if (1 == result) {
						msg = "update job[" + name + "]'s nextJobName[" + nextJobName + "] succeed";
						responseMsg.setData(newVersion);
						responseMsg.setIsOk(1);
					} else {
						msg = "update job[" + name + "]'s nextJobName[" + nextJobName + "] failed";
					}
				} catch (Exception e) {
					msg = "update job[" + name + "]'s nextJobName[" + nextJobName + "] system exception";
					log.error(msg, e);
				}
			} else {
				msg = "the job's nextJob must not be own";
			}
		} else {
			msg = "the job's name must not be blank";
		}
		log.info(msg);
		responseMsg.setMsg(msg);
	}

	public void updateJobSnapshotStatus(ResponseMsg<Integer> responseMsg, int version, String id, int status) {
		String msg = null;
		responseMsg.setIsOk(0);
		if (!StringUtils.isBlank(id)) {
			int newVersion = version + 1;
			try {
				int result = jobSnapshotDao.updateStatus(version, newVersion, id, status);
				if (1 == result) {
					msg = "update jobSnapshot's[" + id + "]'s status[" + status + "] succeed";
					responseMsg.setData(newVersion);
					responseMsg.setIsOk(1);
				} else {
					msg = "update jobSnapshot's[" + id + "]'s status[" + status + "] failed";
				}
			} catch (Exception e) {
				msg = "update jobSnapshot's[" + id + "]'s status[" + status + "] system exception";
				log.error(msg, e);
			}
		} else {
			msg = "the jobSnapshot's id must not be blank";
		}
		log.info(msg);
		responseMsg.setMsg(msg);
	}

	/**
	 * 根据job运行状态 级别 名字 排序
	 * 
	 * @param jobs
	 */
	private void sort(List<Job> jobs) {
		if (null != jobs && jobs.size() > 1) {
			jobs.sort(new Comparator<Job>() {
				@Override
				public int compare(Job job1, Job job2) {
					if (scheduleManager.isRunning(job1.getName()) && !scheduleManager.isRunning(job2.getName())) {
						return -1;
					} else if (scheduleManager.isRunning(job2.getName())
							&& !scheduleManager.isRunning(job1.getName())) {
						return 1;
					} else {
						if (job1.getLevel() < job2.getLevel()) {
							return -1;
						} else if (job1.getLevel() > job2.getLevel()) {
							return 1;
						} else {
							return job1.getName().compareTo(job2.getName());
						}
					}
				}
			});
		}
	}

	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public MasterAbstractSchedulerManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(MasterAbstractSchedulerManager scheduleManager) {
		this.scheduleManager = scheduleManager;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public JobParamDao getJobParamDao() {
		return jobParamDao;
	}

	public void setJobParamDao(JobParamDao jobParamDao) {
		this.jobParamDao = jobParamDao;
	}

	public JobSnapshotDao getJobSnapshotDao() {
		return jobSnapshotDao;
	}

	public void setJobSnapshotDao(JobSnapshotDao jobSnapshotDao) {
		this.jobSnapshotDao = jobSnapshotDao;
	}

	public ExtractItemDao getExtractItemDao() {
		return extractItemDao;
	}

	public void setExtractItemDao(ExtractItemDao extractItemDao) {
		this.extractItemDao = extractItemDao;
	}

	public WorkerSnapshotDao getWorkerSnapshotDao() {
		return workerSnapshotDao;
	}

	public void setWorkerSnapshotDao(WorkerSnapshotDao workerSnapshotDao) {
		this.workerSnapshotDao = workerSnapshotDao;
	}

	public WorkerErrMsgDao getWorkerErrMsgDao() {
		return workerErrMsgDao;
	}

	public void setWorkerErrMsgDao(WorkerErrMsgDao workerErrMsgDao) {
		this.workerErrMsgDao = workerErrMsgDao;
	}

	@Override
	public void updateWorkSnapshotToRegisterCenter(WorkerSnapshot workerSnapshot, boolean isSaveErrMsg) {

	}
}
