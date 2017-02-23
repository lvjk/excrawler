package six.com.crawler.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author six
 * @date 2016年8月1日 下午2:33:36
 */
public class PersonClass {

	private static Map<Integer, String> map = new HashMap<Integer, String>();

	static {
		map.put(1, "年收入5万以内");
		map.put(2, "年收入10万以内");
		map.put(3, "年收入20万以内");
		map.put(4, "年收入50万以内");
		map.put(5, "年收入100万以内");
	}
}
