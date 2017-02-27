package six.com.crawler.common.configure;

import java.io.File;
import java.util.Properties;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author six
 * @date 2015年12月31日 上午9:25:29
 */
@Configuration
public class SpiderConfigure implements EnvironmentAware {

	private RelaxedPropertyResolver propertyResolver;
	
	private String host;
	
	private int port;

	private String spidrHome;

	@Override
	public void setEnvironment(Environment env) {
		port=env.getProperty("server.port",Integer.class);
		this.propertyResolver = new RelaxedPropertyResolver(env, "spider.");
		host=propertyResolver.getProperty("node.host")==null?"127.0.0.1":propertyResolver.getProperty("node.host");
		spidrHome = propertyResolver.getProperty("home");
		Properties prop = System.getProperties();
		String os = prop.getProperty("os.name").toUpperCase();
		String webdriverDir = spidrHome + File.separatorChar + "webdriver";
		String chromeDir = webdriverDir + File.separatorChar + "chrome" + File.separatorChar;
		String driverFilePath = null;
		if (os.indexOf("WIN") != -1) {
			driverFilePath = chromeDir + "chromedriver.exe";
		} else {
			driverFilePath = chromeDir + "chromedriver";
		}
		System.setProperty("webdriver.chrome.driver", driverFilePath);
	}

	public String[] getAdminEmails() {
		String emails = getConfig("email.admin", "359852326@qq.com");
		if (null != emails) {
			return emails.split(";");
		}
		return new String[0];
	}

	public String getConfig(String key, String defaultValue) {
		String value = propertyResolver.getProperty(key);
		String result = defaultValue;
		if (null != value) {
			try {
				result = value.toString();
			} catch (Exception e) {
				// 忽略
			}
		}
		return result;
	}

	public int getConfig(String key, int defaultValue) {
		String value = propertyResolver.getProperty(key);
		int result = defaultValue;
		if (null != value) {
			try {
				result = Integer.valueOf(value);
			} catch (Exception e) {
				// 忽略
			}
		}
		return result;
	}

	public int getPort(){
		return port;
	}
	
	public String getHost(){
		return host;
	}
	
	public String getSpiderHome() {
		return spidrHome;
	}
}
