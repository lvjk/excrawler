package six.com.crawler.work.plugs;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.WebDriverUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.WorkQueue;
import six.com.crawler.work.WorkerLifecycleState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月25日 下午3:26:15
 */
public class CzfdcPresaleInfoWorker extends HtmlCommonWorker {

	String presaleListXpath = "//table[@id='pageTable']/tbody/tr/td[@class='tb_middle'][1]/span";
	String nextPageXpath = "//table[@id='pageTable']/tbody/tr/td/table/tbody/tr/td/a[contains(text(),'下一页')]";
	String dataTableXpath = "//table[@id='jbxx']/tbody/tr";
	String mainWindows;

	Queue<String> queue;

	public CzfdcPresaleInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		mainWindows = getDowner().getWindowHandle();
	}
	
	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		final WebDriver webDriver = getDowner().getWebDriver();
		String lastNewWindows = WebDriverUtils.getOtherWindows(webDriver, mainWindows, findElementTimeout);
		if (StringUtils.isNotBlank(lastNewWindows)) {
			WebDriverUtils.switchToWindows(webDriver, lastNewWindows);
			webDriver.close();
			WebDriverUtils.switchToWindows(webDriver, mainWindows);
		}
		if (null == queue) {
			queue = new LinkedBlockingQueue<>();
			List<WebElement> presaleListElements = WebDriverUtils.findElements(webDriver, presaleListXpath,
					findElementTimeout);
			String preSale = null;
			if (null == presaleListElements || presaleListElements.isEmpty()) {
				// 没有处理数据时 设置 state == WorkerLifecycleState.STOPED
				compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			} else {
				for (WebElement presaleElement : presaleListElements) {
					preSale = StringUtils.trim(presaleElement.getText());
					queue.add(preSale);
				}
			}
		}

		if (null != queue && queue.isEmpty()) {
			queue = null;
			WebElement nextPage = WebDriverUtils.findElement(webDriver, nextPageXpath, findElementTimeout);
			if (null != nextPage) {
				WebDriverUtils.click(webDriver, nextPage, null, findElementTimeout);
			} else {
				// 没有处理数据时 设置 state == WorkerLifecycleState.STOPED
				compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			}
			return;
		}
		List<WebElement> presaleListElements = WebDriverUtils.findElements(webDriver, presaleListXpath,
				findElementTimeout);
		String doPreSale = queue.poll();
		int count = 0;
		for (WebElement presaleElement : presaleListElements) {
			String tempPreSale = StringUtils.trim(presaleElement.getText());
			if (tempPreSale.equalsIgnoreCase(doPreSale)) {
				WebDriverUtils.click(webDriver, presaleElement, null, findElementTimeout);
				List<WebElement> dataTableTrElements = null;
				while (null == dataTableTrElements || dataTableTrElements.size() == 0) {
					String newWindows = WebDriverUtils.getOtherWindows(webDriver, mainWindows, findElementTimeout);
					WebDriverUtils.switchToWindows(webDriver, newWindows);
					dataTableTrElements = WebDriverUtils.findElements(webDriver, dataTableXpath, findElementTimeout);
					count++;
					if (count > 3) {
						queue.add(doPreSale);
						return;
					}
				}
				ResultContext resultContext=getExtracter().extract(doingPage);
				getStore().store(resultContext);
				break;
			}
		}
	
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {
		
	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {
		
	}

	@Override
	public void onComplete(Page p) {

	}



	

}
