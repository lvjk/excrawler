package six.com.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okio.Buffer;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月20日 下午1:38:08
 */
public class Test {

	static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
			'E', 'F' };

	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(2398229&2048);
		System.out.println(1==1.1);
		System.out.println(1==1.0);
		Map<String, Object> postMap = new HashMap<>();
		postMap.put("/wEPDwUKLTQyNDAzOTY4MWRkn7WIqRPgtCTZCkeFSpZWUJ6nXFo=", "%2FwEPDwUKLTQyNDAzOTY4MWRkn7WIqRPgtCTZCkeFSpZWUJ6nXFo%3D");
		postMap.put("/wEWBAKJ+6a1BwLChPzDDQKM54rGBgKdxMCnCTySME/SgJqMruiLF0W8fuPDop1O", "%2FwEWBAKJ%2B6a1BwLChPzDDQKM54rGBgKdxMCnCTySME%2FSgJqMruiLF0W8fuPDop1O");
		postMap.put("确定", "%E7%A1%AE%E5%AE%9A");
		for(String key:postMap.keySet()){
			String result=canonicalize(key, FORM_ENCODE_SET, true, false, true, true);
			if(postMap.get(key).equals(result)){
				System.out.println("yes");
			}else{
				System.out.println("no:["+result+":"+key+"]");
			}
		}
		System.out.println("----------------------------------------------");
		for(String key:postMap.keySet()){
			String result=URLEncoder.encode(key, "utf-8");
			if(postMap.get(key).equals(result)){
				System.out.println("yes");
			}else{
				System.out.println("no:["+result+":"+key+"]");
			}
		}
	}

	static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict,
			boolean plusIsSpace, boolean asciiOnly) {
		return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly);
	}

	static String canonicalize(String input, int pos, int limit, String encodeSet, boolean alreadyEncoded,
			boolean strict, boolean plusIsSpace, boolean asciiOnly) {
		int codePoint;
		for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
			codePoint = input.codePointAt(i);
			if (codePoint < 0x20 || codePoint == 0x7f || codePoint >= 0x80 && asciiOnly
					|| encodeSet.indexOf(codePoint) != -1
					|| codePoint == '%' && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))
					|| codePoint == '+' && plusIsSpace) {
				// Slow path: the character at i requires encoding!
				Buffer out = new Buffer();
				out.writeUtf8(input, pos, i);
				canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly);
				return out.readUtf8();
			}
		}

		// Fast path: no characters in [pos..limit) required encoding.
		return input.substring(pos, limit);
	}

	static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet, boolean alreadyEncoded,
			boolean strict, boolean plusIsSpace, boolean asciiOnly) {
		Buffer utf8Buffer = null; // Lazily allocated.
		int codePoint;
		for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
			codePoint = input.codePointAt(i);
			if (alreadyEncoded && (codePoint == '\t' || codePoint == '\n' || codePoint == '\f' || codePoint == '\r')) {
				// Skip this character.
			} else if (codePoint == '+' && plusIsSpace) {
				// Encode '+' as '%2B' since we permit ' ' to be encoded as
				// either '+' or '%20'.
				out.writeUtf8(alreadyEncoded ? "+" : "%2B");
			} else if (codePoint < 0x20 || codePoint == 0x7f || codePoint >= 0x80 && asciiOnly
					|| encodeSet.indexOf(codePoint) != -1
					|| codePoint == '%' && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))) {
				// Percent encode this character.
				if (utf8Buffer == null) {
					utf8Buffer = new Buffer();
				}
				utf8Buffer.writeUtf8CodePoint(codePoint);
				while (!utf8Buffer.exhausted()) {
					int b = utf8Buffer.readByte() & 0xff;
					out.writeByte('%');
					out.writeByte(HEX_DIGITS[(b >> 4) & 0xf]);
					out.writeByte(HEX_DIGITS[b & 0xf]);
				}
			} else {
				// This character doesn't need encoding. Just copy it over.
				out.writeUtf8CodePoint(codePoint);
			}
		}
	}

	static boolean percentEncoded(String encoded, int pos, int limit) {
		return pos + 2 < limit && encoded.charAt(pos) == '%' && decodeHexDigit(encoded.charAt(pos + 1)) != -1
				&& decodeHexDigit(encoded.charAt(pos + 2)) != -1;
	}

	static int decodeHexDigit(char c) {
		if (c >= '0' && c <= '9')
			return c - '0';
		if (c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		if (c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		return -1;
	}

}
