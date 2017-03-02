package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.service.ExtracterService;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月28日 下午4:35:23
 */
@Controller
public class ExtracterApi extends BaseApi {

	@Autowired
	private ExtracterService extracterService;

	public ExtracterService getExtracterService() {
		return extracterService;
	}

	public void setExtracterService(ExtracterService extracterService) {
		this.extracterService = extracterService;
	}


	@RequestMapping(value = "/crawler/extracter/testExtract", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<List<String>> testExtract(
			ExtractPath extractPath,
			String testHtml,
			String testUrl) {
		ResponseMsg<List<String>> msg = new ResponseMsg<>();
		List<String> result = extracterService.testExtract(extractPath, testHtml,testUrl);
		msg.setData(result);
		return msg;
	}

}
