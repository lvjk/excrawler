package six.com.crawler.work.extract.exception;
/**
 *@author six    
 *@date 2016年8月25日 下午2:16:54  
*/
public class ExtractEmptyResultException extends ExtracterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7641480285086513770L;
	
	public ExtractEmptyResultException(String message) {
		super(message);
	}

	public ExtractEmptyResultException(String message, Throwable cause) {
		super(message, cause);
	}

}
