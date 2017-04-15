package six.com.crawler.admin.service;

import java.util.List;

import six.com.crawler.node.Node;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月24日 下午9:32:27 
*/
public interface ClusterManagerService {

	List<Node> getClusterInfo();
	
	Node getCurrentNode();
	
}
