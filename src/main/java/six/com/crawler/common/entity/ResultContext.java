package six.com.crawler.common.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月13日 下午3:25:23 结果容器
 */
public class ResultContext implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5480289276596003871L;

	/**
	 * 存储结果
	 */
	private Map<String, List<String>> results = new HashMap<String, List<String>>();

	public boolean haveResult() {
		return !results.isEmpty();
	}

	public void addResult(String key, List<String> resultWrapper) {
		if (null == results) {
			results = new HashMap<String, List<String>>();
		}
		results.put(key, resultWrapper);
	}

	public List<String> getResult(String resultKey) {
		return results.get(resultKey);
	}

	public List<String> takeResult(String resultKey) {
		return results.remove(resultKey);
	}

}
