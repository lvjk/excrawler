package six.com.crawler.work.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月19日 上午11:49:46
 */
public class WorkerOtherException extends WorkerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5729405076404082170L;

	public WorkerOtherException(String message) {
		super(WorkerExceptionType.WORKER_OTHER_EXCEPTION, message);
	}
	
	public WorkerOtherException(Throwable cause) {
		super(WorkerExceptionType.WORKER_OTHER_EXCEPTION, cause);
	}

	public WorkerOtherException(String message, Throwable cause) {
		super(WorkerExceptionType.WORKER_OTHER_EXCEPTION, message, cause);
	}

}
