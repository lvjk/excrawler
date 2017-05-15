package six.com.crawler.work.space;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 下午3:05:34
 */
public class SystemUtils {

	private static String MAC;
	private static String PID;

	static {
		MAC = getLocalMac();
		PID = getPid();

		String name = ManagementFactory.getRuntimeMXBean().getName();
		PID = name.split("@")[0];
	}

	public static String getMac() {
		return MAC;
	}

	public static String getPid() {
		return PID;
	}

	private static String getLocalMac() {
		String mac = "";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			byte[] macBytes = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < macBytes.length; i++) {
				int temp = macBytes[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			mac = sb.toString().toUpperCase();
		} catch (Exception e) {
		}
		return mac;
	}
}
