package six.com.crawler.work.downer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.Page;
import six.com.crawler.http.HttpResult;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.downer.cache.DownerCache;
import six.com.crawler.work.downer.exception.DownerException;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午8:17:36 类说明
 */
public abstract class AbstractDowner implements Downer, AutoCloseable {

	protected final static Logger log = LoggerFactory.getLogger(AbstractDowner.class);

	protected AbstractCrawlWorker worker;
	/**
	 * 打开下载缓冲
	 */
	private boolean openDownCache;
	/**
	 * 使用下载缓冲
	 */
	private boolean useDownCache;
	private HttpProxy httpProxy;
	private String pageKey;
	private String lastRequestUrl;
	private HttpResult lastHttpResult;
	private DownerCache downerCache;

	public AbstractDowner(AbstractCrawlWorker worker,boolean openDownCache,boolean useDownCache,DownerCache downerCache) {
		this.worker = worker;
		this.openDownCache=openDownCache;
		this.useDownCache=useDownCache;
		this.downerCache=downerCache;
	}

	protected abstract HttpResult insideDown(Page page) throws DownerException;

	/**
	 * 记录上一次请求的url ，如果跟上一次请求url一样的话那么不下载
	 */
	public void down(Page page) throws DownerException {
		if (null == page || StringUtils.isBlank(page.getOriginalUrl())) {
			throw new DownerException("page is null or page's url is blank");
		}
		if (!useDownCache) {
			if (1 != page.getNoNeedDown()) {
				if (!(StringUtils.equals(page.getPageKey(), pageKey)
						&& StringUtils.equals(page.getOriginalUrl(), lastRequestUrl))) {
					lastHttpResult = insideDown(page);
					pageKey = page.getPageKey();
					lastRequestUrl = page.getOriginalUrl();
					lastHttpResult.setHtml(page.getPageSrc());
					if (openDownCache) {
						downerCache.write(page);
					}
				} else {
					page.setPageSrc(lastHttpResult.getHtml());
				}
			}
		} else {
			downerCache.read(page);
		}
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
		try {
			insideColose();
		} catch (Exception e) {
			log.error("", e);
		}
		downerCache.close();
	}
}
