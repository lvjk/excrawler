package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.service.HttpPorxyService;
import six.com.crawler.entity.HttpProxy;

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

	@RequestMapping(value = "/crawler/httpPorxy/getAll", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<HttpProxy>> getAll() {
		return httpPorxyService.getAll();
	}

	@RequestMapping(value = "/crawler/httpPorxy/save", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> save(HttpProxy httpProxy) {
		return httpPorxyService.save(httpProxy);
	}

	@RequestMapping(value = "/crawler/httpPorxy/test", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> test(HttpProxy httpProxy) {
		return httpPorxyService.test(httpProxy);
	}

	@RequestMapping(value = "/crawler/httpPorxy/del", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> del(@PathVariable("host") String host, @PathVariable("port") int port) {
		return httpPorxyService.del(host, port);
	}
}
