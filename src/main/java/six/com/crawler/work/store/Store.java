package six.com.crawler.work.store;

import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.store.exception.StoreException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午11:32:49
 */
public interface Store {

	public int store(ResultContext resultContext) throws StoreException;

	public void close();
}
