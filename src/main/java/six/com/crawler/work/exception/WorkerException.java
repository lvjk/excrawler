package six.com.crawler.work.exception;

import six.com.crawler.exception.BaseException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 下午2:28:44 
*/
public class WorkerException extends BaseException{


	/**
	 * 
	 */
	private static final long serialVersionUID = 40361872698446830L;

	public WorkerException(String message) {
		super(message);
	}

	public WorkerException(String message, Throwable cause) {
		super(message, cause);
	}
}
