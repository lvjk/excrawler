package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ThreadUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.utils.WebDriverUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkerLifecycleState;
import six.com.crawler.work.space.WorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月20日 上午10:50:04
 */
public class XiAnFang99PresaleWorker extends AbstractCrawlWorker {

	private String openAllPath = "//table/tbody/tr/td/img[@class='cursor']";
	private String nextPagePath = "//div[@id='pager_presale']/a[contains(text(),'>')]";
	WorkSpace<Page> projectInfoQueue;
	WorkSpace<Page> buildingInfoQueue;

	@Override
	protected void insideInit() {
		projectInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("xianfang99_project_info_1", Page.class);
		buildingInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("xianfang99_house_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		WebDriver webDriver = getDowner().getWebDriver();
		List<WebElement> openAllWebElements = WebDriverUtils.findElements(webDriver, openAllPath, findElementTimeout);
		if (null != openAllWebElements) {
			for (int i = 0; i < openAllWebElements.size(); i++) {
				WebElement openAllWebElement = openAllWebElements.get(i);
				String display = openAllWebElement.getCssValue("display");
				if (!"none".equals(display)) {
					WebDriverUtils.click(webDriver, openAllWebElement, null, findElementTimeout);
					ThreadUtils.sleep(1000);
				}
			}
		}
		String html = webDriver.getPageSource();
		Document doc = Jsoup.parse(html);
		String prosaleInfoTableCss = "div[class=fwzx_cat]>div[class=fwzx_catnr]>table[class=fwzx_bgbk]";
		String projectNameCss = "tbody>tr:eq(0)>td";
		String projectInfoUrlCss = "tbody>tr:eq(1)>td:eq(1)>a";
		String presaleInfoCss = "tbody>tr:eq(1)>td";
		String baseProsaleInfoCss = "table";
		String otherProsaleInfoCss = "div>table";
		Elements presaleInfoTables = doc.select(prosaleInfoTableCss);

		doingPage.getMetaMap().put("projectName", new ArrayList<>());
		doingPage.getMetaMap().put("presalePermit", new ArrayList<>());
		doingPage.getMetaMap().put("loudongName", new ArrayList<>());
		doingPage.getMetaMap().put("developer", new ArrayList<>());
		doingPage.getMetaMap().put("issueDate", new ArrayList<>());
		doingPage.getMetaMap().put("saleLoudong", new ArrayList<>());
		doingPage.getMetaMap().put("presaleSuperviseBank", new ArrayList<>());
		doingPage.getMetaMap().put("presaleSuperviseAccount", new ArrayList<>());

		if (null != presaleInfoTables) {
			for (int i = 0; i < presaleInfoTables.size(); i++) {
				Element presaleInfoElement = presaleInfoTables.get(i);
				String preject = presaleInfoElement.select(projectNameCss).first().text();
				Element presaleInfo = presaleInfoElement.select(presaleInfoCss).first();
				Element tempTable1 = presaleInfo.select(baseProsaleInfoCss).first();
				Element projectInfoUrlElement = tempTable1.select(projectInfoUrlCss).first();
				String projectInfoUrl = projectInfoUrlElement.attr("href");
				projectInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), projectInfoUrl);

				Page projectInfoPage = new Page(doingPage.getSiteCode(), 1, projectInfoUrl, projectInfoUrl);
				projectInfoPage.setReferer(doingPage.getFinalUrl());
				projectInfoPage.setType(PageType.DATA.value());

				if (!projectInfoQueue.isDone(projectInfoPage.getPageKey())) {
					projectInfoQueue.push(projectInfoPage);
				}
				Elements tempTable2 = presaleInfo.select(otherProsaleInfoCss);
				tempTable2.add(0, tempTable1);
				paserPresaleTable(doingPage, preject, tempTable2);
			}
		}
		ResultContext resultContext = getExtracter().extract(doingPage);
		getStore().store(resultContext);
		WebElement nextPageElements = WebDriverUtils.findElement(webDriver, nextPagePath, findElementTimeout);
		if (null == nextPageElements) {
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
		} else {
			String disabled = nextPageElements.getAttribute("disabled");
			if ("disabled".equals(disabled) || "true".equals(disabled)) {
				compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
			} else {
				WebDriverUtils.click(webDriver, nextPageElements, nextPagePath, findElementTimeout);
			}
		}

	}

	private void paserPresaleTable(Page doingPage, String projectName, Elements presaleTable) {
		String presaleCodeCss = "tbody>tr:eq(1)>td:eq(0)>a";
		String loudongNameCss = "tbody>tr:eq(1)>td:eq(1)>a";
		String developerCss = "tbody>tr:eq(1)>td:eq(2)";
		String issueDateCss = "tbody>tr:eq(1)>td:eq(3)";
		String saleLoudongCss = "tbody>tr:eq(2)>td>table>tbody>tr:eq(1)>td:eq(0)>span>a";
		String presaleSuperviseBankCss = "tbody>tr:eq(2)>td>table>tbody>tr:eq(1)>td:eq(1)";
		String presaleSuperviseAccountCss = "tbody>tr:eq(2)>td>table>tbody>tr:eq(1)>td:eq(2)";
		for (int i = 0; i < presaleTable.size(); i++) {
			Element table = presaleTable.get(i);
			String presalePermit = table.select(presaleCodeCss).first().text();
			String loudongName = table.select(loudongNameCss).first().text();
			String developer = table.select(developerCss).first().text();
			String issueDate = table.select(issueDateCss).first().text();

			Elements saleLoudongElements = table.select(saleLoudongCss);
			String saleLoudong = "";
			for (Element saleLoudongElement : saleLoudongElements) {
				String tempSaleLoudong = saleLoudongElement.text();
				saleLoudong += tempSaleLoudong + ";";
				String saleLoudongInfoUrl = saleLoudongElement.attr("href");
				saleLoudongInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(),
						saleLoudongInfoUrl);
				Page saleLoudongInfoPage = new Page(doingPage.getSiteCode(), 1, saleLoudongInfoUrl, saleLoudongInfoUrl);
				saleLoudongInfoPage.setReferer(doingPage.getFinalUrl());
				saleLoudongInfoPage.setType(PageType.DATA.value());

				saleLoudongInfoPage.getMetaMap().put("projectName", Arrays.asList(projectName));
				saleLoudongInfoPage.getMetaMap().put("presalePermit", Arrays.asList(presalePermit));
				saleLoudongInfoPage.getMetaMap().put("loudongName", Arrays.asList(loudongName));
				saleLoudongInfoPage.getMetaMap().put("saleLoudong", Arrays.asList(tempSaleLoudong));
				buildingInfoQueue.push(saleLoudongInfoPage);
			}

			String presaleSuperviseBank = "";
			Element presaleSuperviseBankElement = table.select(presaleSuperviseBankCss).first();
			if (null != presaleSuperviseBankElement) {
				presaleSuperviseBank = presaleSuperviseBankElement.text();
			}

			String presaleSuperviseAccount = "";
			Element presaleSuperviseAccountElement = table.select(presaleSuperviseAccountCss).first();
			if (null != presaleSuperviseAccountElement) {
				presaleSuperviseAccount = presaleSuperviseAccountElement.text();
			}

			doingPage.getMetaMap().get("projectName").add(projectName);
			doingPage.getMetaMap().get("presalePermit").add(presalePermit);
			doingPage.getMetaMap().get("loudongName").add(loudongName);

			doingPage.getMetaMap().get("developer").add(developer);
			doingPage.getMetaMap().get("issueDate").add(issueDate);
			doingPage.getMetaMap().get("saleLoudong").add(saleLoudong);

			doingPage.getMetaMap().get("presaleSuperviseBank").add(presaleSuperviseBank);
			doingPage.getMetaMap().get("presaleSuperviseAccount").add(presaleSuperviseAccount);
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	public void onComplete(Page p, ResultContext resultContext) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
