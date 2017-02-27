package six.com.crawler.work.downer.exception;

/**
 * @author six
 * @date 2016年8月26日 下午4:12:06 路由下载处理空异常
 */
public class EmptyRouteDownHtmlProcessorException extends DownerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4299784947945297420L;

	public EmptyRouteDownHtmlProcessorException() {
		super();
	}

	public EmptyRouteDownHtmlProcessorException(String message) {
		super(message);
	}

	public EmptyRouteDownHtmlProcessorException(String message, Throwable cause) {
		super(message, cause);
	}
}
