package six.com.crawler.common.entity;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年12月19日 上午9:55:41 
*/
public class QueueInfo {

	/**
	 * 队列名字
	 */
	private String queueName;
	/**
	 * 代理队列size
	 */
	private int proxyQueueSize;
	/**
	 * 真是队列数量
	 */
	private int realQueueSize;
	
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public int getProxyQueueSize() {
		return proxyQueueSize;
	}
	public void setProxyQueueSize(int proxyQueueSize) {
		this.proxyQueueSize = proxyQueueSize;
	}
	public int getRealQueueSize() {
		return realQueueSize;
	}
	public void setRealQueueSize(int realQueueSize) {
		this.realQueueSize = realQueueSize;
	}
}
