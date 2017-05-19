package six.com.crawler.admin.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.admin.service.BaseService;
import six.com.crawler.admin.service.HttpPorxyService;
import six.com.crawler.dao.HttpProxyDao;
import six.com.crawler.entity.HttpProxy;
import six.com.crawler.work.downer.HttpClient;

/**
 * @author six
 * @date 2016年8月26日 下午2:25:19 代理池 和本地ip 管理
 */
@Service
public class HttpPorxyServiceImpl extends BaseService implements HttpPorxyService {

	final static Logger LOG = LoggerFactory.getLogger(HttpPorxyServiceImpl.class);

	@Autowired
	private HttpProxyDao httpProxyDao;

	@Autowired
	private HttpClient httpClient;

	@Override
	public ResponseMsg<List<HttpProxy>> getAll() {
		ResponseMsg<List<HttpProxy>> responseMsg = createResponseMsg();
		List<HttpProxy> result = httpProxyDao.getAll();
		responseMsg.isOk();
		responseMsg.setData(result);
		return responseMsg;
	}

	@Override
	public ResponseMsg<String> save(HttpProxy httpProxy) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (httpClient.isValidHttpProxy(httpProxy)) {
			if (1 == httpProxyDao.save(httpProxy)) {
				msg = "this httpProxy[" + httpProxy.toString() + "] add succeed";
				responseMsg.isOk();
			} else {
				msg = "this httpProxy[" + httpProxy.toString() + "] add failed";
				responseMsg.isNoOk();
			}
		} else {
			msg = "this httpProxy[" + httpProxy.toString() + "] is invalid";
			responseMsg.isNoOk();
		}
		responseMsg.setMsg(msg);
		return responseMsg;

	}

	@Override
	public ResponseMsg<String> test(HttpProxy httpProxy) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (httpClient.isValidHttpProxy(httpProxy)) {
			msg = "this httpProxy[" + httpProxy.toString() + "] is valid";
			responseMsg.isOk();
		} else {
			msg = "this httpProxy[" + httpProxy.toString() + "] is invalid";
			responseMsg.isNoOk();
		}
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@Override
	public ResponseMsg<String> del(String host, int port) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = null;
		if (1 == httpProxyDao.del(host, port)) {
			msg = "this httpProxy[" + host + ":" + port + "] del succeed";
			responseMsg.isOk();
		} else {
			msg = "this httpProxy[" + host + ":" + port + "] del failed";
			responseMsg.isNoOk();
		}
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@Override
	public ResponseMsg<String> delAll() {
		httpProxyDao.delAll();
		ResponseMsg<String> responseMsg = createResponseMsg();
		responseMsg.setMsg("dell all succeed");
		responseMsg.isOk();
		return responseMsg;
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
