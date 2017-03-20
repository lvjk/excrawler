package six.com.crawler.utils;

import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月10日 下午12:13:05
 */
public class PageQueryUtils {

	static Map<Class<?>,Class<?>> classMap=new HashMap<>();
	
	public static interface PageQuery {

		public void setTotalSize(int totalSize);

		public int getTotalSize();
	}

	public static Class<?> register(Class<?> registerClz) {
		Class<?> clz=classMap.get(registerClz);
		if(null==clz){
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(registerClz);
			enhancer.setInterfaces(new Class[] { PageQuery.class });
			clz= enhancer.createClass();
			classMap.put(registerClz, clz);
		}
		return clz;
	}
}
