package six.com.crawler.work.space;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.dao.RedisManager;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月29日 下午5:20:09
 */
@Component
public class WorkSpaceManager {

	@Autowired
	private RedisManager redisManager;

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
		//TODO 需要引用集群名作为base前缀key
		WorkSpace<T> workQueue = new RedisWorkSpace<>(redisManager, workSpaceName, clz);
		return workQueue;
	}

	/**
	 * 获取所有的工作空间
	 * 
	 * @return
	 */
	public List<WorkSpace<WorkSpaceData>> getAllWorkSpaces() {
		List<WorkSpace<WorkSpaceData>> allWorkSpace = new ArrayList<>();
		Set<String> allWorkSpaceName=new HashSet<>();
		Set<String> allWorkQueueKeys = redisManager.keys(RedisWorkSpace.WORK_QUEUE_KEY_PRE + "*");
		String findWorkSpaceName = null;
		WorkSpace<WorkSpaceData> redisWorkSpace = null;
		for (String fullWorkQueueName : allWorkQueueKeys) {
			findWorkSpaceName = StringUtils.remove(fullWorkQueueName, RedisWorkSpace.WORK_QUEUE_KEY_PRE);
			allWorkSpaceName.add(findWorkSpaceName);
		}
		Set<String> allDoneQueueKeys = redisManager.keys(RedisWorkSpace.WORK_DONE_QUEUE_KEY_PRE + "*");
		for (String fullDoneName : allDoneQueueKeys) {
			findWorkSpaceName = StringUtils.remove(fullDoneName, RedisWorkSpace.WORK_DONE_QUEUE_KEY_PRE);
			allWorkSpaceName.add(findWorkSpaceName);
		}
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
}
