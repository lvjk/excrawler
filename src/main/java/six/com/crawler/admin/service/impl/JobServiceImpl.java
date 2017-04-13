package six.com.crawler.admin.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.admin.service.BaseService;
import six.com.crawler.admin.service.JobService;
import six.com.crawler.admin.service.WorkSpaceService;
import six.com.crawler.dao.ExtractItemDao;
import six.com.crawler.dao.JobDao;
import six.com.crawler.dao.JobParamDao;
import six.com.crawler.dao.JobSnapshotDao;
import six.com.crawler.dao.WorkerErrMsgDao;
import six.com.crawler.dao.WorkerSnapshotDao;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobParam;
import six.com.crawler.entity.JobProfile;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.PageQuery;
import six.com.crawler.entity.WorkSpaceInfo;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.utils.JobTableUtils;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午4:06:47
 */
@Service
public class JobServiceImpl extends BaseService implements JobService {

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
	private AbstractSchedulerManager scheduleManager;

	@Autowired
	private ExtractItemDao extractItemDao;

	@Autowired
	private WorkSpaceService workSpaceService;

	public ResponseMsg<PageQuery<Job>> queryJobs(String jobName, int pageIndex, int pageSize) {
		ResponseMsg<PageQuery<Job>> responseMsg = createResponseMsg();
		pageSize = pageSize <= 0 || pageSize > 50 ? 15 : pageSize;
		PageQuery<Job> pageQuery = new PageQuery<>();
		List<Job> jobs = null;
		int totalSize = 0;
		pageIndex = pageIndex * pageSize;
		// 如果查询jobName==Blank 那么默认查询 执行任务
		if (StringUtils.isBlank(jobName)) {
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
					if (null != job) {
						jobs.add(job);
					}
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
		responseMsg.setIsOk(1);
		return responseMsg;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResponseMsg<List<JobParam>> queryJobParams(String jobName) {
		ResponseMsg<List<JobParam>> responese = createResponseMsg();
		List<JobParam> jobParams = jobParamDao.queryJobParams(jobName);
		/**
		 * 由于< 属于特殊字符，所以改用 {}。
		 */
		for (JobParam jobParam : jobParams) {
			if (("createTableSqlTemplate".equals(jobParam.getName()) || "insertSqlTemplate".equals(jobParam.getName()))
					&& StringUtils.contains(jobParam.getValue(), JobTableUtils.TABLE_NAME_FLAG_OLD)) {
				jobParam.setValue(StringUtils.replace(jobParam.getValue(), JobTableUtils.TABLE_NAME_FLAG_OLD,
						JobTableUtils.TABLE_NAME_FLAG));
			}
		}
		responese.setIsOk(1);
		responese.setData(jobParams);
		return responese;
	}

	@Override
	public ResponseMsg<List<JobSnapshot>> queryJobSnapshotsFromHistory(String jobName) {
		ResponseMsg<List<JobSnapshot>> responseMsg = createResponseMsg();
		List<JobSnapshot> result = jobSnapshotDao.query(jobName);
		responseMsg.setIsOk(1);
		responseMsg.setData(result);
		return responseMsg;
	}

	public ResponseMsg<List<JobSnapshot>> getJobSnapshots(List<JobSnapshot> list) {
		ResponseMsg<List<JobSnapshot>> responseMsg = createResponseMsg();
		List<JobSnapshot> result = new ArrayList<JobSnapshot>();
		if (null != list) {
			JobSnapshot findJobSnapshot = null;
			for (JobSnapshot jobSnapshot : list) {
				findJobSnapshot = scheduleManager.getJobSnapshot(jobSnapshot.getName());
				if (null != findJobSnapshot) {
					jobSnapshot = findJobSnapshot;
					List<WorkerSnapshot> workerSnapshots = scheduleManager.getWorkerSnapshots(jobSnapshot.getName());
					scheduleManager.totalWorkerSnapshot(jobSnapshot, workerSnapshots);
				}
				WorkSpaceInfo workSpaceInfo = workSpaceService.getWorkSpaceInfo(jobSnapshot.getWorkSpaceName());
				jobSnapshot.setWorkSpaceDoingSize(workSpaceInfo.getDoingSize());
				jobSnapshot.setWorkSpaceErrSize(workSpaceInfo.getErrSize());
				result.add(jobSnapshot);
			}
		} else {
			result = Collections.emptyList();
		}
		responseMsg.setIsOk(1);
		responseMsg.setData(result);
		return responseMsg;
	}

	@Override
	public ResponseEntity<InputStreamResource> download(String param) {
		JobProfile profile = new JobProfile();
		Job job = jobDao.query(param);
		if (null != job) {
			List<JobParam> jobParams = jobParamDao.queryJobParams(param);
			job.setParamList(jobParams);
			List<ExtractItem> extractItems = extractItemDao.query(param);
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
				if (null != profile && null != profile.getJob()) {
					Job job = profile.getJob();
					// 删除job参数数据
					jobParamDao.delJobParams(job.getName());
					// 删除job抽取项
					extractItemDao.del(job.getName());
					// 删除job
					jobDao.del(job.getName());
					jobDao.save(job);
					if (null != job.getParamList() && !job.getParamList().isEmpty()) {
						jobParamDao.batchSave(job.getParamList());
					}
					if (null != profile.getExtractItems() && !profile.getExtractItems().isEmpty()) {
						extractItemDao.batchSave(profile.getExtractItems());
					}
					msg = "uploadJobProfile[" + multipartFile.getName() + "] succeed";
				}
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
	public ResponseMsg<List<ExtractItem>> queryExtractItems(String jobName) {
		ResponseMsg<List<ExtractItem>> responseMsg = createResponseMsg();
		List<ExtractItem> result = extractItemDao.query(jobName);
		responseMsg.setIsOk(1);
		responseMsg.setData(result);
		return responseMsg;
	}

	public ResponseMsg<String> updateJobParam(int version, String jobParamId, String name, String value) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (StringUtils.isNotBlank(jobParamId) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
			int newVersion = version + 1;
			int updateResult = jobParamDao.update(version, newVersion, jobParamId, name, value);
			if (1 == updateResult) {
				msg = "update jobParam[" + jobParamId + "] succeed";
				responseMsg.setData(String.valueOf(newVersion));
				responseMsg.setMsg(msg);
				responseMsg.setIsOk(1);
			} else {
				msg = "update jobParam[" + jobParamId + "] failed";
			}

		} else {
			msg = "update jobParam param invalid";
		}
		log.info(msg);
		return responseMsg;
	}

	public ResponseMsg<Integer> updateIsScheduled(int version, String name, int isScheduled) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
		String msg = null;
		if (!StringUtils.isBlank(name)) {
			if (0 == isScheduled || 1 == isScheduled) {
				int newVersion = version + 1;
				boolean updateResult = jobDao.updateIsScheduled(version, newVersion, name, isScheduled) == 1;
				if (updateResult) {
					if (1 == isScheduled) {
						Job job = jobDao.query(name);
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
		return responseMsg;
	}

	public ResponseMsg<Integer> updateCronTrigger(int version, String name, String cronTrigger) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
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
		return responseMsg;
	}

	public ResponseMsg<Integer> updateNextJobName(int version, String name, String nextJobName) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
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
		return responseMsg;
	}

	public ResponseMsg<Integer> updateJobSnapshotStatus(int version, String id, int status) {
		ResponseMsg<Integer> responseMsg = createResponseMsg();
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
		return responseMsg;
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

	public AbstractSchedulerManager getScheduleManager() {
		return scheduleManager;
	}

	public void setScheduleManager(AbstractSchedulerManager scheduleManager) {
		this.scheduleManager = scheduleManager;
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

	public WorkSpaceService getWorkSpaceService() {
		return workSpaceService;
	}

	public void setWorkSpaceService(WorkSpaceService workSpaceService) {
		this.workSpaceService = workSpaceService;
	}
}
