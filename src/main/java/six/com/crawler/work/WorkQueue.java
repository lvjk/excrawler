package six.com.crawler.work;

import six.com.crawler.entity.Page;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年1月16日 上午7:12:58 job工作仓库 类说明
 */
public interface WorkQueue {


	/**
	 * 获取等待处理的数据p
	 * 
	 * @return
	 */
	public Page pull();

	/**
	 * 获取等待处理数据的数量
	 * 
	 * @return
	 */
	public int size();

	/**
	 * 将数据放入等待处理
	 * 
	 * @param page
	 */
	public void push(Page t);

	/**
	 * 重新将数据放入等待处理
	 * 
	 * @param page
	 */
	public void retryPush(Page t);

	/**
	 * 清空等待处理数据
	 * 
	 * @param page
	 */
	public void clear();

	/**
	 * 是否重复处理过的数据key
	 * 
	 * @param Key
	 *            数据key
	 * @return 处理过 true 没处理过false
	 */
	public boolean duplicateKey(String pageKey);

	/**
	 * 添加处理 过的数据key
	 * 
	 * @param page
	 * @return
	 */
	public void addDuplicateKey(String pageKey,String url);

	/**
	 * 等待处理数据是否空
	 * 
	 * @return
	 */
	public boolean isEmptyForWaiting();
	
	/**
	 * 队列修复
	 */
	public void repair();

	/**
	 * 完成处理的数据p
	 * 
	 * @param e
	 */
	public void finish(Page page);
	
	/**
	 * 将数据放入 错误队列
	 * @param page
	 */
	public void pushErr(Page page);
	
	public void againDoErrQueue();

}
