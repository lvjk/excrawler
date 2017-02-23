package six.com.crawler.work.extract;

import six.com.crawler.common.exception.BaseException;

/**
 * @author six
 * @date 2016年8月25日 上午10:12:35 解析处理程序 结果检查异常
 */
public class ExtracterResultException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5663536985607530113L;

	public ExtracterResultException() {
		super();
	}

	public ExtracterResultException(String message) {
		super(message);
	}

	public ExtracterResultException(String message, Throwable cause) {
		super(message, cause);
	}
}
