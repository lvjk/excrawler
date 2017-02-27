package six.com.crawler.work.extract;

import six.com.crawler.common.exception.BaseException;

/**
 * @author six
 * @date 2016年8月25日 上午10:14:40 解析处理程序 结果抽取异常
 */
public class ExtractUnknownException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3712692884588128954L;

	
	public ExtractUnknownException() {
		super();
	}

	public ExtractUnknownException(String message) {
		super(message);
	}

	public ExtractUnknownException(String message, Throwable cause) {
		super(message, cause);
	}
}
