package six.com.crawler.work.downer.exception;

import six.com.crawler.work.extract.exception.ExtracterException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 下午12:57:37 
*/
public class PrimaryExtractException extends ExtracterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8478687869845807975L;

	public PrimaryExtractException(String message) {
		super(message);
	}
	
	public PrimaryExtractException(String message, Throwable cause) {
		super(message, cause);
	}

}
