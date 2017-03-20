package six.com.crawler.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月12日 下午12:59:25 
*/
public class AbstractHttpException extends BaseException{


	/**
	 * 
	 */
	private static final long serialVersionUID = -200418828733119023L;

	
	public AbstractHttpException() {
		super();
	}
	
	public AbstractHttpException(String message) {
		super(message, null);
	}

	public AbstractHttpException(String message, Throwable cause) {
		super(message, cause);
	}

}
