package six.com.crawler.common.ocr;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月27日 下午3:53:09 代码来源于github
 */
public class ImageUtils {

	private ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
	private int size = 32;
	private int smallerSize = 8;
	private double[] c;

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ImageUtils getInstance() {
		return ProxyImageUtils.IMAGE_UTILS;
	}

	/**
	 * 单例模式 实现赖加载
	 * 
	 * @author six
	 * @email 359852326@qq.com
	 */
	private static class ProxyImageUtils {
		static ImageUtils IMAGE_UTILS = new ImageUtils();
	}

	private ImageUtils() {
		initCoefficients();
	}

	public ImageUtils(int size, int smallerSize) {
		this.size = size;
		this.smallerSize = smallerSize;
		initCoefficients();
	}

	/**
	 * 计算图片 hash 评分
	 * 
	 * @param imageHash1
	 *            图片1的hash
	 * @param imageHash2
	 *            图片2的hash
	 * @return 2个图片的相似度评分 >=0 趋向于0表示越相似
	 */
	public int score(String imageHash1, String imageHash2) {
		int counter = 0;
		for (int k = 0; k < imageHash1.length(); k++) {
			if (imageHash1.charAt(k) != imageHash2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * 1. Reduce size. Like Average Hash, pHash starts with a small image.
	 * However, the image is larger than 8x8; 32x32 is a good size. This is
	 * really done to simplify the DCT computation and not because it is needed
	 * to reduce the high frequencies. 2. Reduce color. The image is reduced to
	 * a grayscale just to further simplify the number of computations. 3.
	 * Compute the DCT. The DCT separates the image into a collection of
	 * frequencies and scalars. While JPEG uses an 8x8 DCT, this algorithm uses
	 * a 32x32 DCT. 4. Reduce the DCT. This is the magic step. While the DCT is
	 * 32x32, just keep the top-left 8x8. Those represent the lowest frequencies
	 * in the picture. 5. Compute the average value. Like the Average Hash,
	 * compute the mean DCT value (using only the 8x8 DCT low-frequency values
	 * and excluding the first term since the DC coefficient can be
	 * significantly different from the other values and will throw off the
	 * average). 6. Further reduce the DCT. This is the magic step. Set the 64
	 * hash bits to 0 or 1 depending on whether each of the 64 DCT values is
	 * above or below the average value. The result doesn't tell us the actual
	 * low frequencies; it just tells us the very-rough relative scale of the
	 * frequencies to the mean. The result will not vary as long as the overall
	 * structure of the image remains the same; this can survive gamma and color
	 * histogram adjustments without a problem.
	 * 
	 * @param img
	 * @return 返回图片的hash值
	 */
	public String getImageHash(BufferedImage img) {
		img = resize(img, size, size);
		img = grayscale(img);
		double[][] vals = new double[size][size];
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				vals[x][y] = getBlue(img, x, y);
			}
		}
		double[][] dctVals = applyDCT(vals);
		double total = 0;
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				total += dctVals[x][y];
			}
		}
		total -= dctVals[0][0];
		double avg = total / (double) ((smallerSize * smallerSize) - 1);
		String hash = "";
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				if (x != 0 && y != 0) {
					hash += (dctVals[x][y] > avg ? "1" : "0");
				}
			}
		}
		return hash;
	}

	private BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	private BufferedImage grayscale(BufferedImage img) {
		colorConvert.filter(img, img);
		return img;
	}

	private static int getBlue(BufferedImage img, int x, int y) {
		return (img.getRGB(x, y)) & 0xff;
	}

	private void initCoefficients() {
		c = new double[size];
		for (int i = 1; i < size; i++) {
			c[i] = 1;
		}
		c[0] = 1 / Math.sqrt(2.0);
	}

	private double[][] applyDCT(double[][] f) {
		int N = size;

		double[][] F = new double[N][N];
		for (int u = 0; u < N; u++) {
			for (int v = 0; v < N; v++) {
				double sum = 0.0;
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < N; j++) {
						sum += Math.cos(((2 * i + 1) / (2.0 * N)) * u * Math.PI)
								* Math.cos(((2 * j + 1) / (2.0 * N)) * v * Math.PI) * (f[i][j]);
					}
				}
				sum *= ((c[u] * c[v]) / 4.0);
				F[u][v] = sum;
			}
		}
		return F;
	}

	/**
	 * 将十进制的颜色值转为十六进制
	 * 
	 * @param i
	 * @return
	 */
	public static int getImageRgb(int i) {
		String argb = Integer.toHexString(i);
		// argb分别代表透明,红,绿,蓝 分别占16进制2位
		int r = Integer.parseInt(argb.substring(2, 4), 16);// 后面参数为使用进制
		int g = Integer.parseInt(argb.substring(4, 6), 16);
		int b = Integer.parseInt(argb.substring(6, 8), 16);
		int result = (int) ((r + g + b) / 3);
		return result;
	}

	/**
	 * 自己加周围8个灰度值再除以9，算出其相对灰度值
	 * 
	 * @param gray
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public static int getGray(int gray[][], int x, int y, int w, int h) {
		int rs = gray[x][y] + (x == 0 ? 255 : gray[x - 1][y]) + (x == 0 || y == 0 ? 255 : gray[x - 1][y - 1])
				+ (x == 0 || y == h - 1 ? 255 : gray[x - 1][y + 1]) + (y == 0 ? 255 : gray[x][y - 1])
				+ (y == h - 1 ? 255 : gray[x][y + 1]) + (x == w - 1 ? 255 : gray[x + 1][y])
				+ (x == w - 1 || y == 0 ? 255 : gray[x + 1][y - 1])
				+ (x == w - 1 || y == h - 1 ? 255 : gray[x + 1][y + 1]);
		return rs / 9;
	}

	/**
	 * 读取图片文件
	 * 
	 * @param file
	 * @return
	 */
	public static BufferedImage loadImage(File file) {
		BufferedImage result = null;
		try (FileInputStream input = new FileInputStream(file);) {
			result = ImageIO.read(input);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("get FileInputStream err:" + file.getName(), e);
		} catch (IOException e) {
			throw new RuntimeException("ImageIO read file err:" + file.getName(), e);
		}
		return result;
	}
	
	/**
	 * 读取图片文件
	 * 
	 * @param file
	 * @return
	 */
	public static BufferedImage loadImage(byte[] imageData) {
		BufferedImage result = null;
		ByteArrayInputStream input = new ByteArrayInputStream(imageData);
		try {
			result = ImageIO.read(input);
		} catch (IOException e) {
			throw new RuntimeException("ImageIO read imageData err", e);
		}
		return result;
	}

	public static int getRGB(int r,int g,int b){
		return ((255 & 0xFF) << 24) |
        ((r & 0xFF) << 16) |
        ((g & 0xFF) << 8)  |
        ((b & 0xFF) << 0);
	}
	/**
	 * 读取图片文件
	 * 
	 * @param file
	 * @return
	 */
	public static void writeImage(File file, BufferedImage image) {
		try {
			ImageIO.write(image, "jpg", file);
		} catch (IOException e) {
			throw new RuntimeException("ImageIO writeImage file err:" + file.getName(), e);
		}
	}
	
	/**
	 * @Descriptionmap 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * @author temdy
	 * @Date 2015-01-26
	 * @param path 图片路径
	 * @return
	 */
	public static String imageToBase64(String path) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	    byte[] data =null;
	    // 读取图片字节数组
	    try {
	    	data = FileUtils.readFileToByteArray(new File(path));
	    	// 对字节数组Base64编码,返回Base64编码过的字节数组字符串
		    return Base64.getEncoder().encodeToString(data);
	    } catch (IOException e) {
	       throw new RuntimeException("read file err:"+path, e);
	    }
	}

	/**
	 * @Descriptionmap 对字节数组字符串进行Base64解码并生成图片
	 * @author temdy
	 * @Date 2015-01-26
	 * @param base64 图片Base64数据
	 * @param path 图片路径
	 * @return
	 */
	public static void base64ToImage(String base64, String path) {// 对字节数组字符串进行Base64解码并生成图片
	    if (StringUtils.isNotBlank(base64)){ // 图像数据为空
	    	// Base64解码
	        byte[] bytes = Base64.getDecoder().decode(base64);
	        for (int i = 0; i < bytes.length; ++i) {
	            if (bytes[i] < 0) {// 调整异常数据
	                bytes[i] += 256;
	            }
	        }
	    	try {
		        FileUtils.writeByteArrayToFile(new File(path), bytes);
		    } catch (Exception e) {
		    	throw new RuntimeException("write file err:"+path, e);
		    }
	    }
	}
	    
	
}
