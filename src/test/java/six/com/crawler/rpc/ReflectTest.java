package six.com.crawler.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 下午1:05:34
 */
public class ReflectTest {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		TestService testService = new TestServiceImpl();
		int count = 1000000000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			testService.say("test");
		}
		long end = System.currentTimeMillis();
		System.out.println("直接调用用时:" + (end - start));

		Method method = testService.getClass().getMethod("say", String.class);
		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			method.invoke(testService, "test");
		}
		end = System.currentTimeMillis();
		System.out.println("反射调用用时:" + (end - start));
	}

}
