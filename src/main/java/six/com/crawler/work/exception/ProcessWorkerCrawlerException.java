package six.com.crawler.work.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月19日 下午3:46:03 
*/
public class ProcessWorkerCrawlerException extends WorkerCrawlerException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4342272878768599372L;

	public ProcessWorkerCrawlerException(String message) {
		super(WorkerExceptionType.WORKER_CRAWLER_PROCESS_EXCEPTION, message);
	}

	public ProcessWorkerCrawlerException(String message, Throwable cause) {
		super(WorkerExceptionType.WORKER_CRAWLER_PROCESS_EXCEPTION, message, cause);
	}
}
