package six.com.crawler.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import okhttp3.Request;
import six.com.crawler.BaseTest;
import six.com.crawler.common.http.HttpConstant;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.common.utils.ThreadUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月27日 下午12:18:58
 */
public class ImageDown extends BaseTest {

	@Test
	public void test() {
//		String url = "http://www.cq315house.com/315web/YanZhengCode/ValidCode.aspx";
//		String referer = "http://www.cq315house.com/315web/HtmlPage/RoomInfo.aspx?fid=22922843&bid=75335c4aeec47a9fadcb566143d26646";
//		Request request = httpClient.buildRequest(url, referer, HttpMethod.GET, HttpConstant.headMap, null, null, null);
//		HttpResult httpResult = null;
//		String originalDir = "F:/six/ocr/image/test/";
//		File image = null;
//		String fileName = null;
//		int count = 10000;
//		String nowTime = null;
//		String imageSuffix = ".jpg";
//		for (int i = 0; i < count; i++) {
//			try {
//				httpResult = httpClient.executeRequest(request);
//				nowTime = String.valueOf(System.currentTimeMillis());
//				fileName = originalDir + nowTime + imageSuffix;
//				image = new File(fileName);
//				FileUtils.writeByteArrayToFile(image, httpResult.getData());
//				BufferedImage croppedImage = ImageIO.read(new FileInputStream(image));
//				long start = System.currentTimeMillis();
//				String result = imageDistinguish.distinguish(croppedImage);
//				long end = System.currentTimeMillis();
//				System.out.println("image[" + fileName + "] distinguish result:" + result + "|time:" + (end - start));
//				ThreadUtils.sleep(5000);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

	

}
