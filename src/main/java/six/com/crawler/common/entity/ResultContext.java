package six.com.crawler.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月13日 下午3:25:23 结果容器
 */
public class ResultContext {

	/**
	 * 抽取出来的结果
	 */
	private Map<String, List<String>> results = new HashMap<String, List<String>>();
	/**
	 * 输出保存结果
	 */
	private List<Map<String, String>> outResults = new ArrayList<>();

	public void addExtractResult(String key, List<String> result) {
		if (null == results) {
			results = new HashMap<String, List<String>>();
		}
		results.put(key, result);
	}

	public List<String> getExtractResult(String resultKey) {
		return results.get(resultKey);
	}

	public List<String> takeExtractResult(String resultKey) {
		return results.remove(resultKey);
	}

	public void addoutResult(Map<String, String> dataMap) {
		outResults.add(dataMap);
	}

	public List<Map<String, String>> getOutResults() {
		return outResults;
	}

}
