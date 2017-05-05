package six.com.crawler.work.extract;

import java.util.HashSet;
import java.util.Set;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午9:19:23
 * 抽取器会默认增加 
 * <p>采集数据id(此id跟业务无关)</p>
 * <p>采集数据的workerName</p>
 * <p>采集数据的日期</p>
 * <p>采集数据的数据源url段</p>
 */
public interface Extracter {

	/**
	 * 系统默认 采集数据id:由worker name +worker计数生成 ，跟业务无关
	 */
	public static final String DEFAULT_RESULT_ID = "id";	
	

	/**
	 * 系统默认 采集日期 字段
	 */
	public static final String DEFAULT_RESULT_COLLECTION_DATE = "collectionDate";
	
	/**
	 * 系统默认 数据源url 字段
	 */
	public static final String DEFAULT_RESULT_ORIGIN_URL = "originUrl";
	
	/**
	 * 引用URL
	 */
	public static final String DEFAULT_REAULT_REFERER_URL="refererUrl";

	public static final Set<String> DEFAULT_RESULT_KEY_SET = new HashSet<String>() {
		
		private static final long serialVersionUID = -5572845760795261691L;
		{
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_ID);
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_COLLECTION_DATE);
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_ORIGIN_URL);
			DEFAULT_RESULT_KEY_SET.add(DEFAULT_REAULT_REFERER_URL);
		}
	};

	/**
	 * 抽取page
	 * @param page
	 * @return
	 */
	public ResultContext extract(Page page);
}
