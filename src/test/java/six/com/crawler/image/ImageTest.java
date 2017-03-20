package six.com.crawler.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.BaseTest;
import six.com.crawler.ocr.ImageUtils;
import six.com.crawler.utils.JsUtils;
import six.com.crawler.utils.WebDriverUtils;
import six.com.crawler.work.downer.ChromeDowner;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月26日 下午5:03:53
 */
public class ImageTest extends BaseTest {

	protected final static Logger LOG = LoggerFactory.getLogger(ChromeDowner.class);

	private WebDriver browser;

	public static final String charDirPath = "F:/six/ocr/image/char/";
	static String originalDir = "F:/six/ocr/image/test/";

	@Test
	public void test() {
		try {
			init();
			//work();
		} finally {
			if (null != browser) {
				browser.quit();
			}
		}

	}

	protected void work() {
		String imageXpath = "//img[@id='imgRandom']";
		String inputCodeXpath = "//input[@id='txtCode']";
		String btXpath = "//input[@id='Button1']";
		// String divXpath = "//div[@id='divShowRoomInfo']";
		String url = "http://www.cq315house.com/315web/HtmlPage/ShowRoomsNew.aspx?block=23%E5%B9%A2&buildingid=10856613#";
		browser.get(url);
		WebElement houseBt = browser.findElement(By.xpath("//tr[@id='row_12']/td[3]/a"));
		houseBt.click();
		// WebElement divElement = browser.findElement(By.xpath(divXpath));
		WebElement iframe = browser.findElement(By.xpath("//iframe[@id='fRoomInfo']"));
		Point iframeLocation = iframe.getLocation();
		browser.switchTo().frame(iframe);
		WebElement imageElement = browser.findElement(By.xpath(imageXpath));
		WebElement inputCodeElement = browser.findElement(By.xpath(inputCodeXpath));
		WebElement btElement = browser.findElement(By.xpath(btXpath));
		// String src = imageElement.getAttribute("src");
		Point location = imageElement.getLocation();
		Dimension size = imageElement.getSize();
		int x = iframeLocation.getX() + location.getX() + 1;// 800
		int y = iframeLocation.getY() + location.getY() + 1;// 265
		int w = size.getWidth();
		int h = size.getHeight();
		// 创建全屏截图。
		long start = System.currentTimeMillis();
		String result = null;
		try {
			BufferedImage originalImage = WebDriverUtils.screenshot(browser);
			BufferedImage croppedImage = originalImage.getSubimage(x, y, w, h);
			ImageUtils.writeImage(new File(originalDir, start + ".jpg"), croppedImage);
			result = imageDistinguish.distinguish(croppedImage);
			result = JsUtils.eval(result, result);
			inputCodeElement.sendKeys(result);
			btElement.click();
			long end = System.currentTimeMillis();
			System.out.println("result:" + result + "|time:" + (end - start));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void init() {
		DesiredCapabilities cap = DesiredCapabilities.chrome();
		HashMap<String, Object> settings = new HashMap<String, Object>();
		// settings.put("images", 2); // disabled load images
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("profile.managed_default_content_settings", settings);
		ChromeOptions options = new ChromeOptions();
		String homePath = jobWorkerManager.getConfigure().getSpiderHome();
		String webdriverPath = homePath + File.separatorChar + "webdriver";
		String CHROME_DIR = webdriverPath + File.separatorChar + "chrome";
		String chromeUserDataFileName = CHROME_DIR + File.separatorChar + "data" + File.separatorChar
				+ "node1_qichacha_HTMLJOB_Jobworker__userData" + "_userData";
		options.addArguments("user-data-dir=" + chromeUserDataFileName);
		options.setExperimentalOption("prefs", prefs);
		cap.setCapability(ChromeOptions.CAPABILITY, options);
		browser = new ChromeDriver(cap);
		browser.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
		// browser.manage().timeouts().pageLoadTimeout(5000,TimeUnit.MILLISECONDS);
		// browser.manage().timeouts().setScriptTimeout(1000,TimeUnit.MILLISECONDS);

		WebDriverWait wait = new WebDriverWait(browser, 10);
		ExpectedCondition<Boolean> pageLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver arg0) {
				return ((JavascriptExecutor) arg0).executeScript("return document.readyState").equals("complete");
			}
		};
		wait.until(pageLoad);
	}

}
