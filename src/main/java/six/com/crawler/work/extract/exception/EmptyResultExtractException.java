package six.com.crawler.work.extract.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 * @author six
 * @date 2016年8月25日 下午2:16:54
 */
public class EmptyResultExtractException extends ExtracterException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7641480285086513770L;

	public EmptyResultExtractException(String message) {
		super(WorkerExceptionType.EXTRACT_EMPTY_EXCEPTION, message);
	}

	public EmptyResultExtractException(String message, Throwable cause) {
		super(WorkerExceptionType.EXTRACT_EMPTY_EXCEPTION, message, cause);
	}

}
