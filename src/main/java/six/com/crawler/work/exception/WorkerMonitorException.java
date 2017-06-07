package six.com.crawler.work.exception;

/**
 * 
 * @author weijiyong@tospur.com
 *
 */
public class WorkerMonitorException extends WorkerException{

	private static final long serialVersionUID = 7420619422588660016L;

	public WorkerMonitorException(String message) {
		super(WorkerExceptionType.WORKER_MONITOR_JOB_EXCEPTION, message);
	}
	
	public WorkerMonitorException(Throwable cause) {
		super(WorkerExceptionType.WORKER_MONITOR_JOB_EXCEPTION, cause);
	}

	public WorkerMonitorException(String message, Throwable cause) {
		super(WorkerExceptionType.WORKER_MONITOR_JOB_EXCEPTION, message, cause);
	}
}
