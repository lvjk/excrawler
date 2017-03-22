package six.com.crawler.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.dao.RedisManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月27日 下午5:42:16
 */
@Component
public class ImageDistinguish {

	final static Logger LOG = LoggerFactory.getLogger(ImageDistinguish.class);

	@Autowired
	private RedisManager redisManager;

	private static final String CHAR_IMAGE_KEY = "ocr_char_image";

	private static final int scoreFz = 5;

	private static final int scoreIsTooBigCount = 5;

	/**
	 * 加载字符样本到redis
	 */
	public void loadToRedis(String samplePath) {
		File charDir = new File(samplePath);
		BufferedImage charImageBf = null;
		String charImageHash = null;
		CharImage charImage = null;
		Map<String, List<CharImage>> map = new HashMap<>();
		List<CharImage> list = null;
		for (String fileName : charDir.list()) {
			File imageFile = new File(charDir, fileName);
			try {
				charImageBf = ImageUtils.loadImage(imageFile);
				charImageHash = ImageUtils.getInstance().getImageHash(charImageBf);
				charImage = new CharImage();
				charImage.setHash(charImageHash);
				charImage.setPath(imageFile.getPath());
				String result = fileName.trim().substring(0, 1);
				charImage.setResult(result);
				list = map.get(result);
				if (null == list) {
					list = new ArrayList<>();
					map.put(result, list);
				}
				list.add(charImage);
			} catch (Exception e) {
				LOG.error("load char image err:" + fileName, e);
			}
		}
		redisManager.del(CHAR_IMAGE_KEY);
		for (String key : map.keySet()) {
			list = map.get(key);
			redisManager.hset(CHAR_IMAGE_KEY, key, list);
		}

	}

	/**
	 * 从redis获取样本数据
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, List<CharImage>> getSampleMap() {
		Map<String, List<CharImage>> result = new HashMap<>();
		@SuppressWarnings("rawtypes")
		Map<String, List> map = redisManager.hgetAll(CHAR_IMAGE_KEY, List.class);
		List<CharImage> list = null;
		for (String key : map.keySet()) {
			list = map.get(key);
			result.put(key, list);
		}
		return result;
	}

	/**
	 * 图片识别
	 * 
	 * @param srcImg
	 * @return
	 * @throws Exception
	 */
	public String distinguish(BufferedImage srcImg) {
		if(null==srcImg){
			throw new RuntimeException("srcImg of distinguish is null");
		}
		String result = "";
		// 1.图片二值化
		BufferedImage binaryImage = binaryImage(srcImg);
		// 2.图片切割
		List<ProxyBufferedImage> imgPartList = cutApart(binaryImage);
		// 3.计算图片与训练样本的评分
		Stream<ProxyBufferedImage> stream = imgPartList.stream();
		// 4.获取样本数据
		Map<String, List<CharImage>> samplemap = getSampleMap();
		stream.parallel().forEach(entry -> {
			distinguishPartImage(entry, samplemap);
		});
		// 5.组合每个partImage识别出来的结果
		for (ProxyBufferedImage proxyBufferedImage : imgPartList) {
			result += proxyBufferedImage.getDistinguishResult().getResult();
		}
		return result;
	}

	/**
	 * 图片切割
	 * 
	 * @param srcImg
	 * @return 返回切割后的 部分图片集合
	 */
	public List<ProxyBufferedImage> cutApart(BufferedImage srcImg) {
		int h = srcImg.getHeight();
		List<ProxyBufferedImage> list = new ArrayList<>();
		BufferedImage charImage1 = srcImg.getSubimage(0, 0, 25, h);
		list.add(new ProxyBufferedImage(charImage1));
		BufferedImage charImage2 = srcImg.getSubimage(25, 0, 25, h);
		list.add(new ProxyBufferedImage(charImage2));
		BufferedImage charImage3 = srcImg.getSubimage(50, 0, 25, h);
		list.add(new ProxyBufferedImage(charImage3));
		return list;
	}

	/**
	 * 识别分割后的image
	 * 
	 * @param proxyPartImage
	 *            分割后的图片
	 * @param samplemap
	 *            样本数据
	 * @return
	 */
	private void distinguishPartImage(final ProxyBufferedImage proxyPartImage, Map<String, List<CharImage>> samplemap) {
		final DistinguishResult distinguishResult = proxyPartImage.getDistinguishResult();
		final BufferedImage partImage = proxyPartImage.getBufferedImage();
		final String partImageHash = ImageUtils.getInstance().getImageHash(partImage);
		// 用来记录每种类别样本评分相似度太大的次数
		final Map<String, AtomicInteger> countMap = new ConcurrentHashMap<String, AtomicInteger>();
		// 根据样本类别进行流处理
		samplemap.keySet().stream().parallel().forEach(key -> {
			// 根据类别下样本集合进行流处理
			samplemap.get(key).stream().parallel().forEach(entry -> {
				// 计算 图片与样本的 评分
				int tempScore = ImageUtils.getInstance().score(partImageHash, entry.getHash());
				// 判断计算出评分是否大于 设置的评分阈值
				if (tempScore > scoreFz) {
					AtomicInteger count = countMap.computeIfAbsent(entry.getResult(), countKey -> new AtomicInteger(0));
					if (null != count && count.incrementAndGet() > scoreIsTooBigCount) {
						// break; 目前样本数据少暂时不需要实现 如果数据量大需要实现避免 样本全匹配造成性能问题
					}
				}
				// 判断计算出评分是否小于 distinguishResult 的score 如果小于则赋值
				if (tempScore < distinguishResult.getScore()) {
					distinguishResult.setScore(tempScore);
					distinguishResult.setResult(entry.getResult());
				}
			});
		});
	}

	/**
	 * 图片二值化
	 * 
	 * @param srcImg
	 * @return
	 */
	public BufferedImage binaryImage(BufferedImage srcImg) {
		int width = srcImg.getWidth();
		int height = srcImg.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);// 重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = srcImg.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		return grayImage;
	}
	

	/**
	 * 训练
	 */
	public void practice() {

	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}
}
