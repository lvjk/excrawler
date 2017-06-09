package six.com.crawler.work.space;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.dao.RedisManager;
import six.com.crawler.node.ClusterManager;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月29日 下午5:20:09
 */
@Component
public class WorkSpaceManager {

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private ClusterManager clusterManager;

	private static final String WORKSPACE_PRE = "workspace_";

	/**
	 * 新建一个工作空间
	 * 
	 * @param workSpaceName
	 *            工作空间名称
	 * @param clz
	 *            工作空间 处理的数据class
	 * @return
	 */
	public <T extends WorkSpaceData> WorkSpace<T> newWorkSpace(String workSpaceName, Class<T> clz) {
		DistributedLock distributedLock = clusterManager.getDistributedLock(WORKSPACE_PRE + workSpaceName);
		WorkSpace<T> workQueue = new SegmentRedisWorkSpace<>(redisManager, distributedLock, workSpaceName, clz);
		return workQueue;
	}

	/**
	 * 获取所有的工作空间
	 * 
	 * @return
	 */
	public List<WorkSpace<WorkSpaceData>> getAllWorkSpaces() {
		List<WorkSpace<WorkSpaceData>> allWorkSpace = new ArrayList<>();
		Set<String> allWorkSpaceName = new HashSet<>();

		Set<String> alldoingKeys = redisManager.keys(SegmentRedisWorkSpace.SEGMENT_DOING_MAP_NAME_KEYS + "*");
		String findWorkSpaceName = null;
		for (String fullWorkQueueName : alldoingKeys) {
			findWorkSpaceName = StringUtils.remove(fullWorkQueueName,
					SegmentRedisWorkSpace.SEGMENT_DOING_MAP_NAME_KEYS);
			allWorkSpaceName.add(findWorkSpaceName);
		}
		Set<String> allDoneKeys = redisManager.keys(SegmentRedisWorkSpace.SEGMENT_DONE_MAP_NAME_KEYS + "*");
		for (String fullDoneName : allDoneKeys) {
			findWorkSpaceName = StringUtils.remove(fullDoneName, SegmentRedisWorkSpace.SEGMENT_DONE_MAP_NAME_KEYS);
			allWorkSpaceName.add(findWorkSpaceName);
		}

		Set<String> allErrKeys = redisManager.keys(SegmentRedisWorkSpace.ERR_QUEUE_KEY_PRE + "*");
		for (String fullDoneName : allErrKeys) {
			findWorkSpaceName = StringUtils.remove(fullDoneName, SegmentRedisWorkSpace.ERR_QUEUE_KEY_PRE);
			allWorkSpaceName.add(findWorkSpaceName);
		}

		WorkSpace<WorkSpaceData> redisWorkSpace = null;
		for (String workSpaceName : allWorkSpaceName) {
			redisWorkSpace = newWorkSpace(workSpaceName, WorkSpaceData.class);
			allWorkSpace.add(redisWorkSpace);
		}
		return allWorkSpace;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
}
