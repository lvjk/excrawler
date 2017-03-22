package six.com.crawler.service;

import java.util.List;

import six.com.crawler.entity.HttpProxy;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午4:03:37
 */
public interface HttpPorxyService {


	public List<HttpProxy> getHttpProxys();

	/**
	 * 添加一个 http代理
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public String addHttpProxy(HttpProxy httpProxy);

	/**
	 * 更新指定http代理
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public String testHttpProxy(HttpProxy httpProxy);

	/**
	 * 删除指定http代理
	 * 
	 * @param host
	 * @return
	 */
	public String delHttpProxy(HttpProxy httpProxy);
	
	/**
	 * 删除指定http代理
	 * 
	 * @param host
	 * @return
	 */
	public void delAllHttpProxy();
}
