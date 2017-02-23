package six.com.crawler.common.exception;

/**
 * @author six
 * @date 2016年7月7日 下午5:18:35
 */
public abstract class BaseException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5538222781965562346L;


	public BaseException() {
		super();
	}

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(Throwable cause) {
		super(cause);
	}

	protected BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
