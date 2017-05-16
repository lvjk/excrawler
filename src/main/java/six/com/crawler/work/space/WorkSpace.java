package six.com.crawler.work.space;

import java.util.List;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月16日 上午7:12:58 当任务执行时，都会拥有自己的工作空间,工作空间处理的数据必须实现
 *          WorkSpaceData 接口
 */
public interface WorkSpace<T extends WorkSpaceData> {

	/**
	 * 工作空间名称
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 批量获取处理数据 当传入cursorStr为blank或者"0"的话，默认为新的查询,
	 * 当返回cursorStr返回为"0"的话那么认为查询到末尾了
	 * 
	 * @param resutList
	 * @param cursorStr
	 * @return
	 */
	String batchGetDoingData(List<T> resutList,int segmentIndex, String cursorStr);

	/**
	 * 批量获取异常数据 当传入cursorStr为blank或者"0"的话，默认为新的查询,
	 * 当返回cursorStr返回为"0"的话那么认为查询到末尾了
	 * 
	 * @param resutList
	 * @param cursorStr
	 * @return
	 */
	String batchGetErrData(List<T> resutList, String cursorStr);
	
	
	/**
	 * 批量获取处理过的数据 当传入cursorStr为blank或者"0"的话，默认为新的查询,
	 * 当返回cursorStr返回为"0"的话那么认为查询到末尾了
	 * 
	 * @param resutList
	 * @param cursorStr
	 * @return
	 */
	String batchGetDoneData(List<String> resutList, int segmentIndex,String cursorStr);

	/**
	 * 将数据 推到工作队列中
	 * 
	 * @param data
	 * @return 成功或失败
	 */
	boolean push(T data);

	/**
	 * 将数据 推到工作队列中
	 * 
	 * @param data
	 * @return 成功或失败
	 */
	boolean errRetryPush(T data);

	/**
	 * 从工作队列拉取数据
	 * <p>
	 * 注意:拉去数据并没有删除实际数据，需要结合ack一起使用
	 * </p>
	 * 
	 * @return 有数据返回队列头数据，否则返回null
	 */
	T pull();

	/**
	 * 确认pull出来得数据被成功处理，然后删除实际数据
	 * 
	 * @param data
	 */
	void ack(T data);

	void repair();

	/**
	 * 用来保存处理不了的数据
	 * 
	 * @param data
	 */
	void addErr(T data);

	/**
	 * 将异常数据再一次添加进工作队列
	 */
	void againDoErrQueue();

	/**
	 * 判断key是否被处理过
	 * 
	 * @param key
	 * @return
	 */
	boolean isDone(String key);

	/**
	 * 添加处理过的数据
	 * 
	 * @param data
	 */
	void addDone(String key);

	/**
	 * 获取工作队列size
	 * 
	 * @return
	 */
	int doingSize();
	
	boolean doingIsEmpty();

	int doingSegmentSize();
	/**
	 * 获取工作异常队列size
	 * 
	 * @return
	 */
	int errSize();

	/**
	 * 获取工作处理过的队列size
	 * 
	 * @return
	 */
	int doneSize();

	/**
	 * 清除工作队列
	 */
	void clearDoing();

	/**
	 * 清除工作异常队列
	 */
	void clearErr();

	/**
	 * 清除工作处理过的队列
	 */
	void clearDone();

	void close();
}
