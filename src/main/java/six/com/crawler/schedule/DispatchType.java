package six.com.crawler.schedule;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月15日 上午10:46:29
 * 
 *       job被触发执行的 类型
 * 
 */
public class DispatchType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6468764725271901363L;

	/**
	 * 手动触发名称
	 */
	public final static String DISPATCH_TYPE_MANUAL = "MANUAL";

	/**
	 * 调度触发名称
	 */
	public final static String DISPATCH_TYPE_SCHEDULER = "SCHEDULER";

	/**
	 * master触发名称
	 */
	public final static String DISPATCH_TYPE_MASTER = "MASTER";

	/**
	 * new 一个被job触发的类型DispatchType
	 * 
	 * @param jobName
	 * @return
	 */
	public static DispatchType newDispatchTypeByJob(String name, String currentTimeMillis) {
		return new DispatchType(name, currentTimeMillis);
	}

	/**
	 * new 一个被动手动触发类型DispatchType
	 * 
	 * @return
	 */
	public static DispatchType newDispatchTypeByManual() {
		return new DispatchType(DISPATCH_TYPE_MANUAL, String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * new 一个被动调度器触发类型DispatchType
	 * 
	 * @return
	 */
	public static DispatchType newDispatchTypeByScheduler() {
		return new DispatchType(DISPATCH_TYPE_SCHEDULER, String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * new 一个被动master触发类型DispatchType
	 * 
	 * @return
	 */
	public static DispatchType newDispatchTypeByMaster() {
		return new DispatchType(DISPATCH_TYPE_MASTER, String.valueOf(System.currentTimeMillis()));
	}

	private final String name;
	private final String currentTimeMillis;

	public DispatchType(String name, String currentTimeMillis) {
		this.name = name;
		this.currentTimeMillis = currentTimeMillis;
	}

	public String getName() {
		return name;
	}

	public String getCurrentTimeMillis() {
		return currentTimeMillis;
	}

	public int hashCode() {
		if (null != name) {
			return name.hashCode();
		}
		return 0;
	}

	public boolean equals(Object o) {
		if (null != o) {
			if (o instanceof DispatchType) {
				DispatchType dispatchType = (DispatchType) o;
				if (null != name && name.equals(dispatchType.getName())) {
					return true;
				}
			}
		}
		return false;
	}

}
