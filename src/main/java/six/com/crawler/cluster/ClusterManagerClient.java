package six.com.crawler.cluster;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.entity.Node;
import six.com.crawler.common.utils.JsonUtils;
import six.com.crawler.schedule.RemotingClient;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午4:01:29
 */
@Component
public class ClusterManagerClient extends RemotingClient {

	@Autowired
	private ClusterManager clusterManager;

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	@SuppressWarnings("unchecked")
	public Node getCurrentNode(Node targetNode) {
		String path = "/crawler/cluster/getCurrentNode";
		String json = doExecute(targetNode, path);
		Map<String, Object> map = JsonUtils.toObject(json, Map.class);
		Map<String, Object> nodeMap = (Map<String, Object>) map.get("data");
		Node node = JsonUtils.mapToObject(nodeMap, Node.class);
		return node;
	}
}
