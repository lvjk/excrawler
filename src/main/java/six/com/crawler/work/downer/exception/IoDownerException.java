package six.com.crawler.work.downer.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 *@author six    
 *@date 2016年8月26日 下午4:24:25  
*/
public class IoDownerException extends DownerException{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4216827418140704678L;

	public IoDownerException(String message) {
		super(WorkerExceptionType.DOWNER_UNFOUND_RAW_DATA_EXCEPTION,message);
	}

	public IoDownerException(String message, Throwable cause) {
		super(WorkerExceptionType.DOWNER_UNFOUND_RAW_DATA_EXCEPTION,message, cause);
	}
}
