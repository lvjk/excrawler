package six.com.crawler.work.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import six.com.crawler.work.AbstractWorker;
import six.com.crawler.work.store.exception.StoreException;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午11:36:28 
*/
public class ConsoleStore extends StoreAbstarct{

	public ConsoleStore(AbstractWorker worker, List<String> resultKeys) {
		super(worker, resultKeys);
	}

	@Override
	protected int insideStore(List<Map<String, String>> results) throws StoreException {
		int storeCount = 0;
		if (null != results) {
			List<Object> parameters = new ArrayList<>();
			for (Map<String, String> dataMap : results) {
				parameters.clear();
				for (String resultKey : getResultList()) {
					String param = dataMap.get(resultKey);
					parameters.add(param);
				}
				System.out.println(parameters);
				storeCount ++;
			}
		}
		return storeCount;
	}

}
