package six.com.crawler.http;

import six.com.crawler.exception.HttpReadDataMoreThanMaxException;

/**
 * @author six
 * @date 2016年8月18日 上午11:10:01
 */
public class HttpDecodingUtils {

	static String  Sdch="SDCH";

	public static byte[] decodeing(String contentEncoding, byte[] src) throws HttpReadDataMoreThanMaxException {
		 if (Sdch.equalsIgnoreCase(contentEncoding)) {
			return decodingBySdch(src);
		}else {
			return src;
		}
	}


	private static byte[] decodingBySdch(byte[] src) {
		return src;
	}

}
