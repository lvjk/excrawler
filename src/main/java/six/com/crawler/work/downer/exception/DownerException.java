package six.com.crawler.work.downer.exception;

import six.com.crawler.work.exception.WorkerException;

/**
 * @author six
 * @date 2016年8月26日 下午2:14:46
 * 下载器异常：
 * <p>1.下载超时异常</p>
 * <p>2.重定向太多异常</p>
 * <p>3.未知状态码异常</p>
 * 
 * 
 */
public class DownerException extends WorkerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7822200945702547159L;

	public DownerException(String message) {
		super(message);
	}

	public DownerException(String message, Throwable cause) {
		super(message, cause);
	}
}
