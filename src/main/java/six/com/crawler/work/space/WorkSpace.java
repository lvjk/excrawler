package six.com.crawler.work.space;

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
	public String getName();

	/**
	 * 将数据 推到工作队列中
	 * 
	 * @param data
	 * @return 成功或失败
	 */
	public boolean push(T data);

	/**
	 * 从工作队列拉取数据
	 * <p>
	 * 注意:拉去数据并没有删除实际数据，需要结合ack一起使用
	 * </p>
	 * 
	 * @return 有数据返回队列头数据，否则返回null
	 */
	public T pull();

	/**
	 * 确认pull出来得数据被成功处理，然后删除实际数据
	 * 
	 * @param data
	 */
	public void ack(T data);

	/**
	 * 用来保存处理不了的数据
	 * 
	 * @param data
	 */
	public void addErr(T data);

	/**
	 * 将异常数据再一次添加进工作队列
	 */
	public void againDoErrQueue();

	/**
	 * 判断key是否被处理过
	 * 
	 * @param key
	 * @return
	 */
	public boolean isDone(String key);

	/**
	 * 添加处理过的数据
	 * 
	 * @param data
	 */
	public void addDone(T data);

	/**
	 * 获取工作队列size
	 * 
	 * @return
	 */
	public int doingSize();

	/**
	 * 获取工作异常队列size
	 * 
	 * @return
	 */
	public int errSize();

	/**
	 * 获取工作处理过的队列size
	 * 
	 * @return
	 */
	public int doneSize();

	/**
	 * 清除工作队列
	 */
	public void clearDoing();

	/**
	 * 清除工作异常队列
	 */
	public void clearErr();

	/**
	 * 清除工作处理过的队列
	 */
	public void clearDone();

}
