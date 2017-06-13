package six.com.crawler.schedule;

import java.io.Serializable;

import org.apache.commons.lang3.time.DateFormatUtils;

import six.com.crawler.common.DateFormats;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月15日 上午10:46:29
 * 
 *       job被触发执行的 类型
 * 
 */
public class TriggerType implements Serializable {

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
	 * worker触发名称
	 */
	public final static String DISPATCH_TYPE_WORKER= "worker";

	/**
	 * new 一个被job触发的类型DispatchType
	 * 
	 * @param jobName
	 * @return
	 */
	public static TriggerType newDispatchTypeByJob(String name, String currentTimeMillis) {
		return new TriggerType(name, currentTimeMillis);
	}

	/**
	 * new 一个被动手动触发类型DispatchType
	 * 
	 * @return
	 */
	public static TriggerType newDispatchTypeByManual() {
		return new TriggerType(DISPATCH_TYPE_MANUAL,
				DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2));
	}

	/**
	 * new 一个被动调度器触发类型DispatchType
	 * 
	 * @return
	 */
	public static TriggerType newDispatchTypeByScheduler() {
		return new TriggerType(DISPATCH_TYPE_SCHEDULER,
				DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2));
	}

	/**
	 * new 一个被动master触发类型DispatchType
	 * 
	 * @return
	 */
	public static TriggerType newDispatchTypeByMaster() {
		return new TriggerType(DISPATCH_TYPE_MASTER,
				DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2));
	}
	
	
	/**
	 * new 一个被动master触发类型DispatchType
	 * 
	 * @return
	 */
	public static TriggerType newDispatchTypeByWorker() {
		return new TriggerType(DISPATCH_TYPE_WORKER,
				DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_2));
	}

	private final String name;
	private final String currentTimeMillis;

	public TriggerType(String name, String currentTimeMillis) {
		this.name = name;
		this.currentTimeMillis = currentTimeMillis;
	}

	public String getName() {
		return name;
	}

	public String getCurrentTimeMillis() {
		return currentTimeMillis;
	}

	@Override
	public int hashCode() {
		if (null != name) {
			return name.hashCode();
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (null != o) {
			if (o instanceof TriggerType) {
				TriggerType dispatchType = (TriggerType) o;
				if (null != name && name.equals(dispatchType.getName())) {
					return true;
				}
			}
		}
		return false;
	}

}
