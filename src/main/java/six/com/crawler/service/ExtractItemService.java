package six.com.crawler.service;

import java.util.List;

import six.com.crawler.api.ResponseMsg;
import six.com.crawler.work.extract.ExtractItem;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月30日 上午10:26:34 
*/
public interface ExtractItemService {

	public ResponseMsg<List<ExtractItem>> query(String jobName);
	
	public ResponseMsg<String> add(ExtractItem extractItem);
	
	public ResponseMsg<String> update(ExtractItem extractItem);
	
	public ResponseMsg<String> del(String id);
	
}
