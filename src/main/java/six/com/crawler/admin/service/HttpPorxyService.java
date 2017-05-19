package six.com.crawler.admin.service;

import java.util.List;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.entity.HttpProxy;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午4:03:37
 */
public interface HttpPorxyService {

	public ResponseMsg<List<HttpProxy>> getAll();

	/**
	 * 添加一个 http代理
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public ResponseMsg<String> save(HttpProxy httpProxy);

	/**
	 * 更新指定http代理
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public ResponseMsg<String> test(HttpProxy httpProxy);

	/**
	 * 删除指定http代理
	 * 
	 * @param host
	 * @return
	 */
	public ResponseMsg<String> del(String host, int port);

	public ResponseMsg<String> delAll();
}
