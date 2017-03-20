package six.com.crawler.cluster;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月16日 上午11:44:14
 */
public class ZooKeeperPathUtils {

	public static String EXCRAWLER_ROOT_PATH = "/excrawler";

	public static String getRootPath() {
		return EXCRAWLER_ROOT_PATH;
	}

	public static String getMasterNodesPath() {
		return EXCRAWLER_ROOT_PATH + "/masternode";
	}
	
	public static String getMasterNodePath(String masterNodeName) {
		return EXCRAWLER_ROOT_PATH + "/masternode/"+masterNodeName;
	}

	public static String getMasterStandbyNodesPath() {
		return EXCRAWLER_ROOT_PATH + "/masterstandbynode";
	}
	
	public static String getMasterStandbyNodePath(String masterstandbynodeName) {
		return EXCRAWLER_ROOT_PATH + "/masterstandbynode/"+masterstandbynodeName;
	}

	public static String getWorkerNodesPath() {
		return EXCRAWLER_ROOT_PATH + "/workernode";
	}

	public static String getWorkerNodePath(String nodeName) {
		return getWorkerNodesPath() + "/" + nodeName;
	}
}
