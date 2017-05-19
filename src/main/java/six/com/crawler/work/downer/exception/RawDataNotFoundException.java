package six.com.crawler.work.downer.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 * 源数据未找到异常
 * @author weijiyong@tospur.com
 *
 */
public class RawDataNotFoundException extends DownerException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7272831990726015341L;

	public RawDataNotFoundException(String message) {
		super(WorkerExceptionType.DOWNER_UNFOUND_RAW_DATA_EXCEPTION,message);
	}

	public RawDataNotFoundException(String message, Throwable cause) {
		super(WorkerExceptionType.DOWNER_UNFOUND_RAW_DATA_EXCEPTION,message, cause);
	}
}
