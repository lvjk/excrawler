package six.com.crawler.work.exception;

/**
 * @author six
 * @date 2016年8月18日 上午11:03:42
 *  未知的 http 响应 状态异常
 */
public class UnknownHttpStatusDownHtmlProcessorException extends DownerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3389980017744878875L;

	public UnknownHttpStatusDownHtmlProcessorException(String message) {
		super(message, null);
	}

	public UnknownHttpStatusDownHtmlProcessorException(String message, Throwable cause) {
		super(message, cause);
	}

}
