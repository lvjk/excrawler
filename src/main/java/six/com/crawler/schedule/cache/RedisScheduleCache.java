package six.com.crawler.schedule.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.dao.RedisManager;
import six.com.crawler.entity.Job;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.WorkerSnapshot;
import six.com.crawler.node.ClusterManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月14日 下午5:00:51
 */
@Component
public class RedisScheduleCache implements ScheduleCache, InitializingBean {

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private ClusterManager clusterManager;

	private RedisCacheKeyHelper redisCacheKeyHelper;

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

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String clusterName = clusterManager.getClusterName();
		String basePreKey = clusterName + "_scheduler_cache";
		redisCacheKeyHelper = new RedisCacheKeyHelper(basePreKey);
	}
}
