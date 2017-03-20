package six.com.crawler.service;

import java.util.List;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.HttpProxyType;
import six.com.crawler.http.HttpProxyPool;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午4:03:37
 */
public interface HttpPorxyService {

	/**
	 * 根据siteCode 初始化一个 http代理池
	 * @param siteCode
	 */
	public HttpProxyPool buildHttpProxyPool(String siteCode,HttpProxyType httpProxyType,long restTime);
	

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
