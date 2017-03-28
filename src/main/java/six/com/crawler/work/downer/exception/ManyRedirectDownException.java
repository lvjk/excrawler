package six.com.crawler.work.downer.exception;

/**
 *@author six    
 *@date 2016年8月30日 下午2:42:32  
*/
public class ManyRedirectDownException extends DownerException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3836363279563745918L;


	public ManyRedirectDownException(String message) {
		super(message);
	}

	public ManyRedirectDownException(String message, Throwable cause) {
		super(message, cause);
	}
}
