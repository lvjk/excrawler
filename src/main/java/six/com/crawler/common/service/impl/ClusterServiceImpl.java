package six.com.crawler.common.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.common.entity.Node;
import six.com.crawler.common.service.ClusterService;
import six.com.crawler.schedule.RegisterCenter;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月24日 下午9:33:55 
*/

@Service
public class ClusterServiceImpl implements ClusterService{
	
	
	@Autowired
	private RegisterCenter registerCenter;

	public RegisterCenter getRegisterCenter() {
		return registerCenter;
	}

	public void setRegisterCenter(RegisterCenter registerCenter) {
		this.registerCenter = registerCenter;
	}

	@Override
	public List<Node> getClusterInfo() {
		return registerCenter.getNodes();
	}

}
