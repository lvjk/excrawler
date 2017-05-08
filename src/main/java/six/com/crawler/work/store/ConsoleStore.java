package six.com.crawler.work.store;

import java.util.List;
import java.util.Map;

import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.store.exception.StoreException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午11:36:28 
*/
public class ConsoleStore extends AbstarctStore{

	public ConsoleStore(AbstractWorker<?> worker) {
		super(worker);
	}

	@Override
	protected int insideStore(List<Map<String, String>> results) throws StoreException {
		int storeCount = 0;
		if (null != results) {
			for (Map<String, String> dataMap : results) {
				System.out.println(dataMap);
				storeCount ++;
			}
		}
		return storeCount;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
