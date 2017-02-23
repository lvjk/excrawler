package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.service.HttpPorxyService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午3:59:21
 */
@Controller
public class HttpProxyApi extends BaseApi {

	@Autowired
	private HttpPorxyService httpPorxyService;

	public HttpPorxyService getHttpPorxyService() {
		return httpPorxyService;
	}

	public void setHttpPorxyService(HttpPorxyService httpPorxyService) {
		this.httpPorxyService = httpPorxyService;
	}

	@RequestMapping(value = "/crawler/httpPorxy/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<HttpProxy>> getHttpPorxys() {
		ResponseMsg<List<HttpProxy>> msg = new ResponseMsg<>();
		List<HttpProxy> result = httpPorxyService.getHttpProxys();
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/httpPorxy/add", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> addHttpPorxys(HttpProxy httpProxy) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = httpPorxyService.addHttpProxy(httpProxy);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/httpPorxy/test", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> testHttpPorxys(HttpProxy httpProxy) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = httpPorxyService.testHttpProxy(httpProxy);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/httpPorxy/del", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> delHttpPorxys(HttpProxy httpProxy) {
		ResponseMsg<String> msg = new ResponseMsg<>();
		String result = httpPorxyService.delHttpProxy(httpProxy);
		msg.setMsg(result);
		return msg;
	}
}
