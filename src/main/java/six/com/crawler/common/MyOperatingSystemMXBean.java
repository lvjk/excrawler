package six.com.crawler.common;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @author six
 * @date 2016年6月7日 上午11:41:00 用于操作系统的管理接口，Java 虚拟机在此操作系统上运行。
 */
public class MyOperatingSystemMXBean {

	private static OperatingSystemMXBean osmxb = ManagementFactory.getOperatingSystemMXBean();

	/**
	 * 获取操作系统名字
	 * 
	 * @return
	 */
	public static String getSysTemName() {
		return osmxb.getName();
	}

	/**
	 * 获取操作系统架构
	 * 
	 * @return
	 */
	public static String getArch() {
		return osmxb.getArch();
	}

	/**
	 * 返回操作系统java可用的核心数
	 * 
	 * @return
	 */
	public static int getAvailableProcessors() {
		return osmxb.getAvailableProcessors();
	}
	
	/**
	 * 获取可用内存数量
	 * @return
	 */
	public static long freeMemory(){
		return Runtime.getRuntime().freeMemory();
	}
	
	/**
	 * 获取可用内存比例
	 * @return
	 */
	public static float freeMemoryPRP(){
		return Runtime.getRuntime().freeMemory()/Runtime.getRuntime().totalMemory();
	}
	
}
