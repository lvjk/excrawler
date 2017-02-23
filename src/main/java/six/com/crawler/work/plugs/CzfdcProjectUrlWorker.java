package six.com.crawler.work.plugs;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
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
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月24日 上午11:02:39 常州房地产 新房信息url worker 此网站存在 相同url指向不同项目
 *       ，所以不能使用url去重
 */
public class CzfdcProjectUrlWorker extends AbstractCrawlWorker {
	String newHouseUrlXpat = "//table[@id='hList']/tbody/tr/td/table/tbody/tr/td[2]/span/a";
	String nextPageXpath = "//td[@id='getPage']/a/div[contains(text(),'下一页')]";
	String seedPage = "http://www.czfdc.com.cn/NodePG/House/HouseList/houseList.htm";
	String pageCountXpath = "//td[@id='getPage']/a/div";
	String selectPageIndexXpath = "//td[@id='getPage']/a/div[@class='pageClass04']";
	int maxPageNum;

	public CzfdcProjectUrlWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		WebDriver webDriver = getDowner().getWebDriver();
		List<WebElement> pageCountElements = WebDriverUtils.findElements(webDriver, pageCountXpath, findElementTimeout);
		String pageNum = null;
		int tempMaxPageNum = 1;
		for (WebElement pageCountElement : pageCountElements) {
			pageNum = pageCountElement.getText();
			if (NumberUtils.isNumber(pageNum)) {
				tempMaxPageNum = Integer.valueOf(pageNum);
				if (tempMaxPageNum > maxPageNum) {
					maxPageNum = tempMaxPageNum;
				}
			}
		}
	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		WebDriver webDriver = getDowner().getWebDriver();
		List<WebElement> 项目名称Elements = WebDriverUtils.findElements(webDriver, newHouseUrlXpat, findElementTimeout);
		String tempUrl = null;
		Page newPage = null;
		for (WebElement 项目名称Element : 项目名称Elements) {
			tempUrl = 项目名称Element.getAttribute("href");
			tempUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), tempUrl);
			newPage = new Page(doingPage.getSiteCode(), 1, tempUrl, tempUrl);
			newPage.setReferer(doingPage.getFinalUrl());
			newPage.setType(PageType.DATA.value());
			getWorkQueue().push(newPage);
		}
		WebElement selectPageIndexElement = WebDriverUtils.findElement(webDriver, selectPageIndexXpath,
				findElementTimeout);
		if (null != selectPageIndexElement) {
			String selectPageIndexStr = selectPageIndexElement.getText();
			int selectPageIndex = Integer.valueOf(selectPageIndexStr);
			if (selectPageIndex >= maxPageNum) {
				// 没有处理数据时 设置 state == WorkerLifecycleState.STOPED
				compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			} else {
				WebElement nextPageElement = WebDriverUtils.findElement(webDriver, nextPageXpath, findElementTimeout);
				WebDriverUtils.click(webDriver, nextPageElement, nextPageXpath, findElementTimeout);
			}
		} else {
			// 没有处理数据时 设置 state == WorkerLifecycleState.STOPED
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	@Override
	public void onComplete(Page p) {

	}

}
