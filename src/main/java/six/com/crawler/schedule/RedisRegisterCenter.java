package six.com.crawler.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.entity.JobSnapshot;
import six.com.crawler.common.entity.WorkerSnapshot;
import six.com.crawler.work.Worker;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月22日 上午11:16:25
 */
@Component
public class RedisRegisterCenter implements RegisterCenter {

	final static Logger LOG = LoggerFactory.getLogger(RedisRegisterCenter.class);

	private Map<String, Map<String, Worker>> allJobWorkerMap = new ConcurrentHashMap<String, Map<String, Worker>>();

	@Autowired
	private RedisManager redisManager;

	@Override
	public void reset(String nodeName) {
		String patternKey = RedisRegisterKeyUtils.getResetPreKey(nodeName) + "*";
		Set<String> keys = redisManager.keys(patternKey);
		for (String key : keys) {
			redisManager.del(key);
		}
	}

	public Node getNode(String nodeName) {
		String nodesKey = RedisRegisterKeyUtils.getNodesKey(nodeName);
		Node getNode = redisManager.get(nodesKey, Node.class);
		return getNode;
	}

	@Override
	public List<Node> getNodes() {
		String nodesPreKey = RedisRegisterKeyUtils.getNodesPreKey() + "*";
		Set<String> keys = redisManager.keys(nodesPreKey);
		List<Node> result = new ArrayList<>();
		for (String key : keys) {
			Node node = redisManager.get(key, Node.class);
			result.add(node);
		}
		return result;
	}

	public void registerNode(Node node, int hearbeat) {
		String nodesKey = RedisRegisterKeyUtils.getNodesKey(node.getName());
		redisManager.set(nodesKey, node, hearbeat);
	}

	public void delNode(String nodeName) {
		String nodesKey = RedisRegisterKeyUtils.getNodesKey(nodeName);
		redisManager.del(nodesKey);
	}

	public JobSnapshot getJobSnapshot(String nodeName, String jobName) {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey(nodeName);
		JobSnapshot jobSnapshot = redisManager.hget(jobSnapshotskeyskey, jobName, JobSnapshot.class);
		return jobSnapshot;
	}

	public List<JobSnapshot> getJobSnapshots(String nodeName) {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey(nodeName);
		Map<String, JobSnapshot> findMap = redisManager.hgetAll(jobSnapshotskeyskey, JobSnapshot.class);
		return new ArrayList<>(findMap.values());
	}

	public void registerJobSnapshot(JobSnapshot jobSnapshot) {
		String nodeName = jobSnapshot.getHostNode();
		String jobName = jobSnapshot.getName();
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey(nodeName);
		String workerkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(nodeName, jobName);
		String jobWorkerSerialNumberkey = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(nodeName, jobName);
		redisManager.lock(jobSnapshotskey);
		try {
			// 先删除 过期job信息
			redisManager.hdel(jobSnapshotskey, jobName);
			// 先删除 过期jobWorkerSerialNumberkey信息
			redisManager.del(jobWorkerSerialNumberkey);
			// 先删除 过期workerkey 信息
			redisManager.del(workerkey);
			// 先删除 过期本地map worker信息
			getJobsWorkerMap(jobName).clear();
			// 注册JobSnapshot
			updateJobSnapshot(jobSnapshot);
		} finally {
			redisManager.unlock(jobSnapshotskey);
		}
	}

	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey(jobSnapshot.getHostNode());
		redisManager.lock(jobSnapshotskey);
		try {
			redisManager.hset(jobSnapshotskey, jobSnapshot.getName(), jobSnapshot);
		} finally {
			redisManager.unlock(jobSnapshotskey);
		}
	}

	public void delJobSnapshot(String nodeName, String jobName) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey(nodeName);
		redisManager.hdel(jobSnapshotskey, jobName);
	}

	public List<WorkerSnapshot> getWorkerSnapshots(String nodeName, String jobName) {
		String workerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(nodeName, jobName);
		redisManager.lock(workerSnapshotkey);
		List<WorkerSnapshot> workerSnapshots = new ArrayList<>();
		try {
			Map<String, WorkerSnapshot> workerInfosMap = redisManager.hgetAll(workerSnapshotkey, WorkerSnapshot.class);
			workerSnapshots.addAll(workerInfosMap.values());
		} finally {
			redisManager.unlock(workerSnapshotkey);
		}
		return workerSnapshots;
	}

	public List<Worker> getWorkers(String jobName) {
		return new ArrayList<>(getJobsWorkerMap(jobName).values());
	}

	public Worker getWorker(String jobName, String workerName) {
		return getJobsWorkerMap(jobName).get(workerName);
	}

	public List<Worker> getLocalWorkers() {
		List<Worker> result = new ArrayList<>();
		Collection<Map<String, Worker>> all = allJobWorkerMap.values();
		for (Map<String, Worker> map : all) {
			result.addAll(map.values());
		}
		return result;
	}

	@Override
	public void registerWorker(Worker worker) {
		WorkerSnapshot workerSnapshot = worker.getWorkerSnapshot();
		String workerSnapshotsKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobHostNode(),
				workerSnapshot.getJobName());
		redisManager.lock(workerSnapshotsKey);
		try {
			redisManager.hset(workerSnapshotsKey, workerSnapshot.getName(), workerSnapshot);
			getJobsWorkerMap(workerSnapshot.getJobName()).put(worker.getName(), worker);
		} finally {
			redisManager.unlock(workerSnapshotsKey);
		}
	}

	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		String workerSnapshotKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobHostNode(),
				workerSnapshot.getJobName());
		redisManager.hset(workerSnapshotKey, workerSnapshot.getName(), workerSnapshot);
	}

	public boolean workerIsAllWaited(String nodeName, String jobName) {
		List<WorkerSnapshot> workerSnapshots = getWorkerSnapshots(nodeName, jobName);
		boolean result = true;
		for (WorkerSnapshot workerSnapshot : workerSnapshots) {
			// 判断其他工人是否还在运行
			if (workerSnapshot.getState() != WorkerLifecycleState.WAITED) {
				result = false;
				break;
			}
		}
		return result;
	}

	public void delWorker(String nodeName, String jobName, String workerName) {
		String Jobkey = RedisRegisterKeyUtils.getJobSnapshotsKey(nodeName);
		String WorkerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(nodeName, jobName);
		String serialNumberkey = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(nodeName, jobName);
		redisManager.lock(Jobkey);
		try {
			// 删除注册在redis上的workinfo
			redisManager.hdel(WorkerSnapshotkey, workerName);
			// 删除本地缓存里的 worker 实例
			getJobsWorkerMap(jobName).remove(workerName);
			// 然后检查此job的worker size是否==0 如果是那么清理此job信息
			if (redisManager.hgetAll(WorkerSnapshotkey, WorkerSnapshot.class).isEmpty()) {
				// 先删除 worker过期信息
				redisManager.del(WorkerSnapshotkey);
				// 先删除 worker serialNumberkey 过期信息
				redisManager.del(serialNumberkey);
				// 先删除 过期信息
				redisManager.hdel(Jobkey, jobName);
				// 先删除 过期信息
				allJobWorkerMap.remove(jobName);
			}
		} finally {
			redisManager.unlock(Jobkey);
		}
	}

	@Override
	public int getSerNumOfWorkerByJob(String nodeName, String jobName) {
		String key = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(nodeName, jobName);
		Long sernum = redisManager.getJedisCluster().incr(key);
		return sernum.intValue();
	}

	private Map<String, Worker> getJobsWorkerMap(String jobName) {
		Map<String, Worker> jobsWorkerMap = allJobWorkerMap.get(jobName);
		if (null == jobsWorkerMap) {
			jobsWorkerMap = new ConcurrentHashMap<>();
			allJobWorkerMap.put(jobName, jobsWorkerMap);
		}
		return jobsWorkerMap;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

}
