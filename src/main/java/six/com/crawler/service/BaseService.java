package six.com.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.api.ResponseMsg;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月30日 上午9:25:08 
* 
* 对外提供service 基础类
*/
public abstract class BaseService {

	@Autowired
	private NodeManagerService clusterService;

	public NodeManagerService getClusterService() {
		return clusterService;
	}

	public void setClusterService(NodeManagerService clusterService) {
		this.clusterService = clusterService;
	}
	
	public <T> ResponseMsg<T> createResponseMsg() {
		ResponseMsg<T> responseMsg = new ResponseMsg<>(clusterService.getCurrentNode().getName());
		return responseMsg;
	}
}
