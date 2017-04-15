package six.com.crawler.node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 上午11:44:14
 */
public class ZooKeeperPathUtils {

	public static String EXCRAWLER_ROOT_PATH = "/excrawler";

	public static String EXCRAWLER_CLUSTER_ROOT_PATH = "/excrawler";

	public static String getRootPath() {
		return EXCRAWLER_ROOT_PATH;
	}

	public static String getClusterRootPath(String clusterName) {
		return EXCRAWLER_ROOT_PATH + "/" + clusterName;
	}

	public static String getMasterNodesPath(String clusterName) {
		return getClusterRootPath(clusterName) + "/masternode";
	}

	public static String getMasterNodePath(String clusterName, String masterNodeName) {
		return getMasterNodesPath(clusterName) + "/" + masterNodeName;
	}

	public static String getMasterStandbyNodesPath(String clusterName) {
		return getClusterRootPath(clusterName) + "/masterstandbynode";
	}

	public static String getMasterStandbyNodePath(String clusterName, String masterstandbynodeName) {
		return getMasterStandbyNodesPath(clusterName) + "/" + masterstandbynodeName;
	}

	public static String getWorkerNodesPath(String clusterName) {
		return getClusterRootPath(clusterName) + "/workernode";
	}

	public static String getWorkerNodePath(String clusterName, String nodeName) {
		return getWorkerNodesPath(clusterName) + "/" + nodeName;
	}

	public static String getDistributedLocksPath(String clusterName) {
		return getClusterRootPath(clusterName) + "/lock";
	}

	public static String getDistributedLockPath(String clusterName, String lockPath) {
		return getDistributedLocksPath(clusterName) + "/" + lockPath;
	}
}
