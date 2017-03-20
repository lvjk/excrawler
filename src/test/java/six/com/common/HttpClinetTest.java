package six.com.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import okhttp3.Request;
import six.com.crawler.BaseTest;
import six.com.crawler.http.HttpConstant;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.http.HttpResult;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;
import six.com.crawler.work.downer.PostContentType;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年1月20日 下午12:09:43 
*/
public class HttpClinetTest extends BaseTest {



	@Test
	public void test() {
//		String url="http://www.cq315house.com/315web/YanZhengCode/YanZhengPage.aspx?fid=26666335";
//		Map<String, String> headMap =new HashMap<>();
//		headMap.putAll(HttpConstant.headMap);
//		headMap.put("Content-Type", "application/x-www-form-urlencoded");
//		headMap.put("Origin", "http://www.cq315house.com");
//		headMap.put("Cookie", "ASP.NET_SessionId=xqvvdym4j1m0xe55q4qw1z45");
//		headMap.put("Content-Length", "237");
//		headMap.put("Host", "www.cq315house.com");
//		Map<String, Object> bodys = new HashMap<String, Object>();
//		bodys.put("__VIEWSTATE", "/wEPDwUKLTQyNDAzOTY4MWRkn7WIqRPgtCTZCkeFSpZWUJ6nXFo=");
//		bodys.put("__VIEWSTATEGENERATOR","150E47F4");
//		bodys.put("__EVENTVALIDATION", "/wEWBAKJ+6a1BwLChPzDDQKM54rGBgKdxMCnCTySME/SgJqMruiLF0W8fuPDop1O");
//		bodys.put("txtCode", "0");
//		bodys.put("Button1","确定");
//		bodys.put("hfTableNum", "0");
//		try {
//			/**
//			 * 重要提示如下: HttpUtils请从
//			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
//			 * 下载
//			 *
//			 * 相应的依赖请参照
//			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
//			 */
//			Request request = httpClient.buildRequest(url, url, HttpMethod.POST, headMap, PostContentType.FORM, bodys);
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
