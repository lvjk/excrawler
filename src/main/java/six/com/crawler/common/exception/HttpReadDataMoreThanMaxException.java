package six.com.crawler.common.exception;


/**
 * @author six
 * @date 2016年8月18日 下午3:13:29
 *
 *       http读取数据 超过最大 Max 异常
 */
public class HttpReadDataMoreThanMaxException extends AbstractHttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3163678753780073843L;

	public HttpReadDataMoreThanMaxException(String message) {
		super(message, null);
	}

	public HttpReadDataMoreThanMaxException(String message, Throwable cause) {
		super(message, cause);
	}
}
