package six.com.crawler.work.downer.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 * 
 * 502异常处理类
 * @author weijiyong@tospur.com
 *
 */
public class HttpStatus502DownerException extends DownerException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2606190971403749267L;

	public HttpStatus502DownerException(String message) {
		super(WorkerExceptionType.DOWNER_502_HTTP_STATUS_EXCEPTION,message, null);
	}

	public HttpStatus502DownerException(String message, Throwable cause) {
		super(WorkerExceptionType.DOWNER_502_HTTP_STATUS_EXCEPTION,message, cause);
	}
}
