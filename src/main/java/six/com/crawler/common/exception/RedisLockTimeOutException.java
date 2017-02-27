package six.com.crawler.common.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月24日 上午1:40:34 
*/
public class RedisLockTimeOutException extends RedisException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8313997607081348396L;

	public RedisLockTimeOutException() {
		super();
	}

	public RedisLockTimeOutException(String message) {
		super(message);
	}

	public RedisLockTimeOutException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedisLockTimeOutException(Throwable cause) {
		super(cause);
	}
}
