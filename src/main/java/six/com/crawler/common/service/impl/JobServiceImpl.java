package six.com.crawler.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
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

import six.com.crawler.common.DateFormats;
import six.com.crawler.common.RedisManager;
import six.com.crawler.common.dao.JobSnapshotDao;
import six.com.crawler.common.dao.JobDao;
import six.com.crawler.common.dao.JobParamDao;
import six.com.crawler.common.dao.ExtractItemDao;
import six.com.crawler.common.dao.WorkerErrMsgDao;
import six.com.crawler.common.dao.WorkerSnapshotDao;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.JobParam;
import six.com.crawler.common.entity.JobProfile;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.JobSnapshotState;
import six.com.crawler.common.entity.Node;
import six.com.crawler.common.entity.QueueInfo;
import six.com.crawler.common.entity.WorkerErrMsg;
import six.com.crawler.common.service.JobService;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.schedule.RegisterCenter;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月8日 下午4:06:47
 */
@Service
public class JobServiceImpl implements JobService {

	final static Logger LOG = LoggerFactory.getLogger(JobServiceImpl.class);

	private final static int SAVE_ERR_MSG_MAX = 20;
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
	private AbstractSchedulerManager commonScheduleManager;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private RegisterCenter registerCenter;

	@Autowired
	private ExtractItemDao extractItemDao;

	static final String JOB_SERVICE_OPERATION_PRE = "JobService.operation.";

	public Node totalNodeJobInfo(String nodeName) {
		return jobDao.totalNodeJobInfo(nodeName);
	}

	@Override
	public Job queryJob(String jobName) {
		Job job = jobDao.query(jobName);
		return job;
	}

	@Override
	public List<Job> fuzzyQuery(String jobName) {
		jobName = "%" + jobName + "%";
		return jobDao.fuzzyQuery(jobName);
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
	public List<Job> queryJobs(int pageIndex, int pageSize) {
		Map<String, Object> parameters = new HashMap<>();
		List<Job> jobs = jobDao.queryByParam(parameters);
		return jobs;
	}

	@Override
	public List<JobParam> queryJobParams(String jobName) {
		List<JobParam> result = jobParamDao.queryJobParams(jobName);
		return result;
	}

	@Override
	public void update(Job job) {
		jobDao.update(job);
	}

	@Transactional
	public void reportJobSnapshot(String nodeName, String jobName) {
		JobSnapshot jobSnapshot = getJobSnapshotFromRegisterCenter(nodeName, jobName);
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
		registerCenter.delJobSnapshot(nodeName,jobName);
	}

	public JobSnapshot queryLastJobSnapshotFromHistory(String jobName) {
		JobSnapshot lastJobSnapshot = null;
		List<JobSnapshot> result = queryJobSnapshotsFromHistory(jobName);
		if (null != result && !result.isEmpty()) {
			lastJobSnapshot = result.get(0);
		}
		return lastJobSnapshot;
	}

	@Override
	public List<JobSnapshot> queryJobSnapshotsFromHistory(String jobName) {
		List<JobSnapshot> result = jobSnapshotDao.query(jobName);
		return result;
	}

	public JobSnapshot getJobSnapshotFromRegisterCenter(String nodeName, String jobName, String queueName) {
		JobSnapshot jobSnapshot = getJobSnapshotFromRegisterCenter(nodeName, jobName);
		if (null == jobSnapshot) {
			jobSnapshot = new JobSnapshot(nodeName, jobName);
		}
		QueueInfo tempQueueInfo = getJobQueueInfos(queueName);
		jobSnapshot.setQueueName(queueName);
		jobSnapshot.setRealQueueCount(tempQueueInfo.getRealQueueSize());
		jobSnapshot.setProxyQueueCount(tempQueueInfo.getProxyQueueSize());
		jobSnapshot.setErrQueueCount(tempQueueInfo.getErrQueueCount());
		return jobSnapshot;
	}

	@Override
	public JobSnapshot getJobSnapshotFromRegisterCenter(String hostNode, String jobName) {
		JobSnapshot jobSnapshot = registerCenter.getJobSnapshot(hostNode, jobName);
		if (null != jobSnapshot) {
			// 判断任务是否运行过
			if (jobSnapshot.getEnumState() == JobSnapshotState.EXECUTING
					|| jobSnapshot.getEnumState() == JobSnapshotState.SUSPEND
					|| jobSnapshot.getEnumState() == JobSnapshotState.STOP
					|| jobSnapshot.getEnumState() == JobSnapshotState.FINISHED) {
				List<WorkerSnapshot> workerSnapshots = getWorkSnapshotsFromRegisterCenter(hostNode, jobName);
				totalWorkerSnapshot(jobSnapshot, workerSnapshots);
				jobSnapshot.setWorkerSnapshots(workerSnapshots);
			}
		}
		return jobSnapshot;
	}

	private void totalWorkerSnapshot(JobSnapshot jobSnapshot, List<WorkerSnapshot> workerSnapshots) {
		if (null != jobSnapshot && null != workerSnapshots && !workerSnapshots.isEmpty()) {
			int totalProcessCount = 0;
			int totalResultCount = 0;
			int totalProcessTime = 0;
			int maxProcessTime = 0;
			int minProcessTime = -1;
			int avgProcessTime = 0;
			int errCount = 0;
			Date startTime = null;
			Date endTime = null;
			for (WorkerSnapshot workerSnapshot : workerSnapshots) {
				try {
					if (StringUtils.isNoneBlank(workerSnapshot.getStartTime())) {
						Date tempStartTime = DateUtils.parseDate(workerSnapshot.getStartTime(),
								DateFormats.DATE_FORMAT_1);
						if (null == startTime || tempStartTime.before(startTime)) {
							startTime = tempStartTime;
						}
					}
					if (StringUtils.isNoneBlank(workerSnapshot.getEndTime())) {
						Date tempEndTime = DateUtils.parseDate(workerSnapshot.getEndTime(), DateFormats.DATE_FORMAT_1);
						if (null == endTime || tempEndTime.after(endTime)) {
							endTime = tempEndTime;
						}
					}
				} catch (ParseException e) {
					LOG.error(e.getMessage());
				}
				totalProcessCount += workerSnapshot.getTotalProcessCount();
				totalResultCount += workerSnapshot.getTotalResultCount();
				totalProcessTime += workerSnapshot.getTotalProcessTime();
				if (workerSnapshot.getMaxProcessTime() > maxProcessTime) {
					maxProcessTime = workerSnapshot.getMaxProcessTime();
				}
				if (-1 == minProcessTime || workerSnapshot.getMinProcessTime() < minProcessTime) {
					minProcessTime = workerSnapshot.getMinProcessTime();
				}
				avgProcessTime += workerSnapshot.getAvgProcessTime();
				errCount += workerSnapshot.getErrCount();
			}
			if (null != startTime) {
				jobSnapshot.setStartTime(DateFormatUtils.format(startTime, DateFormats.DATE_FORMAT_1));
			}
			if (null != endTime) {
				jobSnapshot.setEndTime(DateFormatUtils.format(endTime, DateFormats.DATE_FORMAT_1));
			}
			jobSnapshot.setTotalProcessCount(totalProcessCount);
			jobSnapshot.setTotalResultCount(totalResultCount);
			jobSnapshot.setTotalProcessTime(totalProcessTime / workerSnapshots.size());
			jobSnapshot.setMaxProcessTime(maxProcessTime);
			jobSnapshot.setMinProcessTime(minProcessTime);
			jobSnapshot.setAvgProcessTime(avgProcessTime / workerSnapshots.size());
			jobSnapshot.setErrCount(errCount);
		}
	}

	public void registerJobSnapshot(JobSnapshot jobSnapshot) {
		jobSnapshotDao.save(jobSnapshot);
		registerCenter.registerJobSnapshot(jobSnapshot);
	}

	@Override
	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		jobSnapshotDao.update(jobSnapshot);
		registerCenter.updateJobSnapshot(jobSnapshot);
	}


	@Override
	public void updateWorkSnapshotToRegisterCenter(WorkerSnapshot workerSnapshot, boolean isSaveErrMsg) {
		List<WorkerErrMsg> errMsgs = workerSnapshot.getWorkerErrMsgs();
		if (null != errMsgs && ((isSaveErrMsg && errMsgs.size() > 0) || errMsgs.size() >= SAVE_ERR_MSG_MAX)) {
			workerErrMsgDao.batchSave(errMsgs);
			errMsgs.clear();
		}
		registerCenter.updateWorkerSnapshot(workerSnapshot);
	}

	private QueueInfo getJobQueueInfos(String queueName) {
		QueueInfo tempQueueInfo = new QueueInfo();
		tempQueueInfo.setQueueName(queueName);
		String tempProxyKey = RedisWorkQueue.PRE_PROXY_QUEUE_KEY + queueName;
		int proxySize = redisManager.llen(tempProxyKey);
		tempQueueInfo.setProxyQueueSize(proxySize);

		String tempRealKey = RedisWorkQueue.PRE_QUEUE_KEY + queueName;
		int realSize = redisManager.hllen(tempRealKey);
		tempQueueInfo.setRealQueueSize(realSize);

		String tempErrKey = RedisWorkQueue.PRE_ERR_QUEUE_KEY + queueName;
		int errSize = redisManager.hllen(tempErrKey);
		tempQueueInfo.setErrQueueCount(errSize);

		return tempQueueInfo;
	}

	@Override
	public ResponseEntity<InputStreamResource> download(String param) {
		JobProfile profile = new JobProfile();
		Job job = queryJob(param);
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
			LOG.error("downloadProfile err:" + param, e);
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
				LOG.error(msg, e);
			}
		} else {
			msg = "uploadJobProfile is empty";
		}
		return msg;
	}

	@Override
	public List<WorkerSnapshot> getWorkSnapshotsFromRegisterCenter(String nodeName, String jobName) {
		List<WorkerSnapshot> result = registerCenter.getWorkerSnapshots(nodeName, jobName);
		return result;
	}

	@Override
	public List<ExtractItem> queryExtractItems(String jobName) {
		List<ExtractItem> result = extractItemDao.query(jobName);
		return result;
	}

	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public AbstractSchedulerManager getCommonScheduleManager() {
		return commonScheduleManager;
	}

	public void setCommonScheduleManager(AbstractSchedulerManager commonScheduleManager) {
		this.commonScheduleManager = commonScheduleManager;
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

	public RegisterCenter getRegisterCenter() {
		return registerCenter;
	}

	public void setRegisterCenter(RegisterCenter registerCenter) {
		this.registerCenter = registerCenter;
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

}
