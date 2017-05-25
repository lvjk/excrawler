package six.com.crawler.work.space;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月24日 下午12:09:34
 */
public class WorkSpaceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6041439028811992478L;

	public WorkSpaceException(String message) {
		super(message);
	}

	public WorkSpaceException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkSpaceException(Throwable cause) {
		super(cause);
	}
}
