package six.com.crawler.utils;

import java.util.HashSet;
import java.util.Set;

public class TelPhoneUtils {

	// 电话 前缀分隔符
	static Set<String> set = new HashSet<>();
	static {
		set.add("—");
		set.add("-");
		set.add("*");
	}

	public static boolean isTelPhone(String str) {
		if (str == null || str.trim().length() == 0) {
			return false;
		}
		// 全是数字
		for (int i = 0; i < str.length(); i++) {
			char tempChar = str.charAt(i);
			if ((i == 3 || i == 4 || i == 9) && set.contains(String.valueOf(tempChar))) {

			} else if ((i == 3 + 8 || i == 4 + 8) && set.contains(String.valueOf(tempChar))) {

			} else if (tempChar < 48 || tempChar > 57) {
				return false;
			}
		}
		// 带有
		return true;
	}

	public static void main(String[] s) {
		String num = "01233";
		if (isTelPhone(num)) {
			System.out.println(num + " 是数字");
		} else {
			System.out.println(num + " 不是数字");
		}
	}
}
