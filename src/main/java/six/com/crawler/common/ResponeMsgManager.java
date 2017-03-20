package six.com.crawler.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月19日 下午9:34:03 类说明 响应消息 错误编码
 */
public class ResponeMsgManager {

	public static Map<String, String> errCodeMap=new HashMap<String, String>();
	public static final String SYSTEM_ERR_0001 = "system err,please try again later";

	private ResponeMsgManager() {
		Resource resource = new ClassPathResource("/errs.properties");
		try {
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			for (Object key : props.keySet()) {
				errCodeMap.put(key.toString(), props.getProperty(key.toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	static class InsideResponeMsgManager {
		static ResponeMsgManager responeMsgManager = new ResponeMsgManager();
	}

	public static ResponeMsgManager instance() {
		return InsideResponeMsgManager.responeMsgManager;
	}

	public String getErrMsg(String errCode) {
		return errCodeMap.get(errCode);
	}
}
