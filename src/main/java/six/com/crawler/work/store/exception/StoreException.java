package six.com.crawler.work.store.exception;

import six.com.crawler.work.exception.WorkerException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月23日 下午3:13:08 
*/
public class StoreException extends WorkerException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4971967663948212856L;


	public StoreException(String message) {
		super(message);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
