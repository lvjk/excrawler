package six.com.crawler.rpc.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午4:56:39
 */
public class WrapperServerService {

	private Object targetOb;
	private Method targetMethod;

	public WrapperServerService(Object targetOb, Method targetMethod) {
		this.targetOb = targetOb;
		this.targetMethod = targetMethod;
	}

	public Object invoke(Object[] paras)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return targetMethod.invoke(targetOb, paras);
	}
}
