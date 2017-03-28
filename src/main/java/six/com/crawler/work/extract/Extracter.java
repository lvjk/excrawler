package six.com.crawler.work.extract;

import java.util.HashSet;
import java.util.Set;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午9:19:23
 */
public interface Extracter {

	// 系统默认 采集数据id
	public static final String DEFAULT_RESULT_ID = "id";
	// 系统默认 采集日期 字段
	public static final String DEFAULT_RESULT_COLLECTION_DATE = "collectionDate";
	// 系统默认 数据源url 字段
	public static final String DEFAULT_RESULT_ORIGIN_URL = "originUrl";

	public static final Set<String> DEFAULT_RESULT_KEY_SET = new HashSet<String>() {
		
		private static final long serialVersionUID = -5572845760795261691L;
		{
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_ID);
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_COLLECTION_DATE);
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_ORIGIN_URL);
		}
	};

	/**
	 * 抽取page
	 * @param page
	 * @return
	 */
	public ResultContext extract(Page page);
}
