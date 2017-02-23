package six.com.crawler.work.store;


import java.util.List;

import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractWorker;

/**
 * @author six
 * @date 2016年8月22日 下午3:17:03
 */
public class FileStoreProcessor extends StoreAbstarct {

	public FileStoreProcessor(AbstractWorker worker,List<String> resultKeys) {
		super(worker,resultKeys);
	}

	@Override
	protected int insideStore(ResultContext resultContext) throws StoreException {
		System.out.println(resultContext);
		return 1;
	}
}
