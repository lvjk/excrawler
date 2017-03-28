package six.com.crawler.exception;


/**
 *@author six    
 *@date 2016年8月26日 下午4:24:25  
*/
public class HttpIoException extends AbstractHttpException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2507844940537119834L;


	public HttpIoException(String message) {
		super(message);
	}

	public HttpIoException(String message, Throwable cause) {
		super(message, cause);
	}
}
