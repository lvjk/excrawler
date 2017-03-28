package six.com.crawler.work.downer.exception;

/**
 * @author six
 * @date 2016年8月18日 上午11:03:42
 *  未知的 http 响应 状态异常
 */
public class UnknownHttpStatusDownException extends DownerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3389980017744878875L;

	public UnknownHttpStatusDownException(String message) {
		super(message, null);
	}

	public UnknownHttpStatusDownException(String message, Throwable cause) {
		super(message, cause);
	}

}
