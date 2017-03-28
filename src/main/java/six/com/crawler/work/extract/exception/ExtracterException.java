package six.com.crawler.work.extract.exception;

import six.com.crawler.work.exception.WorkerException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午10:10:30 
*/
public class ExtracterException extends WorkerException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8842899171102267005L;



	public ExtracterException(String message) {
		super(message);
	}

	public ExtracterException(String message, Throwable cause) {
		super(message, cause);
	}

}
