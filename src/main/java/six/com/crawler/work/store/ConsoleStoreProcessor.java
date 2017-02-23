package six.com.crawler.work.store;


import java.util.List;

import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractWorker;

/**
 * @author six
 * @date 2016年8月23日 下午12:16:21
 */
public class ConsoleStoreProcessor extends StoreAbstarct {

	public ConsoleStoreProcessor(AbstractWorker worker,List<String> resultKeys) {
		super(worker,resultKeys);
	}

	@Override
	protected int insideStore(ResultContext resultContext){
		System.out.println(resultContext);
		return 1;
	}

}
