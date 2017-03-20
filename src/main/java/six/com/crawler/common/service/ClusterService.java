package six.com.crawler.common.service;

import java.util.List;

import six.com.crawler.common.entity.Node;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月24日 下午9:32:27 
*/
public interface ClusterService {

	List<Node> getClusterInfo();
	
	Node getCurrentNode();
	
}
