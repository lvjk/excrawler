package six.com.crawler.work.store;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.store.exception.StoreException;

/**
 * @author six
 * @date 2016年8月26日 上午9:24:48
 */
public abstract class AbstarctStore implements Store{

	final static Logger LOG = LoggerFactory.getLogger(AbstarctStore.class);
	private AbstractWorker<?> worker;
	
	public AbstarctStore(AbstractWorker<?> worker) {
		this.worker = worker;
	}

	/**
	 * 内部存储处理方法
	 * 
	 * @param t
	 */
	protected abstract int insideStore(List<Map<String, String>> results) throws StoreException;

	/**
	 * 内部处理方法
	 * 
	 * @param t
	 */
	public final int store(ResultContext resultContext) throws StoreException {
		return insideStore(resultContext.getOutResults());
	}

	public AbstractWorker<?> getAbstractWorker() {
		return worker;
	}

}
