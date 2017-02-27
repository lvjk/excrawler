package six.com.crawler.work.store;

import six.com.crawler.common.exception.BaseException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年2月23日 下午3:13:08 
*/
public class StoreException extends BaseException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4971967663948212856L;

	public StoreException() {
		super();
	}

	public StoreException(String message) {
		super(message);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public StoreException(Throwable cause) {
		super(cause);
	}

	protected StoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
