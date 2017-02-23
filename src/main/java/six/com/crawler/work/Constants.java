package six.com.crawler.work;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月28日 下午10:49:22
 */
public interface Constants {

	final static String RESULT_ID_KEY = "id";
	
	int WOKER_PROCESS_PAGE_MAX_RETRY_COUNT = 3;
	/**
	 * 存储在redis 正在处理的省份
	 */
	String REDIS_PROCESSOR_OPERATION_DOING_PROVINCE_KEY = "spider_redis_processor_operation_doing_province_";

	/**
	 * 存储在redis 处理过的省份
	 */
	String REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY = "spider_redis_processor_operation_done_province_";

	/**
	 * 存储在redis 是否还有下一页
	 */
	String REDIS_PROCESSOR_OPERATION_HAS_NEXT_PAGE_KEY = "spider_redis_processor_operation_has_next_page_";

	String ITEM_SEPARATE = "<_item_>";

	// 系统默认 采集数据id
	public static final String RESULT_ID = "id";
	// 系统默认 采集日期 字段
	public static final String COLLECTION_DATE_FIELD = "collectionDate";
	// 系统默认 数据源url 字段
	public static final String ORIGIN_URL = "originUrl";
	// 系统默认 列表页URL 字段
	public static final String LISTING_URL = "listingUrl";
	// 系统默认 数据页URL 字段
	public static final String DATA_URL = "dataUrl";
	// 系统默认 下一页 URL字段
	public static final String NEXT_URL = "nextUrl";
}
