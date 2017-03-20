package six.com.crawler.downer;

import java.io.File;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import okhttp3.Request;
import six.com.crawler.BaseTest;
import six.com.crawler.http.HttpConstant;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.http.HttpResult;
import six.com.crawler.utils.AutoCharsetDetectorUtils.ContentType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月20日 上午11:02:59
 */
public class OkDownerTest extends BaseTest {

	@Test
	public void test() {
		String url = "http://newhouse.nc.fang.com/house/s/xihuqu/b81/b91/";
		Request request = httpClient.buildRequest(url, null, HttpMethod.GET, HttpConstant.headMap, null, null);
		HttpResult httpResult = null;
		String originalDir = "F:/test";
		File file = null;
		String fileName = null;
		String nowTime = null;
		String fileSuffix = ".txt";
		try {
			httpResult = httpClient.executeRequest(request);
			nowTime = String.valueOf(System.currentTimeMillis());
			fileName = nowTime + fileSuffix;
			file = new File(originalDir, fileName);
			String html=httpClient.getHtml(httpResult, ContentType.HTML);
			FileUtils.write(new File("f://"+String.valueOf(System.currentTimeMillis())+".txt"),html);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
