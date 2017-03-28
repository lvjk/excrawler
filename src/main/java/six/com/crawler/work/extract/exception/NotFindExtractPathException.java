package six.com.crawler.work.extract.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月28日 下午2:23:34 
*/
public class NotFindExtractPathException extends ExtracterException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7156839885029059512L;

	public NotFindExtractPathException(String message) {
		super(message);
	}
	
	public NotFindExtractPathException(String message, Throwable cause) {
		super(message, cause);
	}

}
