package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.ThreadUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.common.utils.WebDriverUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月1日 下午4:29:31
 */
public class SzplGovSuiteStateWorker extends HtmlCommonWorker {

	final static Logger LOG = LoggerFactory.getLogger(SzplGovSuiteStateWorker.class);
	RedisWorkQueue suiteInfoQueue;
	String suiteCodeXpath = "//div[@id='updatepanel1']/table[3]/tbody/tr[@class='a1']/td/div[contains(text(),'房号')]";
	String suiteInfoXpath = "//div[@id='updatepanel1']/table[3]/tbody/tr[@class='a1']/td/div/a";
	String suiteStateXpath = "//div[@id='updatepanel1']/table[3]/tbody/tr[@class='a1']/td/div/a/img";
	String branchXpath = "//div[@id='divShowBranch']/a";
	String selectBranchXpath = "//div[@id='divShowBranch']/font";

	String preSaleBtXpath = "//a[@id='imgBt1']";
	String nowSaleBtXpath = "//a[@id='imgBt2']";
	List<String> saleStateBtXpathList = new ArrayList<>();
	Map<String, String> stateMap;

	public SzplGovSuiteStateWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		suiteInfoQueue = new RedisWorkQueue(getManager().getRedisManager(),
				"szpl_gov_suite_info_detail");
		saleStateBtXpathList = new ArrayList<>();
		saleStateBtXpathList.add(preSaleBtXpath);
		saleStateBtXpathList.add(nowSaleBtXpath);
		stateMap = new HashMap<>();
		stateMap.put("imc/b1_2.gif", "期房待售");
		stateMap.put("imc/b3.gif", "已签销售合同");
		stateMap.put("imc/b2.gif", "已签备案");
		stateMap.put("imc/b10.gif", "已签认购书");
		stateMap.put("imc/b123.gif", "初始登记");
		stateMap.put("imc/bz3_n.gif", "管理局锁定");
		stateMap.put("imc/b1_3.gif", "安居房");
		stateMap.put("imc/bz1.gif", "自动锁定");
		stateMap.put("imc/b6_1.gif", "司法查封");
		stateMap.put("imc/b4.gif", "未批准");
	}

	@Override
	public void onComplete(Page p) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		WebDriver webDriver = getDowner().getWebDriver();
		List<String> 项目名称list = doingPage.getMetaMap().remove("projectName");
		List<String> 楼名list = doingPage.getMetaMap().remove("floorName");
		List<String> 建设工程规划许可证list = doingPage.getMetaMap().remove("constructionPlanPermit");
		List<String> 建筑工程施工许可证list = doingPage.getMetaMap().remove("constructionPermit");

		String 项目名称 = 项目名称list.get(0);
		String 楼名 = 楼名list.get(0);
		String 建设工程规划许可证 = 建设工程规划许可证list.get(0);
		String 建筑工程施工许可证 = 建筑工程施工许可证list.get(0);

		项目名称list = new ArrayList<String>();
		楼名list = new ArrayList<String>();
		建设工程规划许可证list = new ArrayList<String>();
		建筑工程施工许可证list = new ArrayList<String>();

		List<String> suiteCodeList = new ArrayList<String>();
		List<String> 座号list = new ArrayList<>();
		List<String> 销售类型list = new ArrayList<>();
		List<String> suiteStateList = new ArrayList<String>();

		String saleType = "预售";
		for (String saleStateBtXpath : saleStateBtXpathList) {
			WebElement saleStateBt = WebDriverUtils.findElement(webDriver, saleStateBtXpath, findElementTimeout);
			if (null != saleStateBt) {
				String tempSaleType = saleStateBt.getText();
				if (!StringUtils.contains(tempSaleType, "预售")) {
					WebDriverUtils.click(webDriver, saleStateBt, saleStateBtXpath, findElementTimeout);
					ThreadUtils.sleep(500);
				}
				saleType = tempSaleType;
			}
			Queue<String> branchQueue = new LinkedBlockingQueue<>();
			List<WebElement> branchWebElements = WebDriverUtils.findElements(webDriver, branchXpath,
					findElementTimeout);
			for (int i = 0; i < branchWebElements.size(); i++) {
				WebElement branchWebElement = branchWebElements.get(i);
				String branch = branchWebElement.getText();
				branchQueue.add(branch);
			}
			boolean flag = false;
			do {
				flag = false;
				WebElement branchElement = WebDriverUtils.findElement(webDriver, selectBranchXpath, findElementTimeout);
				String branch = "";
				if (null != branchElement) {
					branch = branchElement.getText();
				}
				List<WebElement> suiteInfoList = WebDriverUtils.findElements(webDriver, suiteInfoXpath,
						findElementTimeout);
				String suiteInfoUrl = null;
				Page suiteInfoPage = null;
				if (null != suiteInfoList && suiteInfoList.size() > 0) {
					for (WebElement element : suiteInfoList) {
						suiteInfoUrl = element.getAttribute("href");
						suiteInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), suiteInfoUrl);
						suiteInfoPage = new Page(doingPage.getSiteCode(), 1, suiteInfoUrl, suiteInfoUrl);
						suiteInfoPage.setReferer(doingPage.getFinalUrl());
						suiteInfoPage.setType(doingPage.getType().value());
						suiteInfoPage.getMetaMap().put("projectName", Arrays.asList(项目名称));
						suiteInfoPage.getMetaMap().put("floorName", Arrays.asList(楼名));
						suiteInfoPage.getMetaMap().put("constructionPlanPermit", Arrays.asList(建设工程规划许可证));
						suiteInfoPage.getMetaMap().put("constructionPermit", Arrays.asList(建筑工程施工许可证));
						if (!suiteInfoQueue.duplicateKey(suiteInfoPage.getPageKey())) {
							suiteInfoQueue.push(suiteInfoPage);
						}
					}

					List<WebElement> suiteCodeElementList = WebDriverUtils.findElements(webDriver, suiteCodeXpath,
							findElementTimeout);
					for (WebElement element : suiteCodeElementList) {
						String suiteCode = element.getText();
						suiteCode = StringUtils.remove(suiteCode, "房号：");
						suiteCode = StringUtils.remove(suiteCode, "房号:");
						suiteCode = StringUtils.remove(suiteCode, "房号");
						suiteCodeList.add(suiteCode);
						项目名称list.add(项目名称);
						楼名list.add(楼名);
						建设工程规划许可证list.add(建设工程规划许可证);
						建筑工程施工许可证list.add(建筑工程施工许可证);
						座号list.add(branch);
						销售类型list.add(saleType);
					}
					List<WebElement> suiteStateElementList = WebDriverUtils.findElements(webDriver, suiteStateXpath,
							findElementTimeout);
					for (WebElement element : suiteStateElementList) {
						String suiteState = element.getAttribute("src");
						String state = null;
						for (String stateMapKey : stateMap.keySet()) {
							if (StringUtils.contains(suiteState, stateMapKey)) {
								state = stateMap.get(stateMapKey);
								break;
							}
						}
						if (null == state) {
							throw new RuntimeException("unkonw suite state:" + doingPage.getFinalUrl());
						}
						suiteStateList.add(state);
					}
				}
				String queueBranch = branchQueue.poll();
				if (null != queueBranch) {
					branchWebElements = WebDriverUtils.findElements(webDriver, branchXpath, findElementTimeout);
					for (int i = 0; i < branchWebElements.size(); i++) {
						WebElement branchWebElement = branchWebElements.get(i);
						String tempBranch = branchWebElement.getText();
						if (queueBranch.equals(tempBranch)) {
							WebDriverUtils.click(webDriver, branchWebElement, null, findElementTimeout);
							flag = true;
							ThreadUtils.sleep(500);
							break;
						}
					}
				}
			} while (flag);
		}

		doingPage.getMetaMap().put("projectName", 项目名称list);
		doingPage.getMetaMap().put("floorName", 楼名list);
		doingPage.getMetaMap().put("constructionPlanPermit", 建设工程规划许可证list);
		doingPage.getMetaMap().put("constructionPermit", 建筑工程施工许可证list);

		doingPage.getMetaMap().put("houseNo", suiteCodeList);
		doingPage.getMetaMap().put("status", suiteStateList);
		doingPage.getMetaMap().put("floorNo", 座号list);
		doingPage.getMetaMap().put("saleType", 销售类型list);
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {

	}

}
