package six.com.crawler.work.exception;

import six.com.crawler.exception.BaseException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 下午2:28:44
 * 
 *       worker 工作异常基类
 */
public abstract class WorkerException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 40361872698446830L;

	private String type;

	public WorkerException(String type, String message) {
		super(message);
		this.type = type;
	}

	public WorkerException(String type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	public WorkerException(String type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
