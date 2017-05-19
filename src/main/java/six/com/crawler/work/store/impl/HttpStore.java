package six.com.crawler.work.store.impl;

import java.util.List;
import java.util.Map;

import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.store.AbstarctStore;
import six.com.crawler.work.store.exception.StoreException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午11:37:37 
*/
public class HttpStore extends AbstarctStore{

	public HttpStore(AbstractWorker<?> worker) {
		super(worker);
	}

	@Override
	protected int insideStore(List<Map<String, String>> results) throws StoreException {
		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
