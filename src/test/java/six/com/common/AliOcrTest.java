package six.com.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import okhttp3.Request;
import six.com.crawler.BaseTest;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.ocr.ImageUtils;
import six.com.crawler.common.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.work.downer.PostContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月5日 上午11:36:57
 */
public class AliOcrTest extends BaseTest {

	@Test
	public void test() {
//		String path="f://test/ocr/aliyuntest.png";
//		String base64 = ImageUtils.imageToBase64(path);
//		System.out.println(base64);
//		String host = "http://ali-checkcode.showapi.com/checkcode";
//		Map<String, String> headers = new HashMap<String, String>();
//		headers.put("Authorization", "APPCODE f3c27fa8a841412b82437c87209fecc3");
//		Map<String, Object> bodys = new HashMap<String, Object>();
//		bodys.put("convert_to_jpg", "0");
//		bodys.put("img_base64",base64);
//		bodys.put("typeId", "3040");
//		try {
//			/**
//			 * 重要提示如下: HttpUtils请从
//			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
//			 * 下载
//			 *
//			 * 相应的依赖请参照
//			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
//			 */
//			Request request = httpClient.buildRequest(host, null, HttpMethod.POST, headers, PostContentType.FORM, bodys,
//					null, null);
//			HttpResult httpResult = httpClient.executeRequest(request);
//			String result = httpClient.getHtml(httpResult, ContentType.OTHER);
//			System.out.println(result);
//			// 获取response的body
//			// System.out.println(EntityUtils.toString(response.getEntity()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
