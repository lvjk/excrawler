package six.com.crawler.admin.service;

import java.util.List;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.work.extract.ExtractItem;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月30日 上午10:26:34 
* 抽取项服务
*/
public interface ExtractItemService {

	/**
	 * 根据jobname 查询 抽取项 ，
	 * @param jobName
	 * @return 查询到的结果list
	 */
	public ResponseMsg<List<ExtractItem>> query(String jobName);
	
	/**
	 * 添加一个抽取项
	 * @param extractItem
	 * @return 添加数据消息
	 */
	public ResponseMsg<String> add(ExtractItem extractItem);
	
	
	public ResponseMsg<String> update(ExtractItem extractItem);
	
	public ResponseMsg<String> del(String id);
	
}
