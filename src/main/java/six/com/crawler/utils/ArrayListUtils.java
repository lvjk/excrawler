package six.com.crawler.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月5日 下午3:56:05
 */
public class ArrayListUtils {

	@SafeVarargs
    @SuppressWarnings("varargs")
	public static <T> List<T> asList(T... a) {
		if (null == a) {
			throw new NullPointerException();
		}
		List<T> list = new ArrayList<>();
		for (T t : a) {
			list.add(t);
		}
		return list;
	}
}
