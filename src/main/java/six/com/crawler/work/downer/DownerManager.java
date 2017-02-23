package six.com.crawler.work.downer;

import java.util.concurrent.locks.StampedLock;
import six.com.crawler.work.HtmlCommonWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月14日 上午10:53:52
 */
public class DownerManager {

	static final StampedLock stampedLock = new StampedLock();

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static DownerManager getInstance() {
		return DownerManagerUtils.DownerManager;
	}

	/**
	 * 单例模式 实现赖加载
	 * 
	 * @author six
	 * @email 359852326@qq.com
	 */
	private static class DownerManagerUtils {
		static DownerManager DownerManager = new DownerManager();
	}

	public Downer buildDowner(DownerType downerType, HtmlCommonWorker worker) {
		Downer downer = null;
		if (downerType == DownerType.OKHTTP) {
			downer = new OkHttpDowner(worker);
		} else if (downerType == DownerType.HTTPCLIENT) {
			downer = new ApacheHttpDowner(worker);
		} else if (downerType == DownerType.CHROME) {
			downer = new ChromeDowner(worker);
		} else {
			downer = new OkHttpDowner(worker);
		}
		return downer;
	}

}
