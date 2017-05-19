package six.com.crawler.work.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月19日 下午12:16:20
 */
public class WorkerInitException extends WorkerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3763806166817997825L;

	public WorkerInitException(String message) {
		super(WorkerExceptionType.WORKER_INIT_EXCEPTION, message);
	}

	public WorkerInitException(String message, Throwable cause) {
		super(WorkerExceptionType.WORKER_INIT_EXCEPTION, message, cause);
	}

}
