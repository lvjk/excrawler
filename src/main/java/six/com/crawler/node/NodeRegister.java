package six.com.crawler.node;

import java.util.List;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年6月8日 下午4:13:09
 */
public interface NodeRegister {

	Node getMaster();
	
	List<Node> getWorkerNodes();

	Node getWorkerNode(String nodeName);

	void registerMaster(Node node);
	
	void registerWorker(Node node);

}
