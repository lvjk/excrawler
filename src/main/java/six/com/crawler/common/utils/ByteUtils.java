package six.com.crawler.common.utils;
/**
 *@author six    
 *@date 2016年6月16日 下午1:02:47  
*/
public class ByteUtils {

	public  static int bytesToInt(byte[] ary) {
		int value;
		int offset = 0;
		value = (int) ((ary[offset] & 0xFF) | ((ary[offset + 1] << 8) & 0xFF00) | ((ary[offset + 2] << 16) & 0xFF0000)
				| ((ary[offset + 3] << 24) & 0xFF000000));
		return value;
	}
}
