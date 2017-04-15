package six.com.crawler.schedule.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerSnapshot;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月14日 下午5:00:51
 */
@Component
public class RedisScheduleCache implements ScheduleCache {

	@Autowired
	private RedisManager redisManager;

	@Override
	public Job getJob(String jobName) {
		String jobskey = RedisCacheKeyUtils.getJobsKey();
		Job job = getRedisManager().hget(jobskey, jobName, Job.class);
		return job;
	}

	@Override
	public void setJob(Job job) {
		String jobskey = RedisCacheKeyUtils.getJobsKey();
		getRedisManager().hset(jobskey, job.getName(), job);
	}

	@Override
	public void delJob(String jobName) {
		String jobskey = RedisCacheKeyUtils.getJobsKey();
		getRedisManager().hdel(jobskey, jobName);
	}

	@Override
	public JobSnapshot getJobSnapshot(String jobName) {
		String jobSnapshotskeyskey = RedisCacheKeyUtils.getJobSnapshotsKey();
		JobSnapshot jobSnapshot = getRedisManager().hget(jobSnapshotskeyskey, jobName, JobSnapshot.class);
		return jobSnapshot;
	}

	@Override
	public List<JobSnapshot> getJobSnapshots() {
		String jobSnapshotskeyskey = RedisCacheKeyUtils.getJobSnapshotsKey();
		Map<String, JobSnapshot> findMap = getRedisManager().hgetAll(jobSnapshotskeyskey, JobSnapshot.class);
		return new ArrayList<>(findMap.values());
	}

	@Override
	public void setJobSnapshot(JobSnapshot jobSnapshot) {
		String jobName = jobSnapshot.getName();
		String jobSnapshotskey = RedisCacheKeyUtils.getJobSnapshotsKey();
		String workerkey = RedisCacheKeyUtils.getWorkerSnapshotsKey(jobName);
		String jobWorkerSerialNumberkey = RedisCacheKeyUtils.getWorkerSerialNumbersKey(jobName);
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
		String jobSnapshotskey = RedisCacheKeyUtils.getJobSnapshotsKey();
		getRedisManager().hset(jobSnapshotskey, jobSnapshot.getName(), jobSnapshot);
	}

	@Override
	public void delJobSnapshot(String jobName) {
		String jobSnapshotskey = RedisCacheKeyUtils.getJobSnapshotsKey();
		getRedisManager().hdel(jobSnapshotskey, jobName);
	}

	@Override
	public List<WorkerSnapshot> getWorkerSnapshots(String jobName) {
		String workerSnapshotkey = RedisCacheKeyUtils.getWorkerSnapshotsKey(jobName);
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
		String workerSnapshotKey = RedisCacheKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobName());
		getRedisManager().hset(workerSnapshotKey, workerSnapshot.getName(), workerSnapshot);
	}

	@Override
	public void delWorkerSnapshots(String jobName) {
		String WorkerSnapshotkey = RedisCacheKeyUtils.getWorkerSnapshotsKey(jobName);
		getRedisManager().del(WorkerSnapshotkey);
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

}
