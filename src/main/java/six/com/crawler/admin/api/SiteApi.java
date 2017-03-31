package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.admin.service.SiteService;
import six.com.crawler.entity.Site;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月1日 下午2:06:40
 */
@Controller
public class SiteApi extends BaseApi {

	@Autowired
	private SiteService siteService;

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@RequestMapping(value = "/crawler/site/query/{pageIndex}/{pageSize}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Site>> querySites(@PathVariable("pageIndex") int pageIndex,
			@PathVariable("pageSize") int pageSize) {
		ResponseMsg<List<Site>> msg = createResponseMsg();
		List<Site> result = siteService.querySites(pageIndex, pageSize);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/site/upload/profile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> uploadFile(@RequestParam("file") MultipartFile file) {
		ResponseMsg<String> responseMsg = createResponseMsg();
		String msg = uploadFile(siteService, file);
		responseMsg.setMsg(msg);
		return responseMsg;
	}

	@RequestMapping(value = "/crawler/site/download/profile/{siteCode}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("siteCode") String siteCode) {
		return downloadFile(siteService, siteCode);
	}
}
