package six.com.crawler.work.downer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
	private String lastRequestUrl;
	private HttpResult lastHttpResult;

	public AbstractDowner(AbstractCrawlWorker worker) {
		this.worker = worker;
	}

	protected abstract HttpResult insideDown(Page page) throws DownerException;

	/**
	 * 记录上一次请求的url ，如果跟上一次请求url一样的话那么不下载
	 */
	public HttpResult down(Page page) throws DownerException {
		if (null == page || StringUtils.isBlank(page.getOriginalUrl())) {
			throw new DownerException("page is null or page's url is blank");
		}
		if (1 != page.getNoNeedDown()) {
			if (!StringUtils.equals(page.getOriginalUrl(), lastRequestUrl)) {
				lastHttpResult = insideDown(page);
				lastRequestUrl = page.getOriginalUrl();
				lastHttpResult.setHtml(page.getPageSrc());
			} else {
				page.setPageSrc(lastHttpResult.getHtml());
			}
		}
		return lastHttpResult;
	}

	public AbstractCrawlWorker getHtmlCommonWorker() {
		return worker;
	}

	public WebDriver getWebDriver() {
		return null;
	}

	public String getWindowHandle() {
		return null;
	}

	public WebElement findWebElement(String xpath) {
		return null;
	}

	public List<WebElement> findWebElements(String xpath) {
		return null;
	}

	public void click(WebElement webElement, String xpath) {

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
	public final void close() {
		insideColose();
	}
}
