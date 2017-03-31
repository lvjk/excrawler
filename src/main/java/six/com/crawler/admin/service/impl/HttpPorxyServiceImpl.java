package six.com.crawler.admin.service.impl;


import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.admin.service.HttpPorxyService;
import six.com.crawler.dao.HttpProxyDao;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.http.HttpClient;


/**
 * @author six
 * @date 2016年8月26日 下午2:25:19 代理池 和本地ip 管理
 */
@Service
public class HttpPorxyServiceImpl implements HttpPorxyService {

	final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceImpl.class);

	@Autowired
	private HttpProxyDao httpProxyDao;

	@Autowired
	private HttpClient httpClient;

	@Override
	public List<HttpProxy> getHttpProxys() {
		return httpProxyDao.getHttpProxys();
	}

	@Override
	public String addHttpProxy(HttpProxy httpProxy) {
		if (httpClient.isValidHttpProxy(httpProxy)) {
			if(httpProxyDao.save(httpProxy)){
				return "this httpProxy[" + httpProxy.toString() + "] add succeed";
			}else{
				return "this httpProxy[" + httpProxy.toString() + "] add succeed";
			}
		}else{
			return "this httpProxy[" + httpProxy.toString() + "] is invalid";
		}

	}

	@Override
	public String testHttpProxy(HttpProxy httpProxy) {
		if (httpClient.isValidHttpProxy(httpProxy)) {
			return "this httpProxy[" + httpProxy.toString() + "] is valid";
		} else {
			return "this httpProxy[" + httpProxy.toString() + "] is invalid";
		}
	}

	@Override
	public String delHttpProxy(HttpProxy httpProxy) {
		if (httpProxyDao.del(httpProxy)) {
			return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] del succeed";
		}
		return "this httpProxy[" + httpProxy.getHost() + ":" + httpProxy.getPort() + "] del failed";
	}

	public void delAllHttpProxy() {
		httpProxyDao.delAllHttpProxy();
	}
	
	public HttpProxyDao getHttpProxyDao() {
		return httpProxyDao;
	}

	public void setHttpProxyDao(HttpProxyDao httpProxyDao) {
		this.httpProxyDao = httpProxyDao;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
