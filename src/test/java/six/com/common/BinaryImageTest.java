package six.com.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import six.com.crawler.common.ocr.ImageUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月19日 上午9:35:20
 */
public class BinaryImageTest {

	static String dir = "f://test/ocr";
	
	public static void main(String[] args) {
		String fileName = "test_2.jpg";
		BufferedImage srcImg = ImageUtils.loadImage(new File(dir, fileName));
		int maxR = 190;
		int maxG = 190;
		int maxB = 190;
		BufferedImage doneImg = binaryImage(srcImg, maxR, maxG, maxB);
		String tempPath = "result_" + System.currentTimeMillis() + ".png";
		ImageUtils.writeImage(new File(dir, tempPath),doneImg);
		subImage(doneImg);
	}
	
	
	public static void subImage(BufferedImage doneImg){
		List<Integer> widthList=new ArrayList<>();
		widthList.add(35);
		widthList.add(57);
		widthList.add(25);
		widthList.add(52);
		List<Integer> pxList=new ArrayList<>();
		pxList.add(91);
		pxList.add(218);
		pxList.add(355);
		pxList.add(455);
		int high=22;
		for (int i = 0; i < pxList.size(); i++) {
			int p_x=pxList.get(i);
			int width=widthList.get(i);
			int p_y=0;
			for (int y = 0; y < 6; y++) {
				BufferedImage subImg = doneImg.getSubimage(p_x,p_y, width, high);
				String tempPath = "result_" + System.currentTimeMillis() + ".png";
				ImageUtils.writeImage(new File(dir, tempPath),subImg);
				p_y+=2+high;
			}
		}
	}

	/**
	 * 图片二值化
	 * 
	 * @param srcImg
	 * @return
	 */
	public static BufferedImage binaryImage(BufferedImage srcImg, int a_r, int a_g, int a_b) {
		int width = srcImg.getWidth();
		int height = srcImg.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, srcImg.getType());
		srcImg.getType();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = srcImg.getRGB(i, j);
				int r = (int) (rgb >> 16) & 0xFF;
				int g = (int) (rgb >> 8) & 0xFF;
				int b = (int) (rgb >> 0) & 0xFF;
				if (r > a_r) {
					r = 255;
				}

				if (g > a_g) {
					g = 255;
				}

				if (b > a_b) {
					b = 255;
				}
				int newRGB = ImageUtils.getRGB(r, g, b);
				grayImage.setRGB(i, j, newRGB);
			}
		}
		return grayImage;
	}

}
