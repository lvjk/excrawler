package six.com.crawler.work;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月28日 下午10:49:22
 */
public class Constants {

	public static final int WOKER_PROCESS_PAGE_MAX_RETRY_COUNT = 3;
	/**
	 * 存储在redis 正在处理的省份
	 */
	public static final String REDIS_PROCESSOR_OPERATION_DOING_PROVINCE_KEY = "spider_redis_processor_operation_doing_province_";

	/**
	 * 存储在redis 处理过的省份
	 */
	public static final String REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY = "spider_redis_processor_operation_done_province_";

	/**
	 * 存储在redis 是否还有下一页
	 */
	public static final String REDIS_PROCESSOR_OPERATION_HAS_NEXT_PAGE_KEY = "spider_redis_processor_operation_has_next_page_";

	public static final String ITEM_SEPARATE = "<_item_>";

	// 系统默认 采集数据id
	public static final String DEFAULT_RESULT_ID= "id";
	// 系统默认 采集日期 字段
	public static final String DEFAULT_RESULT_COLLECTION_DATE = "collectionDate";
	// 系统默认 数据源url 字段
	public static final String DEFAULT_RESULT_ORIGIN_URL = "originUrl";
	
	public static final Set<String> DEFAULT_RESULT_KEY_SET;
	
	static{
		DEFAULT_RESULT_KEY_SET=new HashSet<>();
		DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_ID);
		DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_COLLECTION_DATE);
		DEFAULT_RESULT_KEY_SET.add(DEFAULT_RESULT_ORIGIN_URL);
	}
}
