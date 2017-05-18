package six.com.crawler.work.downer;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.Page;
import six.com.crawler.work.downer.exception.DownerException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月14日 上午9:58:53
 */
public interface Downer {

	WebDriver getWebDriver();

	String getWindowHandle();

	WebElement findWebElement(String xpath);

	List<WebElement> findWebElements(String xpath);

	void click(WebElement webElement, String xpath);

	Page down(Page page) throws DownerException;

	byte[] downBytes(Page page) throws DownerException;

	void setHttpProxy(HttpProxy httpProxy);

	HttpProxy getHttpProxy();

	void close();
}
