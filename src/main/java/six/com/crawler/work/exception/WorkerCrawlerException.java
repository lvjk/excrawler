package six.com.crawler.work.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月19日 上午11:46:24
 */
public abstract class WorkerCrawlerException extends WorkerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6837835105330279257L;

	public WorkerCrawlerException(String type, String message) {
		super(type, message);
	}

	public WorkerCrawlerException(String type, String message, Throwable cause) {
		super(type, message, cause);
	}

}
