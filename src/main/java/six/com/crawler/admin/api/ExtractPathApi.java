package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.service.ExtractPathService;
import six.com.crawler.admin.vo.TestExtractPathVo;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月28日 下午4:35:23
 */
@Controller
public class ExtractPathApi extends BaseApi {

	@Autowired
	private ExtractPathService extracterService;

	public ExtractPathService getExtracterService() {
		return extracterService;
	}

	public void setExtracterService(ExtractPathService extracterService) {
		this.extracterService = extracterService;
	}


	@RequestMapping(value = "/crawler/extracter/queryExtractPaths", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<List<ExtractPath>> queryExtractPaths(
			@RequestParam("siteCode") String siteCode,
			@RequestParam("pathName") String pathName) {
		ResponseMsg<List<ExtractPath>> msg = createResponseMsg();
		extracterService.fuzzyQuery(msg,siteCode,pathName);
		return msg;
	}
	
	@RequestMapping(value = "/crawler/extracter/testExtract", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<List<String>> testExtract(
			TestExtractPathVo extractPath) {
		ResponseMsg<List<String>> msg = createResponseMsg();
		List<String> result = extracterService.testExtract(extractPath);
		msg.setData(result);
		return msg;
	}

}
