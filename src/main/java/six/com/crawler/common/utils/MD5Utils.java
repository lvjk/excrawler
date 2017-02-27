package six.com.crawler.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2015年12月30日 下午9:42:42 类说明
 */
public class MD5Utils {

	static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static MessageDigest getMessageDigest() {
		MessageDigest mdInst = null;
		try {
			mdInst = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// 忽略
		}
		return mdInst;
	}

	public final static String MD5(String str) {
		char[] data = md5_char(str);
		return new String(data);
	}

	public final static char[] md5_char(String str) {
		char[] result = null;
		if (null != str) {
			byte[] btInput = str.getBytes();
			MessageDigest mdInst = getMessageDigest();
			if (null != mdInst) {
				mdInst.reset();
				mdInst.update(btInput);
				byte[] md = mdInst.digest();
				int j = md.length;
				char cs[] = new char[j * 2];
				int k = 0;
				for (int i = 0; i < j; i++) {
					byte byte0 = md[i];
					cs[k++] = hexDigits[byte0 >>> 4 & 0xf];
					cs[k++] = hexDigits[byte0 & 0xf];
				}
				result = cs;
			} else {
				result = str.toCharArray();
			}
		} else {
			result = new char[0];
		}
		return result;
	}

	public final static String MD5_16(String str) {
		char[] data = md5_char(str);
		if(data!=null){
			return new String(data, 16, 16);
		}else{
			return str;
		}
	}

	public final static String MD5_6(String str) {
		char[] data = md5_char(str);
		if(data!=null){
			return new String(data, 12, 6);
		}else{
			return str;
		}
	}

	public final static String MD5_8(String str) {
		char[] data = md5_char(str);
		if(data!=null){
			return new String(data, 12, 8);
		}else{
			return str;
		}
	}
}
