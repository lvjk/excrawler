package six.com.crawler.common.service;

import java.util.List;
import java.util.Map;

import six.com.crawler.common.entity.DoneInfo;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.QueueInfo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月8日 下午3:25:59
 */
public interface WorkQueueService {

	/**
	 * 获取队列里的信息
	 * @param queueName
	 * @param queueCursor
	 * @return  map.put(queueCursor,queueCursor) map.put(list,list)
	 */
	public Map<String,Object> getQueueInfo(String queueName,String queueCursor);
	
	/**
	 * 获取错误队列里的信息
	 * @return
	 */
	public List<Page> getErrQueueInfo(String queueName,int index);

	
	/**
	 * 通过指定队列名字获取信息
	 * @param queueName
	 * @return
	 */
	public QueueInfo getQueueInfos(String queueName);


	/**
	 * 获取所有任务处理过信息
	 * 
	 * @return
	 */
	public List<DoneInfo> getQueueDones();

	/**
	 * 移除指定任务处理过信息
	 * 
	 * @return
	 */
	public String cleanQueueDones(String queueName);

	/**
	 * 通过 queueName 清除指定任务队列
	 * 
	 * @param queueName
	 * @return
	 */
	public String cleanQueue(String queueName);

	/**
	 * 通过 queueName 修復指定任务队列
	 * 
	 * @param queueName againDoErrQueue
	 * @return
	 */
	public String repairQueue(String queueName);
	
	/**
	 * 将错误队列里的数据重新处理
	 * @param queueName
	 * @return
	 */
	public String againDoErrQueue(String queueName);
}
