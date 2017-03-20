package six.com.crawler.ocr;

import java.awt.image.BufferedImage;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月28日 下午12:11:34
 */
public class ProxyBufferedImage {

	private BufferedImage bufferedImage;
	private DistinguishResult distinguishResult = new DistinguishResult();

	protected ProxyBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public DistinguishResult getDistinguishResult() {
		return distinguishResult;
	}
}
