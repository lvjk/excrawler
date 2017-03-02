package six.com.crawler.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author six
 * @date 2016年7月15日 上午9:34:58 编码识别工具
 */
public class AutoCharsetDetectorUtils {

	final static Logger LOG = LoggerFactory.getLogger(AutoCharsetDetectorUtils.class);

	public enum ContentType {
		HTML, XML, OTHER
	}

	private static Pattern metaPattern = Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>",
			Pattern.CASE_INSENSITIVE);
	private static Pattern charsetPattern = Pattern.compile("charset=([^ \"\'/]+)", Pattern.CASE_INSENSITIVE);
	private static Pattern xmlPattern = Pattern.compile("<\\?xml(.*)encoding=\"(\\S+)\"", Pattern.CASE_INSENSITIVE);
	private static UniversalDetector autoDetector = new UniversalDetector(null);

	private static Map<String, String> encodingReplacementMap = new HashMap<String, String>();

	private static String DEFAULT_CHARSET = "UTF-8";
	private static Map<String, String> peculiarCharacterMap = new HashMap<String, String>();
	static {
		encodingReplacementMap.put("gb2312", "GBK");
		encodingReplacementMap.put("utf_8", "UTF-8");

		Resource resource = new ClassPathResource("/peculiarCharacter");
		try {
			String path = resource.getURI().getPath();
			List<String> list = FileUtils.readLines(new File(path));
			String[] temp = null;
			for (String str : list) {
				temp = str.split("=<>=");
				if (temp.length > 2) {
					peculiarCharacterMap.put(temp[0], temp[1]);
				} else {
					peculiarCharacterMap.put(temp[0], "");
				}
			}
		} catch (IOException e) {
			LOG.error("init AutoCharsetDetectorUtils config err", e);
			System.exit(1);
		}
	}

	/**
	 * 替换特殊字符
	 * 
	 * @param str
	 * @return
	 */
	public static String replacePeculiarCharacter(String str) {
		if (StringUtils.isNoneBlank(str)) {
			for (String key : peculiarCharacterMap.keySet()) {
				String value = peculiarCharacterMap.get(key);
				str = StringUtils.replace(str, key, value);
			}
		}
		return str;
	}

	/**
	 * 获取编码
	 * 
	 * @param bytes
	 * @param type
	 * @return
	 */
	public static String getCharset(byte[] bytes, ContentType type) {
		String charsetResult = null;
		if (type == ContentType.HTML) {
			String content = new String(bytes);
			Matcher m = metaPattern.matcher(content);
			if (m.find()) {
				String meta = m.group(1);
				m = charsetPattern.matcher(meta);
				if (m.find()) {
					String charset = StringUtils.remove(m.group(1), ';');
					if (StringUtils.isNotBlank(charset)) {
						charsetResult = replacement(charset);
					}
				}
			} else {
				m = charsetPattern.matcher(content);
				if (m.find()) {
					String charset = StringUtils.remove(m.group(1), ';');
					if (StringUtils.isNotBlank(charset)) {
						charsetResult = replacement(charset);
					}
				}
			}
		} else if (type == ContentType.XML) {
			String content = new String(bytes);
			Matcher m = xmlPattern.matcher(content);
			if (m.find()) {
				String charset = StringUtils.remove(m.group(2), ';');
				if (StringUtils.isNotBlank(charset)) {
					charsetResult = replacement(charset);
				}
			}
		}
		if (type == ContentType.OTHER || null == charsetResult) {
			autoDetector.handleData(bytes, 0, bytes.length);
			autoDetector.dataEnd();
			charsetResult = autoDetector.getDetectedCharset();
			autoDetector.reset();
		}
		if (null == charsetResult) {
			charsetResult = DEFAULT_CHARSET;
		}
		return charsetResult;
	}

	public static String replacement(String charset) {
		String replacement = encodingReplacementMap.get(StringUtils.lowerCase(charset));
		return replacement != null ? replacement : charset;
	}
}
