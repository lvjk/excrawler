package six.com.crawler.work.downer.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 * @author six
 * @date 2016年8月18日 下午3:13:29
 *
 *       http读取数据 超过最大 Max 异常
 */
public class DataTooBigDownerException extends DownerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3062149725002886914L;

	public DataTooBigDownerException(String message) {
		super(WorkerExceptionType.DOWNER_DATA_TOO_BIG_EXCEPTION,message, null);
	}

	public DataTooBigDownerException(String message, Throwable cause) {
		super(WorkerExceptionType.DOWNER_DATA_TOO_BIG_EXCEPTION,message, cause);
	}
}
