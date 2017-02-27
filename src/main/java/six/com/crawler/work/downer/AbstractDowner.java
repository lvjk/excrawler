package six.com.crawler.work.downer;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.http.HttpResult;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.exception.DownerException;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午8:17:36 类说明
 */
public abstract class AbstractDowner implements Downer, AutoCloseable {

	protected final static Logger LOG = LoggerFactory.getLogger(AbstractDowner.class);

	protected AbstractCrawlWorker worker;
	private HttpProxy httpProxy;

	public AbstractDowner(AbstractCrawlWorker worker) {
		this.worker = worker;
	}

	protected abstract HttpResult insideDown(Page page) throws DownerException;

	public HttpResult down(Page page) throws DownerException {
		return insideDown(page);
	}
	
	public AbstractCrawlWorker getHtmlCommonWorker(){
		return worker;
	}
	
	public WebDriver getWebDriver(){
		return null;
	}
	public String getWindowHandle(){
		return null;
	}

	public WebElement findWebElement(String xpath) {
		return null;
	}

	public List<WebElement> findWebElements(String xpath) {
		return null;
	}
	
	public void click(WebElement webElement,String xpath){
		
	}

	public void setHttpProxy(HttpProxy httpProxy) {
		this.httpProxy = httpProxy;
	}

	public HttpProxy getHttpProxy() {
		return httpProxy;
	}

	/**
	 * 内部close方法
	 */
	protected abstract void insideColose();

	@Override
	public final void close(){
		insideColose();
	}
}
