package six.com.crawler.utils;

import java.util.Collection;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午10:13:16
 */
public class CollectionsCheckUtils {

	public static <T extends Collection<?>> T checkNull(T collection) {
		if (null == collection) {
			throw new NullPointerException();
		}
		return collection;
	}

	public static <T extends Collection<?>> T checkNullOrEmpty(T collection) {
		if (null == collection || collection.isEmpty()) {
			throw new IllegalArgumentException();
		}
		return collection;
	}
}
