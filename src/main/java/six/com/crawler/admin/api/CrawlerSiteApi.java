package six.com.crawler.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.entity.Site;
import six.com.crawler.common.service.SiteService;

/**
 *@author six    
 *@date 2016年5月31日 下午3:59:35  
 * 爬虫  site api
*/
@Controller
public class CrawlerSiteApi {

	@Autowired
	private SiteService siteService;
	
	@RequestMapping(value = "/crawler/site/getall", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Site>> getAllJobs(){
		ResponseMsg<List<Site>> responseMsg=new ResponseMsg<List<Site>>();
		return responseMsg;
	}
	
	@RequestMapping(value = "/crawler/site/analysis", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> analysisSite(String siteCode){
		ResponseMsg<String> responseMsg=new ResponseMsg<String>();
		return responseMsg;
	}
	
	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
}
