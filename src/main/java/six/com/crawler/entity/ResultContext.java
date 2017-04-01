package six.com.crawler.entity;

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
	private Map<String, List<String>> extracterResult;
	/**
	 * 输出保存结果
	 */
	private List<Map<String, String>> outResults;

	public ResultContext() {
		extracterResult = new HashMap<String, List<String>>();
		outResults = new ArrayList<>();
	}

	/**
	 * 添加抽取结果
	 * 
	 * @param key
	 * @param result
	 */
	public void addExtractResult(String key, List<String> result) {
		extracterResult.put(key, result);
	}

	/**
	 * 获取抽取结果
	 * 
	 * @param resultKey
	 * @return
	 */
	public List<String> getExtractResult(String resultKey) {
		return extracterResult.get(resultKey);
	}

	/**
	 * 添加输出结果
	 * 
	 * @param dataMap
	 */
	public void addoutResult(Map<String, String> dataMap) {
		outResults.add(dataMap);
	}

	/**
	 * 获取输出结果
	 * 
	 * @return
	 */
	public List<Map<String, String>> getOutResults() {
		return outResults;
	}

}
