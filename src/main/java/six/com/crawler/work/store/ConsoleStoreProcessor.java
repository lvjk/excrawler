package six.com.crawler.work.store;

import java.util.List;
import java.util.Map;

import six.com.crawler.work.AbstractWorker;

/**
 * @author six
 * @date 2016年8月23日 下午12:16:21
 */
public class ConsoleStoreProcessor extends StoreAbstarct {

	public ConsoleStoreProcessor(AbstractWorker worker, List<String> resultKeys) {
		super(worker, resultKeys);
	}

	@Override
	protected int insideStore(List<Map<String, String>> results) {
		System.out.println(results);
		return 1;
	}

}
