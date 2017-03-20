package six.com.crawler.work.exception;

import six.com.crawler.common.exception.BaseException;

/**
 * @author six
 * @date 2016年8月26日 下午2:14:46
 */
public class DownerException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7822200945702547159L;

	public DownerException() {
		super();
	}

	public DownerException(String message) {
		super(message);
	}

	public DownerException(String message, Throwable cause) {
		super(message, cause);
	}
}
