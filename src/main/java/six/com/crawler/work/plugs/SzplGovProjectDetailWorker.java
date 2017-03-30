package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.utils.WebDriverUtils;
import six.com.crawler.utils.JsoupUtils.TableResult;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.RedisWorkSpace;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月31日 上午10:06:39
 */
public class SzplGovProjectDetailWorker extends AbstractCrawlWorker {

	Map<String, String> fieldMap = new HashMap<String, String>();
	RedisWorkSpace<Page> suiteQueue;

	private void doSuite(WebDriver driver, Page page) {
		String 项目名称Xpath = "//table[@id='DataList1']/tbody/tr[@bgcolor='#F5F9FC']/td[1]";
		String 楼名Xpath = "//table[@id='DataList1']/tbody/tr[@bgcolor='#F5F9FC']/td[2]";
		String 建设工程规划许可证Xpath = "//table[@id='DataList1']/tbody/tr[@bgcolor='#F5F9FC']/td[3]";
		String 建筑工程施工许可证Xpath = "//table[@id='DataList1']/tbody/tr[@bgcolor='#F5F9FC']/td[4]";
		String 套房信息UlrXpath = "//table[@id='DataList1']/tbody/tr[@bgcolor='#F5F9FC']/td[5]/a";
		List<WebElement> 项目名称Elments = WebDriverUtils.findElements(driver, 项目名称Xpath, findElementTimeout);
		List<WebElement> 楼名Elments = WebDriverUtils.findElements(driver, 楼名Xpath, findElementTimeout);
		List<WebElement> 建设工程规划许可证Elments = WebDriverUtils.findElements(driver, 建设工程规划许可证Xpath, findElementTimeout);
		List<WebElement> 建筑工程施工许可证Elments = WebDriverUtils.findElements(driver, 建筑工程施工许可证Xpath, findElementTimeout);
		List<WebElement> 套房信息UlrElments = WebDriverUtils.findElements(driver, 套房信息UlrXpath, findElementTimeout);
		WebElement 项目名称Elment = null;
		WebElement 楼名Elment = null;
		WebElement 建设工程规划许可证Elment = null;
		WebElement 建筑工程施工许可证Elment = null;
		WebElement 套房信息UlrElment = null;
		String 项目名称 = null;
		String 楼名 = null;
		String 建设工程规划许可证 = null;
		String 建筑工程施工许可证 = null;
		String 套房信息Url = null;
		Page 套房信息Page = null;
		List<String> 项目名称list = null;
		List<String> 楼名list = null;
		List<String> 建设工程规划许可证list = null;
		List<String> 建筑工程施工许可证list = null;
		for (int i = 0; i < 项目名称Elments.size(); i++) {
			项目名称list = new ArrayList<>();
			楼名list = new ArrayList<>();
			建设工程规划许可证list = new ArrayList<>();
			建筑工程施工许可证list = new ArrayList<>();

			项目名称Elment = 项目名称Elments.get(i);
			项目名称 = 项目名称Elment.getText();
			项目名称list.add(项目名称);

			楼名Elment = 楼名Elments.get(i);
			楼名 = 楼名Elment.getText();
			楼名list.add(楼名);

			建设工程规划许可证Elment = 建设工程规划许可证Elments.get(i);
			建设工程规划许可证 = 建设工程规划许可证Elment.getText();
			建设工程规划许可证list.add(建设工程规划许可证);

			建筑工程施工许可证Elment = 建筑工程施工许可证Elments.get(i);
			建筑工程施工许可证 = 建筑工程施工许可证Elment.getText();
			建筑工程施工许可证list.add(建筑工程施工许可证);

			套房信息UlrElment = 套房信息UlrElments.get(i);
			套房信息Url = 套房信息UlrElment.getAttribute("href");

			套房信息Url = UrlUtils.paserUrl(page.getBaseUrl(), page.getFinalUrl(), 套房信息Url);
			套房信息Page = new Page(page.getSiteCode(), 1, 套房信息Url, 套房信息Url);
			套房信息Page.setType(PageType.DATA.value());
			套房信息Page.setReferer(page.getFinalUrl());

			套房信息Page.getMetaMap().put("projectName", 项目名称list);
			套房信息Page.getMetaMap().put("floorName", 楼名list);
			套房信息Page.getMetaMap().put("constructionPlanPermit", 建设工程规划许可证list);
			套房信息Page.getMetaMap().put("constructionPermit", 建筑工程施工许可证list);
			suiteQueue.push(套房信息Page);
		}
	}

	private void doPoject(WebDriver driver, Page page) {
		String trXpath = "//table/tbody/tr[@class='a1']/../..";
		WebElement tableElment = WebDriverUtils.findElement(driver, trXpath, findElementTimeout);
		String tableHtml = tableElment.getText();
		tableHtml = tableElment.getAttribute("outerHTML");// innerHTML
		Document table = Jsoup.parse(tableHtml);
		Element removeElement = table.getElementById("DataList1");
		if (null != removeElement) {
			Element tempElment = removeElement.parent().parent();
			tempElment.remove();
		}
		List<TableResult> results = JsoupUtils.paserTable(table);
		List<String> list = null;
		String key = null;
		String value = null;
		Map<String, List<String>> metaMap = new HashMap<>();
		for (TableResult result : results) {
			value = result.getValue();
			boolean isAdd = false;
			for (String field : fieldMap.keySet()) {
				if (result.getKey().contains(field)) {
					key = fieldMap.get(field);
					isAdd = true;
					break;
				}
			}
			if (isAdd) {
				list = metaMap.computeIfAbsent(key, mapKey -> new ArrayList<>());
				if (list.size() > 0) {
					value = list.remove(0) + ";" + result.getValue();
				}
				list.add(value);
				metaMap.put(key, list);
			}
		}
		page.getMetaMap().putAll(metaMap);
	}

	@Override
	protected void insideInit() {
		suiteQueue = new RedisWorkSpace<Page>(getManager().getRedisManager(), "szpl_gov_suite_state",Page.class);
		fieldMap.put("项目名称", "projectName");
		fieldMap.put("宗地号", "landId");
		fieldMap.put("宗地位置", "address");
		fieldMap.put("受让日期", "farminDate");
		fieldMap.put("所在区域", "district");
		fieldMap.put("权属来源", "ownerSource");
		fieldMap.put("批准机关", "approvalAuthority");
		fieldMap.put("合同文号", "contractNumber");
		fieldMap.put("使用年限", "durableYears");
		fieldMap.put("补充协议", "sideAgreement");
		fieldMap.put("用地规划许可证", "presellId");
		fieldMap.put("房屋用途", "houseUse");
		fieldMap.put("土地用途", "landUse");
		fieldMap.put("土地等级", "landGrade");
		fieldMap.put("基地面积", "baseArea");
		fieldMap.put("宗地面积", "landArea");
		fieldMap.put("总建筑面积", "totalBuildArea");
		fieldMap.put("预售总套数", "preSaletotalNum");
		fieldMap.put("预售总面积", "preSaletotalArea");
		fieldMap.put("现售总套数", "saletotalNum");
		fieldMap.put("现售总面积", "saletotalArea");
		fieldMap.put("售楼电话", "saleCall");
		fieldMap.put("价款监管机构", "priceRegulator");
		fieldMap.put("账户名称", "accountName");
		fieldMap.put("账号", "account");
		fieldMap.put("工程监管机构", "projectregulator");
		fieldMap.put("物业管理公司", "propertyCompany");
		fieldMap.put("管理费", "managementCost");
		fieldMap.put("备注", "remark");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		WebDriver webDriver = getDowner().getWebDriver();
		doPoject(webDriver, doingPage);
		doSuite(webDriver, doingPage);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	public void onComplete(Page p,ResultContext resultContext) {

	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}

}
