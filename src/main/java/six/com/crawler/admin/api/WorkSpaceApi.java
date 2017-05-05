package six.com.crawler.admin.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import six.com.crawler.admin.service.WorkSpaceService;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.WorkSpaceInfo;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.utils.ArrayListUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月8日 下午3:24:18
 */

@Controller
public class WorkSpaceApi extends BaseApi {

	@Autowired
	private WorkSpaceService workQueueService;

	@RequestMapping(value = "/crawler/workSpace/getWorkSpaces", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<WorkSpaceInfo>> getWorkSpaces() {
		return workQueueService.getWorkSpaces();
	}

	@RequestMapping(value = "/crawler/workSpace/getDoingData/{workSpaceName}/{cursor}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Map<String, Object>> getDoingData(@PathVariable("workSpaceName") String workSpaceName,
			@PathVariable("cursor") String cursor) {
		return workQueueService.getWorkSpaceDoingData(workSpaceName, cursor);
	}

	@RequestMapping(value = "/crawler/workSpace/getErrData/{workSpaceName}/{cursor}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Map<String, Object>> getErrData(@PathVariable("workSpaceName") String workSpaceName,
			@PathVariable("cursor") String cursor) {
		return workQueueService.getWorkSpaceErrData(workSpaceName, cursor);
	}

	@RequestMapping(value = "/crawler/workSpace/clearDoing/{workSpaceName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> clearDoing(@PathVariable("workSpaceName") String workSpaceName) {
		return workQueueService.clearDoing(workSpaceName);
	}

	@RequestMapping(value = "/crawler/workSpace/clearErr/{workSpaceName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> clearErr(@PathVariable("workSpaceName") String workSpaceName) {
		return workQueueService.clearErr(workSpaceName);
	}

	@RequestMapping(value = "/crawler/workSpace/clearDone/{workSpaceName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> clearDone(@PathVariable("workSpaceName") String workSpaceName) {
		return workQueueService.clearDone(workSpaceName);
	}

	@RequestMapping(value = "/crawler/workSpace/againDoErrQueue/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> againDoErrQueue(@PathVariable("queueName") String queueName) {
		return workQueueService.againDoErrQueue(queueName);
	}
	
	@RequestMapping(value = "/crawler/tools/addDoingQueue/{workSpaceName}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseMsg<String> addDoingQueue(@PathVariable("workSpaceName") String workSpaceName, @RequestBody Map<String,Object> requestBoby) {
		String siteCode = requestBoby.get("siteCode").toString();
		String url = requestBoby.get("url").toString();
		String httpMethod = requestBoby.get("httpMethod").toString();
		String params = requestBoby.get("params").toString();
		String meta = requestBoby.get("meta").toString();
		Page page=new Page(siteCode,1,url,url);
		if(httpMethod.equals(HttpMethod.GET.value)){
			page.setMethod(HttpMethod.GET);
		}else{
			page.setMethod(HttpMethod.POST);
			if(null!=params&&params.length()>0){
				Map<String,Object> paramsMap=(Map<String, Object>) JSON.parseObject(params);
				page.setParameters(paramsMap);
			}
		}
		
		if(null!=meta&&meta.length()>0){
			Map<String,Object> metaMap=(Map<String, Object>) JSON.parseObject(meta);
			for (String key:metaMap.keySet()) {
				page.getMetaMap().put(key, ArrayListUtils.asList(metaMap.get(key).toString()));
			}
		}
		
		return workQueueService.AddDoing(workSpaceName, page);
	}

}
