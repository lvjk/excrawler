package six.com.crawler.node;

import java.util.List;
import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Maps;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月8日 下午4:12:26
 */
public class LocalNodeRegister implements NodeRegister {

	private Node master;
	private Map<String, Node> localWorkerNodes = Maps.newConcurrentMap();


	@Override
	public Node getMaster() {
		return master;
	}

	@Override
	public List<Node> getWorkerNodes() {
		return Lists.newArrayList(localWorkerNodes.values());
	}

	@Override
	public Node getWorkerNode(String nodeName) {
		return localWorkerNodes.get(nodeName);
	}

	@Override
	public void registerMaster(Node node) {
		this.master=node;
	}

	@Override
	public void registerWorker(Node node) {
		localWorkerNodes.put(node.getName(), node);
	}

}
