package six.com.crawler.work.extract.exception;

import six.com.crawler.work.exception.WorkerCrawlerException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午10:10:30
 */
public abstract class ExtracterException extends WorkerCrawlerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8842899171102267005L;

	public ExtracterException(String type, String message) {
		super(type, message);
	}

	public ExtracterException(String type, String message, Throwable cause) {
		super(type, message, cause);
	}

}
