package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.admin.service.ExtractItemService;
import six.com.crawler.work.extract.ExtractItem;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月30日 上午10:25:51 
*/
@Controller
public class ExtractItemApi extends BaseApi {

	@Autowired
	private ExtractItemService extractItemService;
	
	
	@RequestMapping(value = "/crawler/extractItem/query/{jobName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<ExtractItem>> query( @PathVariable("jobName") String jobName) {
		return extractItemService.query(jobName);
	}
	
	

	public ExtractItemService getExtractItemService() {
		return extractItemService;
	}

	public void setExtractItemService(ExtractItemService extractItemService) {
		this.extractItemService = extractItemService;
	}
}
