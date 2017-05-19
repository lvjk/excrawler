package six.com.crawler.work.extract.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午10:10:48 
*/
public class InvalidPathExtracterException extends ExtracterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3917579915429512446L;

	public InvalidPathExtracterException(String message) {
		super(WorkerExceptionType.EXTRACT_INVALID_PATH_EXCEPTION,message);
	}
	
	public InvalidPathExtracterException(String message, Throwable cause) {
		super(WorkerExceptionType.EXTRACT_INVALID_PATH_EXCEPTION,message, cause);
	}
}
