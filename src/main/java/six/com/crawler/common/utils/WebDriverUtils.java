package six.com.crawler.common.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月28日 下午2:29:08
 */
public class WebDriverUtils {

	/**
	 * 获取元素集合
	 * 
	 * @param driver
	 * @param xpath
	 * @param timeout
	 * @return
	 */
	public static List<WebElement> findElements(WebDriver driver, String xpath, long timeout) {
		List<WebElement> result = null;
		Exception exception = null;
		long start = System.currentTimeMillis();
		long end = 0;
		while (null == result) {
			try {
				result = driver.findElements(By.xpath(xpath));
				return result;
			} catch (Exception e) {
				if (e instanceof NoSuchElementException) {
					return result;
				}
				exception = e;
			}
			end = System.currentTimeMillis();
			if ((end - start) > timeout) {
				throw new RuntimeException("findElements[" + xpath + "] timeout", exception);
			}
		}
		return result;
	}
	
	
	/**
	 * 获取元素集合
	 * 
	 * @param driver
	 * @param xpath
	 * @param timeout
	 * @return
	 */
	public static List<WebElement> findElements(WebElement webElement, String xpath, long timeout) {
		List<WebElement> result = null;
		Exception exception = null;
		long start = System.currentTimeMillis();
		long end = 0;
		while (null == result) {
			try {
				result = webElement.findElements(By.xpath(xpath));
				return result;
			} catch (Exception e) {
				if (e instanceof NoSuchElementException) {
					return result;
				}
				exception = e;
			}
			end = System.currentTimeMillis();
			if ((end - start) > timeout) {
				throw new RuntimeException("findElements[" + xpath + "] timeout", exception);
			}
		}
		return result;
	}
	
	/**
	 * 获取元素集合
	 * 
	 * @param driver
	 * @param xpath
	 * @param timeout
	 * @return
	 */
	public static WebElement findElement(WebElement webElement, String xpath, long timeout) {
		WebElement result = null;
		Exception exception = null;
		long start = System.currentTimeMillis();
		long end = 0;
		while (null == result) {
			try {
				result = webElement.findElement(By.xpath(xpath));
				return result;
			} catch (Exception e) {
				if (e instanceof NoSuchElementException) {
					return result;
				}
				exception = e;
			}
			end = System.currentTimeMillis();
			if ((end - start) > timeout) {
				throw new RuntimeException("findElement[" + xpath + "] timeout", exception);
			}
		}
		return result;
	}


	public static String getText(WebDriver webDriver, WebElement element, String xpath, long timeout) {
		Exception exception = null;
		String text = null;
		long start = System.currentTimeMillis();
		long end = 0;
		while (true) {
			if (null != element) {
				try {
					text = element.getText();
					return text;
				} catch (Exception e) {
					if (e instanceof StaleElementReferenceException) {
						if (null != xpath) {
							element = findElement(webDriver, xpath, timeout);
							continue;
						}
					}
					exception = e;
				}
				end = System.currentTimeMillis();
				if ((end - start) > timeout) {
					throw new RuntimeException("click element[" + element.getTagName() + "] err", exception);
				}
			} else {
				throw new RuntimeException("click element is null:" + xpath);
			}
		}
	}

	/**
	 * 获取某个元素
	 * 
	 * @param driver
	 * @param xpath
	 * @param timeout
	 * @return
	 */
	public static void click(WebDriver webDriver, WebElement element, String xpath, long timeout) {
		Exception exception = null;
		long start = System.currentTimeMillis();
		long end = 0;
		while (true) {
			if (null != element) {
				try {
					element.click();
					return;
				} catch (Exception e) {
					if (e instanceof StaleElementReferenceException) {
						if (null != xpath) {
							element = findElement(webDriver, xpath, timeout);
							continue;
						}
					}
					exception = e;
				}
				end = System.currentTimeMillis();
				if ((end - start) > timeout) {
					throw new RuntimeException("click element[" + element.getTagName() + "] err", exception);
				}
			} else {
				throw new RuntimeException("click element is null:" + xpath);
			}
		}
	}

	public static void switchToWindows(WebDriver webDriver, String windows) {
		if (null != windows && windows.trim().length() > 0) {
			webDriver.switchTo().window(windows);
		}
	}

	public static void switchToIframe(WebDriver webDriver, String xpath, long timeout) {
		if (null != xpath && xpath.trim().length() > 0) {
			WebElement element = findElement(webDriver, xpath, timeout);
			webDriver.switchTo().frame(element);
		}
	}

	public static String getOtherWindows(WebDriver webDriver, String mainWindows, long timeout) {
		Set<String> windows = webDriver.getWindowHandles();
		String otherWindow = null;
		for (String tempWindow : windows) {
			if (!tempWindow.equalsIgnoreCase(mainWindows)) {
				otherWindow = tempWindow;
				break;
			}
		}
		return otherWindow;
	}

	/**
	 * 获取某个元素
	 * 
	 * @param driver
	 * @param xpath
	 * @param timeout
	 * @return
	 */
	public static WebElement findElement(WebDriver driver, String xpath, long timeout) {
		WebElement result = null;
		Exception exception = null;
		long start = System.currentTimeMillis();
		long end = 0;
		while (null == result) {
			try {
				result = driver.findElement(By.xpath(xpath));
				return result;
			} catch (Exception e) {
				if (e instanceof NoSuchElementException) {
					return result;
				}
				exception = e;
			}
			end = System.currentTimeMillis();
			if ((end - start) > timeout) {
				throw new RuntimeException("findElements[" + xpath + "] timeout", exception);
			}
		}
		return result;
	}

	/**
	 * 等待某个元素
	 * 
	 * @param driver
	 * @param xpath
	 * @param timeout
	 */
	public static void waitForElement(WebDriver driver, String xpath, long timeout) {
		findElement(driver, xpath, timeout);
	}

	/**
	 * 获取某个元素的text=text
	 * 
	 * @param driver
	 * @param elements
	 * @param text
	 * @return
	 */
	public static WebElement findElement(WebDriver driver, List<WebElement> elements, String text) {
		WebElement result = null;
		for (WebElement element : elements) {
			if (text.equals(element.getText())) {
				result = element;
				break;
			}
		}
		return result;
	}

	/**
	 * 屏幕截图
	 * 
	 * @param driver
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage screenshot(WebDriver driver) {
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		byte[] bytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
		BufferedImage originalImage = null;
		try {
			originalImage = ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (IOException e) {
			throw new RuntimeException("screenshot driver err:" + driver.getCurrentUrl());
		}
		return originalImage;
	}

}
