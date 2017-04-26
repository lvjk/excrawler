package six.com.crawler.work.downer.exception;

/**
 * 
 * 502异常处理类
 * @author weijiyong@tospur.com
 *
 */
public class HttpFiveZeroTwoException extends DownerException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2606190971403749267L;

	public HttpFiveZeroTwoException(String message) {
		super(message, null);
	}

	public HttpFiveZeroTwoException(String message, Throwable cause) {
		super(message, cause);
	}
}
