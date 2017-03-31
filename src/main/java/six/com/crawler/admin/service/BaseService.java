package six.com.crawler.admin.service;


import six.com.crawler.admin.api.ResponseMsg;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月30日 上午9:25:08 
* 
* 对外提供service 基础类
* 所有的admin service 都可继承此类
* 并且所有admin service 返回值应是 ResponseMsg<？> 类型
* 
*/
public abstract class BaseService {

	public static <T> ResponseMsg<T> createResponseMsg() {
		ResponseMsg<T> responseMsg = new ResponseMsg<>();
		return responseMsg;
	}
}
