package six.com.crawler.node;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月15日 下午2:20:33
 */
public class CuratorFrameworkHelper {

	final static Logger log = LoggerFactory.getLogger(CuratorFrameworkHelper.class);

	public static CuratorFramework newCuratorFramework(String connectStr, String clusterName, RetryPolicy retryPolicy) {
		CuratorFramework curatorFramework = null;
		curatorFramework = CuratorFrameworkFactory.newClient(connectStr, retryPolicy);
		curatorFramework.start();
		try {
			curatorFramework.blockUntilConnected();
		} catch (InterruptedException e) {
			log.error("connect zooKeeper[" + connectStr + "] err", e);
		}
		try {
			/**
			 * 初始化应用基本目录
			 */
			Stat stat = curatorFramework.checkExists().forPath(ZooKeeperPathUtils.getRootPath());
			if (null == stat) {
				curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath(ZooKeeperPathUtils.getRootPath());
			}
			/**
			 * 初始化集群基本目录
			 */
			stat = curatorFramework.checkExists().forPath(ZooKeeperPathUtils.getClusterRootPath(clusterName));
			if (null == stat) {
				curatorFramework.create().withMode(CreateMode.PERSISTENT)
						.forPath(ZooKeeperPathUtils.getClusterRootPath(clusterName));
			}
			/**
			 * 初始化master node节点目录
			 */
			stat = curatorFramework.checkExists().forPath(ZooKeeperPathUtils.getMasterNodesPath(clusterName));
			if (null == stat) {
				curatorFramework.create().withMode(CreateMode.PERSISTENT)
						.forPath(ZooKeeperPathUtils.getMasterNodesPath(clusterName));
			}

			/**
			 * 初始化master stand node目录
			 */
			stat = curatorFramework.checkExists().forPath(ZooKeeperPathUtils.getMasterStandbyNodesPath(clusterName));
			if (null == stat) {
				curatorFramework.create().withMode(CreateMode.PERSISTENT)
						.forPath(ZooKeeperPathUtils.getMasterStandbyNodesPath(clusterName));
			}

			/**
			 * 初始化worker node目录
			 */
			stat = curatorFramework.checkExists().forPath(ZooKeeperPathUtils.getWorkerNodesPath(clusterName));
			if (null == stat) {
				curatorFramework.create().withMode(CreateMode.PERSISTENT)
						.forPath(ZooKeeperPathUtils.getWorkerNodesPath(clusterName));
			}

			/**
			 * 初始化集群锁目录
			 */
			stat = curatorFramework.checkExists().forPath(ZooKeeperPathUtils.getDistributedLocksPath(clusterName));
			if (null == stat) {
				curatorFramework.create().withMode(CreateMode.PERSISTENT)
						.forPath(ZooKeeperPathUtils.getDistributedLocksPath(clusterName));
			}
		} catch (Exception e) {
			log.error("init zooKeeper's persistent path err", e);
		}
		return curatorFramework;
	}
}
