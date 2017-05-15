package six.com.crawler.work.downer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.HttpProxy;
import six.com.crawler.entity.Page;
import six.com.crawler.http.HttpResult;
import six.com.crawler.utils.WebDriverUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.Constants;
import six.com.crawler.work.downer.cache.DownerCache;
import six.com.crawler.work.downer.exception.DownerException;

/**
 * @author six
 * @date 2016年5月18日 下午5:42:26
 */
public class ChromeDowner extends AbstractDowner {

	protected final static Logger LOG = LoggerFactory.getLogger(ChromeDowner.class);

	private ChromeDriver browser;

	private String waitJsLoadElementXpath;

	private static final long defaultWaitTimeOunt = 3000;


	public ChromeDowner(AbstractCrawlWorker worker, boolean openDownCache, boolean useDownCache,
			DownerCache downerCache) {
		super(worker, openDownCache, useDownCache, downerCache);
		DesiredCapabilities cap = DesiredCapabilities.chrome();
		HashMap<String, Object> settings = new HashMap<String, Object>();
		String loadImages = worker.getJob().getParam("loadImages");
		if ("0".equals(loadImages)) {
			settings.put("images", 2); // 设置不加载图片
		}
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("profile.managed_default_content_settings", settings);
		ChromeOptions options = new ChromeOptions();
		String homePath = worker.getManager().getConfigure().getSpiderHome();
		String webdriverPath = homePath + File.separatorChar + "webdriver";
		String chromePath = webdriverPath + File.separatorChar + "chrome";
		String chromeUserDataFileName = chromePath + File.separatorChar + "usrData" + File.separatorChar
				+ worker.getName();
		options.addArguments("user-data-dir=" + chromeUserDataFileName);
		options.setExperimentalOption("prefs", prefs);
		cap.setCapability(ChromeOptions.CAPABILITY, options);
		HttpProxy httpProxy = getHttpProxy();
		if (null != httpProxy) {
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(httpProxy.toString());
			cap.setCapability(CapabilityType.ForSeleniumServer.AVOIDING_PROXY, true);
			cap.setCapability(CapabilityType.ForSeleniumServer.ONLY_PROXYING_SELENIUM_TRAFFIC, true);
			cap.setCapability(CapabilityType.PROXY, proxy);
		}
		browser = new ChromeDriver(cap);
		browser.setErrorHandler(new ErrorHandler());
		// 搜索元素时 10秒异常
		browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		// 页面加载 30秒抛异常
		browser.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		browser.manage().timeouts().setScriptTimeout(1000, TimeUnit.MILLISECONDS);
		waitJsLoadElementXpath = worker.getJob().getParam("waitJsLoadElementXpath");
		// WebDriverWait wait = new WebDriverWait(browser, 10);
		// ExpectedCondition<Boolean> pageLoad = new
		// ExpectedCondition<Boolean>() {
		// public Boolean apply(WebDriver arg0) {
		// return ((JavascriptExecutor) arg0).executeScript("return
		// document.readyState").equals("complete");
		// }
		// };
		// wait.until(pageLoad);
	}

	protected HttpResult insideDown(Page page) throws DownerException {
		HttpResult result = new HttpResult();
		browser.get(page.getOriginalUrl());
		waitForload();
		String html = browser.getPageSource();
		String currentUrl = browser.getCurrentUrl();
		page.setFinalUrl(currentUrl);
		page.setPageSrc(html);
		return result;
	}

	public WebDriver getWebDriver() {
		return browser;
	}

	public String getWindowHandle() {
		return browser.getWindowHandle();
	}

	public WebElement findWebElement(String xpath) {
		WebElement findWebElement = WebDriverUtils.findElement(browser, xpath, defaultWaitTimeOunt);
		return findWebElement;
	}

	public List<WebElement> findWebElements(String xpath) {
		List<WebElement> findWebElements = WebDriverUtils.findElements(browser, xpath, defaultWaitTimeOunt);
		return findWebElements;
	}

	public void click(WebElement webElement, String xpath) {
		WebDriverUtils.click(browser, webElement, xpath, defaultWaitTimeOunt);
	}

	private void waitForload() {
		if (null != waitJsLoadElementXpath && waitJsLoadElementXpath.length() > 0
				&& !"null".equalsIgnoreCase(waitJsLoadElementXpath)) {
			WebElement waitElement = null;
			long start = System.currentTimeMillis();
			while (null == waitElement) {
				waitElement = WebDriverUtils.findElement(browser, waitJsLoadElementXpath,
						Constants.FIND_ELEMENT_TIMEOUT);
				if (null != waitElement) {
					break;
				}
				long end = System.currentTimeMillis();
				if ((end - start) > defaultWaitTimeOunt) {
					throw new RuntimeException("wait load element[" + waitJsLoadElementXpath + "] timeout");
				}
			}
		}
	}

	public byte[] downBytes(Page page) throws DownerException {
		return null;
	}

	protected boolean internalLoginAndLogout(Page page) {
		return false;
	}

	protected boolean checkIsLogOut(Page page) {
		return false;
	}

	protected void insideColose() {
		browser.close();
		browser.quit();
	}

}
