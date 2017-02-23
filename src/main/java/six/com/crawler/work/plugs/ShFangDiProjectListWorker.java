package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.common.utils.WebDriverUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月4日 下午2:12:53
 */
public class ShFangDiProjectListWorker extends AbstractCrawlWorker {

	String searchPageUlr = "http://www.fangdi.com.cn/complexPro.asp";
	String nextXpath = "//table[@id='Table7']/tbody/tr/td/a";
	String districtXpath = "//select[@name='districtID']/option";// 选择区县xpath
	String searchBtXpath = "//input[@name='imageField3']";// 选择区县xpath
	String projectInfoUrlKey = "项目详细_url";
	Queue<String> districtQueue;
	Set<String> districtSet;
	int findElementTimeout = 1000;
	RedisWorkQueue projectInfoQueue;
	String doingDistrict;

	public ShFangDiProjectListWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		WebDriver webDriver = getDowner().getWebDriver();
		if (null == districtQueue) {
			districtQueue = new LinkedBlockingQueue<String>();
			districtSet = new HashSet<>();
			List<WebElement> webElements = WebDriverUtils.findElements(webDriver, districtXpath, findElementTimeout);
			String district = null;
			for (WebElement webElement : webElements) {
				district = webElement.getText();
				if (StringUtils.isNotBlank(district)) {
					districtQueue.add(district);
					districtSet.add(district);
				}
			}
		}

		if (null == doingDistrict) {
			doingDistrict = districtQueue.poll();
			if (null != doingDistrict) {
				List<WebElement> webElements = WebDriverUtils.findElements(webDriver, districtXpath,
						findElementTimeout);
				WebElement searchBt = WebDriverUtils.findElement(webDriver, searchBtXpath, findElementTimeout);
				String district = null;
				for (WebElement webElement : webElements) {
					district = webElement.getText();
					if (doingDistrict.equalsIgnoreCase(district)) {
						WebDriverUtils.click(webDriver, webElement, null, findElementTimeout);
						WebDriverUtils.click(webDriver, searchBt, null, findElementTimeout);
						doWoerk(doingPage);
						break;
					}
				}

			} else {
				// 没有处理数据时 设置 state == WorkerLifecycleState.SUSPEND
				compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			}
		} else {
			WebElement nexpWebElement = getNextWebElement(webDriver);
			if (null != nexpWebElement) {
				WebDriverUtils.click(webDriver, nexpWebElement, null, findElementTimeout);
				doWoerk(doingPage);
			} else {
				districtSet.remove(doingDistrict);
				doingDistrict = null;
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	private WebElement getNextWebElement(WebDriver webDriver) {
		List<WebElement> nextWebElements = WebDriverUtils.findElements(webDriver, nextXpath, findElementTimeout);
		WebElement nextWebElement = null;
		for (WebElement webElement : nextWebElements) {
			String isNext = webElement.getText();
			if (StringUtils.contains(isNext, "下一页")) {
				nextWebElement = webElement;
				break;
			}
		}
		return nextWebElement;
	}

	private void doWoerk(Page doingPage) {
		WebDriver webDriver = getDowner().getWebDriver();
		String 状态Xpath = "//center/table[6]/tbody/tr[@valign='middle']/td[1]";
		String 项目名称Xpath = "//center/table[6]/tbody/tr[@valign='middle']/td[2]";
		String 项目地址Xpath = "//center/table[6]/tbody/tr[@valign='middle']/td[3]";
		String 总套数Xpath = "//center/table[6]/tbody/tr[@valign='middle']/td[4]";
		String 总面积Xpath = "//center/table[6]/tbody/tr[@valign='middle']/td[5]";
		String 所在区县Xpath = "//center/table[6]/tbody/tr[@valign='middle']/td[6]";
		String 项目详细UrlXpath = "//center/table[6]/tbody/tr[@valign='middle']/td[2]/a";
		List<WebElement> 状态Elements = WebDriverUtils.findElements(webDriver, 状态Xpath, findElementTimeout);
		List<WebElement> 项目名称Elements = WebDriverUtils.findElements(webDriver, 项目名称Xpath, findElementTimeout);
		List<WebElement> 项目地址Elements = WebDriverUtils.findElements(webDriver, 项目地址Xpath, findElementTimeout);
		List<WebElement> 总套数Elements = WebDriverUtils.findElements(webDriver, 总套数Xpath, findElementTimeout);
		List<WebElement> 总面积Elements = WebDriverUtils.findElements(webDriver, 总面积Xpath, findElementTimeout);
		List<WebElement> 所在区县Elements = WebDriverUtils.findElements(webDriver, 所在区县Xpath, findElementTimeout);
		List<WebElement> 项目详细UrlElements = WebDriverUtils.findElements(webDriver, 项目详细UrlXpath, findElementTimeout);

		List<String> 状态List = new ArrayList<String>();
		List<String> 项目名称List = new ArrayList<String>();
		List<String> 项目地址List = new ArrayList<String>();
		List<String> 总套数List = new ArrayList<String>();
		List<String> 总面积List = new ArrayList<String>();
		List<String> 所在区县List = new ArrayList<String>();

		String 状态 = null;
		String 项目名称 = null;
		String 项目地址 = null;
		String 总套数 = null;
		String 总面积 = null;
		String 所在区县 = null;
		String 项目详细Url = null;
		WebElement tempWebElement = null;
		Page 项目详细Page = null;
		for (int i = 0; i < 状态Elements.size(); i++) {
			tempWebElement = 状态Elements.get(i);
			状态 = tempWebElement.getText();
			状态List.add(状态);

			tempWebElement = 项目名称Elements.get(i);
			项目名称 = tempWebElement.getText();
			项目名称List.add(项目名称);

			tempWebElement = 项目地址Elements.get(i);
			项目地址 = tempWebElement.getText();
			项目地址List.add(项目地址);

			tempWebElement = 总套数Elements.get(i);
			总套数 = tempWebElement.getText();
			总套数List.add(总套数);

			tempWebElement = 总面积Elements.get(i);
			总面积 = tempWebElement.getText();
			总面积List.add(总面积);

			tempWebElement = 所在区县Elements.get(i);
			所在区县 = tempWebElement.getText();
			所在区县List.add(所在区县);

			tempWebElement = 项目详细UrlElements.get(i);
			项目详细Url = tempWebElement.getAttribute("href");
			项目详细Url = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), 项目详细Url);
			项目详细Page = new Page(doingPage.getSiteCode(), 1, 项目详细Url, 项目详细Url);
			项目详细Page.setReferer(doingPage.getFinalUrl());
			项目详细Page.setType(PageType.DATA.value());
			projectInfoQueue.push(项目详细Page);

		}

		doingPage.getMetaMap().put("status", 状态List);
		doingPage.getMetaMap().put("projectName", 项目名称List);
		doingPage.getMetaMap().put("address", 项目地址List);
		doingPage.getMetaMap().put("totalNum", 总套数List);
		doingPage.getMetaMap().put("totalArea", 总面积List);
		doingPage.getMetaMap().put("district", 所在区县List);
		ResultContext resultContext = getExtracter().extract(doingPage);
		getStore().store(resultContext);
	}

	@Override
	protected void insideInit() {
		projectInfoQueue = new RedisWorkQueue(getManager().getRedisManager(), "sh_fangdi_project_info");
	}

	@Override
	public void onComplete(Page p) {

	}

}
