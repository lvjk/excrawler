package six.com.crawler.work.store;


import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午11:32:28
 */
public class StoreFactory {

	/**
	 * 生成一个抽取器
	 * 
	 * @param worker
	 * @param extractItems
	 * @param extracterType
	 * @return
	 */
	public static Store newStore(AbstractCrawlWorker worker,StoreType storeType) {
		Store store = null;
		if (StoreType.CONSOLE == storeType) {
			store = new ConsoleStore(worker);
		} else if (StoreType.DB == storeType) {
			store = new DataBaseStore(worker);
		} else if (StoreType.HTTP == storeType) {
			store = new HttpStore(worker);
		} else if (StoreType.FILE == storeType) {
			store = new FileStore(worker);
		}  else if (StoreType.REDIS == storeType) {
			store = new RedisStore(worker);
		} else {
			store = new ConsoleStore(worker);
		}
		return store;
	}
}
