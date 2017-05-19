package six.com.crawler.work.extract.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月28日 下午2:23:34
 */
public class UnfoundPathExtractException extends ExtracterException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7156839885029059512L;

	public UnfoundPathExtractException(String message) {
		super(WorkerExceptionType.EXTRACT_UNFOUND_PATH_EXCEPTION, message);
	}

	public UnfoundPathExtractException(String message, Throwable cause) {
		super(WorkerExceptionType.EXTRACT_UNFOUND_PATH_EXCEPTION, message, cause);
	}

}
