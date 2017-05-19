package six.com.crawler.work.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月19日 下午3:38:57 
*/
public class UnknowWorkerCrawlerException extends WorkerCrawlerException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5426964448545692680L;

	public UnknowWorkerCrawlerException(String message) {
		super(WorkerExceptionType.WORKER_CRAWLER_UNKNOW_EXCEPTION, message);
	}

	public UnknowWorkerCrawlerException(String message, Throwable cause) {
		super(WorkerExceptionType.WORKER_CRAWLER_UNKNOW_EXCEPTION, message, cause);
	}
}
