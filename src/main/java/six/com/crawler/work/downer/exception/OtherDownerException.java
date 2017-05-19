package six.com.crawler.work.downer.exception;

import six.com.crawler.work.exception.WorkerExceptionType;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月19日 上午11:38:32 
*/
public class OtherDownerException extends DownerException{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6422920541089653430L;

	public OtherDownerException(String message) {
		super(WorkerExceptionType.DOWNER_OTHER_EXCEPTION,message, null);
	}

	public OtherDownerException(String message, Throwable cause) {
		super(WorkerExceptionType.DOWNER_OTHER_EXCEPTION,message, cause);
	}
}