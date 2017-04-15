package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.service.ClusterManagerService;
import six.com.crawler.node.Node;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月24日 下午9:25:33 
* 
* 
* 
* 
* 
* 
*/

@Controller
public class ClusterApi extends BaseApi{

	@Autowired
	private ClusterManagerService clusterService;
	
	@RequestMapping(value = "/crawler/cluster/info", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Node>> getClusterInfo() {
		ResponseMsg<List<Node>> msg = createResponseMsg();
		List<Node> result = clusterService.getClusterInfo();
		msg.setData(result);
		return msg;
	} 
	
	@RequestMapping(value = "/crawler/cluster/getCurrentNode", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Node> getCurrentNode() {
		ResponseMsg<Node> msg = createResponseMsg();
		Node currentNode = clusterService.getCurrentNode();
		msg.setData(currentNode);
		return msg;
	} 
	
	
	public ClusterManagerService getClusterService() {
		return clusterService;
	}

	public void setClusterService(ClusterManagerService clusterService) {
		this.clusterService = clusterService;
	}
}
