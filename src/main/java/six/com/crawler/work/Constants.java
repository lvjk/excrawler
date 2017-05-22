package six.com.crawler.work;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月28日 下午10:49:22
 */
public class Constants {

	/** 获取元素超时 **/
	public static final int FIND_ELEMENT_TIMEOUT= 1000;
	
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
}
