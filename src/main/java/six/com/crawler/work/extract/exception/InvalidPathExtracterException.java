package six.com.crawler.work.extract.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午10:10:48 
*/
public class InvalidPathExtracterException extends ExtracterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3917579915429512446L;

	public InvalidPathExtracterException(String message) {
		super(message);
	}
	
	public InvalidPathExtracterException(String message, Throwable cause) {
		super(message, cause);
	}
}
