package six.com.crawler.admin.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import six.com.crawler.common.entity.DoneInfo;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.service.WorkQueueService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月8日 下午3:24:18
 */

@Controller
public class QueueApi extends BaseApi{

	@Autowired
	private WorkQueueService workQueueService;

	@RequestMapping(value = "/crawler/queue/getQueueInfo/{queueName}/{queueCursor}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<Map<String,Object>> getQueueInfo(@PathVariable("queueName") String queueName,
			@PathVariable("queueCursor") String queueCursor) {
		ResponseMsg<Map<String,Object>> msg = createResponseMsg();
		Map<String,Object> result = workQueueService.getQueueInfo(queueName,queueCursor);
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/queue/getErrQueueInfo/{queueName}/{errQueueIndex}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<Page>> getErrQueueInfo(@PathVariable("queueName") String queueName
			,@PathVariable("errQueueIndex")int errQueueIndex) {
		ResponseMsg<List<Page>> msg = createResponseMsg();
		List<Page> result = workQueueService.getErrQueueInfo(queueName,errQueueIndex);
		msg.setData(result);
		return msg;
	}
	
	
	@RequestMapping(value = "/crawler/queue/getQueueDones", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<List<DoneInfo>> getQueueDones() {
		ResponseMsg<List<DoneInfo>> msg = createResponseMsg();
		List<DoneInfo> result = workQueueService.getQueueDones();
		msg.setData(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/queue/cleanQueueDone/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> cleanQueueDone(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = workQueueService.cleanQueueDones(queueName);
		msg.setMsg(result);
		return msg;
	}


	@RequestMapping(value = "/crawler/queue/cleanQueue/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> cleanQueue(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = workQueueService.cleanQueue(queueName);
		msg.setMsg(result);
		return msg;
	}

	@RequestMapping(value = "/crawler/queue/repairQueue/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> repairQueue(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = workQueueService.repairQueue(queueName);
		msg.setMsg(result);
		return msg;
	}
	
	
	@RequestMapping(value = "/crawler/queue/againDoErrQueue/{queueName}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseMsg<String> againDoErrQueue(@PathVariable("queueName") String queueName) {
		ResponseMsg<String> msg = createResponseMsg();
		String result = workQueueService.againDoErrQueue(queueName);
		msg.setMsg(result);
		return msg;
	}

}
