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
import six.com.crawler.common.entity.Node;
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
	public void repair() {
		String patternKey = RedisRegisterKeyUtils.getResetPreKey() + "*";
		Set<String> keys = redisManager.keys(patternKey);
		for (String key : keys) {
			redisManager.del(key);
		}
	}

	/**
	 * 通过nodeName获取节点
	 * 
	 * @return
	 */
	public Node getMasterNode() {
		String nodesKey = RedisRegisterKeyUtils.getMasterNodePreKey();
		Node masterNode = redisManager.get(nodesKey, Node.class);
		return masterNode;
	}

	/**
	 * 注册节点
	 * 
	 * @param node
	 * @param hearbeat
	 *            节点信息的有效期 秒
	 */
	public void registerMasterNode(Node masterNode) {
		String nodesKey = RedisRegisterKeyUtils.getMasterNodePreKey();
		redisManager.set(nodesKey, masterNode);
	}

	public Node getNode(String nodeName) {
		String nodesKey = RedisRegisterKeyUtils.getWorkerNodeKey(nodeName);
		Node getNode = redisManager.get(nodesKey, Node.class);
		return getNode;
	}

	@Override
	public List<Node> getNodes() {
		String nodesPreKey = RedisRegisterKeyUtils.getWorkerNodesPreKey()+ "*";
		Set<String> keys = redisManager.keys(nodesPreKey);
		List<Node> result = new ArrayList<>();
		for (String key : keys) {
			Node node = redisManager.get(key, Node.class);
			result.add(node);
		}
		return result;
	}

	public void registerNode(Node node,int heartbeat) {
		String nodesKey = RedisRegisterKeyUtils.getWorkerNodeKey(node.getName());
		redisManager.set(nodesKey, node,heartbeat);
	}

	public void delNode(String nodeName) {
		String nodesKey = RedisRegisterKeyUtils.getWorkerNodeKey(nodeName);
		redisManager.del(nodesKey);
	}

	public JobSnapshot getJobSnapshot(String jobName) {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		JobSnapshot jobSnapshot = redisManager.hget(jobSnapshotskeyskey, jobName, JobSnapshot.class);
		return jobSnapshot;
	}

	public List<JobSnapshot> getJobSnapshots() {
		String jobSnapshotskeyskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		Map<String, JobSnapshot> findMap = redisManager.hgetAll(jobSnapshotskeyskey, JobSnapshot.class);
		return new ArrayList<>(findMap.values());
	}

	public void registerJobSnapshot(JobSnapshot jobSnapshot) {
		String jobName = jobSnapshot.getName();
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		String workerkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		String jobWorkerSerialNumberkey = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(jobName);
		redisManager.lock(jobSnapshotskey);
		try {
			// 先删除 过期job信息
			redisManager.hdel(jobSnapshotskey, jobName);
			// 先删除 过期jobWorkerSerialNumberkey信息
			redisManager.del(jobWorkerSerialNumberkey);
			// 先删除 过期workerkey 信息
			redisManager.del(workerkey);
			// 注册JobSnapshot
			updateJobSnapshot(jobSnapshot);
		} finally {
			redisManager.unlock(jobSnapshotskey);
		}
	}

	public void updateJobSnapshot(JobSnapshot jobSnapshot) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		redisManager.lock(jobSnapshotskey);
		try {
			redisManager.hset(jobSnapshotskey, jobSnapshot.getName(), jobSnapshot);
		} finally {
			redisManager.unlock(jobSnapshotskey);
		}
	}

	public void delJobSnapshot(String jobName) {
		String jobSnapshotskey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		redisManager.hdel(jobSnapshotskey, jobName);
	}

	public List<WorkerSnapshot> getWorkerSnapshots(String jobName) {
		String workerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
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
		String workerSnapshotsKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobName());
		redisManager.lock(workerSnapshotsKey);
		try {
			redisManager.hset(workerSnapshotsKey, workerSnapshot.getName(), workerSnapshot);
		} finally {
			redisManager.unlock(workerSnapshotsKey);
		}
	}

	public void updateWorkerSnapshot(WorkerSnapshot workerSnapshot) {
		String workerSnapshotKey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(workerSnapshot.getJobName());
		redisManager.hset(workerSnapshotKey, workerSnapshot.getName(), workerSnapshot);
	}

	public boolean workerIsAllWaited(String jobName) {
		List<WorkerSnapshot> workerSnapshots = getWorkerSnapshots(jobName);
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

	
	public void delWorkerSnapshots(String jobName){
		String Jobkey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		String WorkerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		redisManager.lock(Jobkey);
		try {
			redisManager.del(WorkerSnapshotkey);
		} finally {
			redisManager.unlock(Jobkey);
		}
	}

	public void delWorker1(String jobName, String workerName) {
		String Jobkey = RedisRegisterKeyUtils.getJobSnapshotsKey();
		String WorkerSnapshotkey = RedisRegisterKeyUtils.getWorkerSnapshotsKey(jobName);
		String serialNumberkey = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(jobName);
		redisManager.lock(Jobkey);
		try {
			// 删除注册在redis上的workinfo
			redisManager.hdel(WorkerSnapshotkey, workerName);
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
	public int getSerNumOfWorkerByJob(String jobName) {
		String key = RedisRegisterKeyUtils.getWorkerSerialNumbersKey(jobName);
		Long sernum = redisManager.getJedisCluster().incr(key);
		return sernum.intValue();
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

}
