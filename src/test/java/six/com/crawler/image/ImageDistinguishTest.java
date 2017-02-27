package six.com.crawler.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.common.ocr.ImageUtils;
import six.com.crawler.common.ocr.ProxyBufferedImage;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月27日 下午3:56:36
 */
public class ImageDistinguishTest extends BaseTest {

	public static final String CHAR_IMAGE_KEY = "ocr_char_image";

	@Test
	public void test() {
		// String dirPath = "F:/six/ocr/image/err/";
		// String dirPath = "F:/six/ocr/image/test/";
		imageDistinguish.loadToRedis("F:/six/ocr/image/char");
		// doErrImage(dirPath);
	}

	String charPath = "F:/six/ocr/image/test/char";

	public void doErrImage(String dir) {
		File imageDir = new File(dir);
		String[] files = imageDir.list();
		BufferedImage bufferedImage = null;
		long start = 0;
		long end = 0;
		String result = null;
		if (null != files) {
			for (String fileName : files) {
				start = System.currentTimeMillis();
				File imageFile = new File(imageDir, fileName);
				try {
					bufferedImage = ImageUtils.loadImage(imageFile);
					BufferedImage binaryImage = imageDistinguish.binaryImage(bufferedImage);
					List<ProxyBufferedImage> list = imageDistinguish.cutApart(binaryImage);
					for (ProxyBufferedImage p : list) {
						ImageUtils.writeImage(new File(charPath, System.currentTimeMillis() + ".jpg"),
								p.getBufferedImage());
					}
					end = System.currentTimeMillis();
					System.out.println(
							" distinguish image[" + fileName + "] result:" + result + "|time:" + (end - start));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
}
