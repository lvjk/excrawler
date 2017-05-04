package six.com.crawler.schedule.exception;

import six.com.crawler.exception.BaseException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月3日 上午10:02:58 
* 非法执行job
*/
public class IllegalExecutJobException extends BaseException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3636968717504398033L;

	public IllegalExecutJobException(String message) {
		super(message);
	}

	public IllegalExecutJobException(String message, Throwable cause) {
		super(message, cause);
	}

}
