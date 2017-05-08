package six.com.crawler.schedule.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月14日 下午5:00:51
 */
public class RedisScheduleCache implements ScheduleCache {

	private RedisManager redisManager;

	private RedisCacheKeyHelper redisCacheKeyHelper;

	public RedisScheduleCache(RedisManager redisManager, String basePreKey) {
		this.redisManager = redisManager;
		redisCacheKeyHelper = new RedisCacheKeyHelper(basePreKey);
	}

	@Override
	public Job getJob(String jobName) {
		String jobskey = redisCacheKeyHelper.getJobsKey();
		Job job = getRedisManager().hget(jobskey, jobName, Job.class);
		return job;
	}

	@Override
	public void setJob(Job job) {
		String jobskey = redisCacheKeyHelper.getJobsKey();
		getRedisManager().hset(jobskey, job.getName(), job);
	}

	@Override
	public void delJob(String jobName) {
		String jobskey = redisCacheKeyHelper.getJobsKey();
		getRedisManager().hdel(jobskey, jobName);
	}

	@Override
	public JobSnapshot getJobSnapshot(String jobName) {
		String jobSnapshotskeyskey = redisCacheKeyHelper.getJobSnapshotsKey();
		JobSnapshot jobSnapshot = getRedisManager().hget(jobSnapshotskeyskey, jobName, JobSnapshot.class);
		return jobSnapshot;
	}

	@Override
	public List<JobSnapshot> getJobSnapshots() {
		String jobSnapshotskeyskey = redisCacheKeyHelper.getJobSnapshotsKey();
		Map<String, JobSnapshot> findMap = getRedisManager().hgetAll(jobSnapshotskeyskey, JobSnapshot.class);
		return new ArrayList<>(findMap.values());
	}

	@Override
	public void setJobSnapshot(JobSnapshot jobSnapshot) {
		String jobName = jobSnapshot.getName();
		String jobSnapshotskey = redisCacheKeyHelper.getJobSnapshotsKey();
		String workerkey = redisCacheKeyHelper.getWorkerSnapshotsKey(jobName);
		String jobWorkerSerialNumberkey = redisCacheKeyHelper.getWorkerSerialNumbersKey(jobName);
		getRedisManager().lock(jobSnapshotskey);
		try {
			// 先删除 过期job信息
			getRedisManager().hdel(jobSnapshotskey, jobName);
			// 先删除 过期jobWorkerSerialNumberkey信息
			getRedisManager().del(jobWorkerSerialNumberkey);
			// 先删除 过期workerkey 信息
			getRedisManager().del(workerkey);
			// 注册JobSnapshot
			updateJobSnapshot(jobSnapshot);
		} finally {
			getRedisManager().unlock(jobSnapshotskey);
		}
	}

	@Override
	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		String jobSnapshotskey = redisCacheKeyHelper.getJobSnapshotsKey();
		getRedisManager().hset(jobSnapshotskey, jobSnapshot.getName(), jobSnapshot);
	}

	@Override
	public void delJobSnapshot(String jobName) {
		String jobSnapshotskey = redisCacheKeyHelper.getJobSnapshotsKey();
		getRedisManager().hdel(jobSnapshotskey, jobName);
	}

	@Override
	public String newWorkerNameByJob(String jobName, String currentNodeName) {
		String key = redisCacheKeyHelper.getWorkerSerialNumbersKey(jobName);
		Long sernum = getRedisManager().incr(key);
		int serialNumber = sernum.intValue();
		StringBuilder sbd = new StringBuilder();
		sbd.append(jobName).append("_");
		sbd.append("worker_");
		sbd.append(currentNodeName).append("_");
		sbd.append(serialNumber);
		return sbd.toString();
	}

	@Override
	public List<WorkerSnapshot> getWorkerSnapshots(String jobName) {
		String workerSnapshotkey = redisCacheKeyHelper.getWorkerSnapshotsKey(jobName);
		List<WorkerSnapshot> workerSnapshots = new ArrayList<>();
		Map<String, WorkerSnapshot> workerInfosMap = getRedisManager().hgetAll(workerSnapshotkey, WorkerSnapshot.class);
		workerSnapshots.addAll(workerInfosMap.values());
		return workerSnapshots;
	}

	@Override
	public void setWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		updateWorkerSnapshot(workerSnapshot);
	}

	@Override
	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		String workerSnapshotKey = redisCacheKeyHelper.getWorkerSnapshotsKey(workerSnapshot.getJobName());
		getRedisManager().hset(workerSnapshotKey, workerSnapshot.getName(), workerSnapshot);
	}

	@Override
	public void delWorkerSnapshots(String jobName) {
		String WorkerSnapshotkey = redisCacheKeyHelper.getWorkerSnapshotsKey(jobName);
		getRedisManager().del(WorkerSnapshotkey);
	}

	@Override
	public void setJobParam(String jobName, String paramKey, String param) {
		String jobParamKey = redisCacheKeyHelper.getJobParamKey(jobName);
		getRedisManager().hset(jobParamKey, paramKey, param);
	}

	@Override
	public String getJobParam(String jobName, String paramKey) {
		String jobParamKey = redisCacheKeyHelper.getJobParamKey(jobName);
		String jobParam = getRedisManager().hget(jobParamKey, paramKey, String.class);
		return jobParam;
	}

	@Override
	public Map<String, String> getJobParams(String jobName) {
		String jobParamKey = redisCacheKeyHelper.getJobParamKey(jobName);
		Map<String, String> jobParams = getRedisManager().hgetAll(jobParamKey, String.class);
		return jobParams;
	}

	@Override
	public void delJobParam(String jobName) {
		String jobParamKey = redisCacheKeyHelper.getJobParamKey(jobName);
		getRedisManager().del(jobParamKey);
	}

	@Override
	public void clear() {
		String patternKey = redisCacheKeyHelper.getResetPreKey() + "*";
		Set<String> keys = getRedisManager().keys(patternKey);
		for (String key : keys) {
			getRedisManager().del(key);
		}
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}
}
