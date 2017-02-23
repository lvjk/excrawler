package six.com.crawler.store;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年4月20日 下午8:19:49 类说明
 */
public class LocalLinkFiter {

	final static int maxChar = 16;

	static BigDecimal max = new BigDecimal(maxChar);

	//static Map<String, Short> map = new HashMap<String, Short>();
	
	static Map<String, BitSet> map = new HashMap<String, BitSet>();

	public static void main(String[] a) throws IOException {
		int max = 20000000;
		String temp = "http://geek.csdn.net/news/detail/294661";
		String newStr = null;
		String MD5 = null;
		for (int i = 0; i <max; i++) {
			newStr=temp+i;
			MD5=DigestUtils.md5Hex(newStr);
			save(MD5);
		}
		//temp = "http://geek.csdn1.net/news/detail/294661";
		for (int i = 0; i <max; i++) {
			newStr=temp+i;
			MD5=DigestUtils.md5Hex(newStr);
			if (isLive(MD5)) {
				System.out.println("存在");
			} else {
				System.out.println("不存在");
			}
		}
	}

	public static boolean isLive(String MD5) {

		final int size = MD5.length();
		char c = 0;
		BigDecimal key = null;
		BigDecimal lastLayerIndex = null;
		int lastOffsetIndex = 1;
		int tempOffset = 1;
		BigDecimal beforeCout = null;
		BigDecimal temp = null;
		BigDecimal tempKey = null;
		BitSet bit16=null;
		for (int depth = 0; depth < size; depth++) {
			beforeCout = new BigDecimal(0);
			c = MD5.charAt(depth);
			tempOffset = getOffset(c);
			tempKey = getKey(depth, lastLayerIndex, lastOffsetIndex, c);
			for (int i = depth - 1; i >= 0; i--) {
				temp = max.pow(i);
				beforeCout = beforeCout.add(temp);
			}
			key = beforeCout.add(tempKey);// 273
			bit16 = map.get(key.toString());
			if (null==bit16||!bit16.get(tempOffset)) {// 判断是否存在
				return false;
			}
			//int takeValue=bit16.intValue();
			//takeValue=takeValue<<maxChar-tempOffset>>maxChar;
			//if(takeValue==0){
			//	return false;
			//}
			lastOffsetIndex = tempOffset;
			lastLayerIndex = tempKey;
		}
		return true;
	}

	public static void save(String MD5) {
		final int size = MD5.length();
		char c = 0;
		BigDecimal key = null;
		BigDecimal lastLayerIndex = null;
		int lastOffsetIndex = 1;
		int tempOffset = 1;
		BigDecimal beforeCout = null;
		BigDecimal temp = null;
		BigDecimal tempKey = null;
		BitSet bit16=null;
		for (int depth = 0; depth < size; depth++) {
			beforeCout = new BigDecimal(0);
			c = MD5.charAt(depth);
			tempOffset = getOffset(c);
			tempKey = getKey(depth, lastLayerIndex, lastOffsetIndex, c);
			for (int i = depth - 1; i >= 0; i--) {
				temp = max.pow(i);
				beforeCout = beforeCout.add(temp);
			}
			key = beforeCout.add(tempKey);
			bit16 = map.get(key.toString());
			if (bit16 == null) {
				//bit16=new Short((short)(1<<tempOffset));
				bit16=new BitSet(maxChar);
			}
			//else{
			//	short newValue=(short)bit16.intValue();
			//	newValue=(short)(bit16.intValue()|1<<tempOffset);
			//	bit16=new Short(newValue);
			//}
			bit16.set(tempOffset, true);
			map.put(key.toString(), bit16);
			lastOffsetIndex = tempOffset;
			lastLayerIndex = tempKey;
		}
	}

	public static BigDecimal getKey(int depath, BigDecimal lastLayerIndex, int lastOffsetIndex, char c) {
		if (depath == 0) {
			return new BigDecimal(1);
		}
		// (lastLayerIndex-1)*maxDepth+lastOffsetIndex;
		BigDecimal defaultBegin = new BigDecimal(1);
		BigDecimal index = lastLayerIndex.subtract(defaultBegin);
		index = index.multiply(max);
		index = index.add(new BigDecimal(lastOffsetIndex));
		return index;
	}

	public static int getOffset(char c) {
		int temp = c;
		if (c <= 57) {
			return temp - 47;
		} else {
			return temp - 86;
		}
	}

}
