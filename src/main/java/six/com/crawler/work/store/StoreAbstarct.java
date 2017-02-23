package six.com.crawler.work.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractWorker;

/**
 * @author six
 * @date 2016年8月26日 上午9:24:48
 */
public abstract class StoreAbstarct {

	final static Logger LOG = LoggerFactory.getLogger(StoreAbstarct.class);
	private AbstractWorker worker;
	// 处理的结果key
	List<String> resultKeys;
	private boolean resultIsList;
	// 主要的结果key
	private String mainResultKey;
	// 主要的结果key
	private Set<String> mainResultKeys;

	public StoreAbstarct(AbstractWorker worker, List<String> resultKeys) {
		this.worker = worker;
		String resultIsListStr = worker.getJob().getParameter(JobConTextConstants.RESULT_IS_LIST, String.class);
		if ("1".equals(resultIsListStr)) {
			resultIsList = true;
		}
		String mainResultKey = worker.getJob().getParameter(JobConTextConstants.MAIN_RESULT_KEY, String.class);
		if (StringUtils.isNotBlank(mainResultKey)) {
			String[] mainResultKeys = mainResultKey.split(";");
			this.mainResultKey = mainResultKeys[0];
			this.mainResultKeys = new HashSet<>(mainResultKeys.length);
			for (String key : mainResultKeys) {
				this.mainResultKeys.add(key);
			}
		}
		this.resultKeys = resultKeys;
	}

	/**
	 * 内部存储处理方法
	 * 
	 * @param t
	 */
	protected abstract int insideStore(ResultContext resultContext)throws StoreException;

	/**
	 * 内部处理方法
	 * 
	 * @param t
	 */
	public final int store(ResultContext resultContext) throws StoreException{
		int storeCount = 0;
		if (resultContext.haveResult()) {
			int size = 1;
			if (null != getMainResultKey()) {
				List<String> mainResultList = resultContext.getResult(getMainResultKey());
				if (null != mainResultList) {
					size = mainResultList.size();
					List<String> resultList = null;
					for (String resultKey : resultKeys) {
						if (!getMainResultKey().equalsIgnoreCase(resultKey)) {
							resultList = resultContext.getResult(resultKey);
							int tempSize = 0;
							if (null == resultList) {
								resultList = new ArrayList<>();
								resultContext.addResult(resultKey, resultList);
								tempSize = size;
								for (int i = 0; i < tempSize; i++) {
									resultList.add("");
								}
							} else if (resultList.size() < size) {
								tempSize = size - resultList.size();
								for (int i = 0; i < tempSize; i++) {
									resultList.add("");
								}
							} else if (resultList.size() > size) {
								resultList = resultList.subList(0, size - 1);
								resultContext.addResult(resultKey, resultList);
							}
						}
					}
				}
			}
			storeCount = insideStore(resultContext);
		}
		return storeCount;
	}

	public AbstractWorker getAbstractWorker() {
		return worker;
	}

	protected boolean getResultIsList() {
		return resultIsList;
	}

	protected Set<String> getMainResultKeys() {
		return mainResultKeys;
	}

	protected String getMainResultKey() {
		return mainResultKey;
	}
}
