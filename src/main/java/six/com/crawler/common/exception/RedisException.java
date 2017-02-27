package six.com.crawler.common.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月7日 下午3:43:05
 */
public class RedisException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7231026919082884307L;
	
	public RedisException() {
		super();
	}

	public RedisException(String message) {
		super(message);
	}

	public RedisException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedisException(Throwable cause) {
		super(cause);
	}

	protected RedisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
