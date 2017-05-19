package six.com.crawler.work.extract.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 * @author six
 * @date 2016年8月25日 上午10:14:40 解析处理程序 结果抽取异常
 */
public class UnknownExtractException extends ExtracterException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3712692884588128954L;

	public UnknownExtractException(String message) {
		super(WorkerExceptionType.EXTRACT_UNKNOWN_EXCEPTION, message);
	}

	public UnknownExtractException(String message, Throwable cause) {
		super(WorkerExceptionType.EXTRACT_UNKNOWN_EXCEPTION, message, cause);
	}
}
