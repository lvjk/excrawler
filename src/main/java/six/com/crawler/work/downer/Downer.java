package six.com.crawler.work.downer;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.work.downer.exception.DownerException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月14日 上午9:58:53
 */
public interface Downer {

	
	public WebDriver getWebDriver();
	
	public String getWindowHandle();
	
	public WebElement findWebElement(String xpath);
	
	public List<WebElement> findWebElements(String xpath);
	
	public void click(WebElement webElement,String xpath);
	
	public HttpResult down(Page page) throws DownerException;
	
	public byte[] downBytes(Page page) throws DownerException;
	
	public void setHttpProxy(HttpProxy httpProxy);
	
	public HttpProxy getHttpProxy();
	
	public void close();
}
