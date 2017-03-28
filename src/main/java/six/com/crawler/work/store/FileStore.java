package six.com.crawler.work.store;

import java.util.List;
import java.util.Map;

import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.store.exception.StoreException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午11:37:45 
*/
public class FileStore extends StoreAbstarct{

	public FileStore(AbstractWorker worker, List<String> resultKeys) {
		super(worker, resultKeys);
	}

	@Override
	protected int insideStore(List<Map<String, String>> results) throws StoreException {
		return 0;
	}

}
