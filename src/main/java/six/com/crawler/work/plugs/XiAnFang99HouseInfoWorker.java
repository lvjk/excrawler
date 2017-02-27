package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.utils.WebDriverUtils;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月3日 下午2:13:43
 */
public class XiAnFang99HouseInfoWorker extends AbstractCrawlWorker {

	Map<String, String> saleTypeMap;

	@Override
	protected void insideInit() {
		saleTypeMap = new HashMap<>();
		saleTypeMap.put("预售", "//table[@class='sf_lpb_tj']/tbody/tr/td/ul/li/a[contains(text(),'预售')]");
		saleTypeMap.put("现售", "//table[@class='sf_lpb_tj']/tbody/tr/td/ul/li/a[contains(text(),'现售')]");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		WebDriver webDriver = getDowner().getWebDriver();
		for (String saleType : saleTypeMap.keySet()) {
			String saleTypeXpath = saleTypeMap.get(saleType);
			WebElement saleTypeWebElement = WebDriverUtils.findElement(webDriver, saleTypeXpath, findElementTimeout);
			WebDriverUtils.click(webDriver, saleTypeWebElement, saleTypeXpath, findElementTimeout);

			String html = webDriver.getPageSource();
			Document doc = Jsoup.parse(html);
			// 获取 单元信息
			String unitCss = "div[id=erp_con_1]>div>table>tbody>tr:eq(0)>td>div>div";
			Elements unitElements = doc.select(unitCss);
			List<String> units = new ArrayList<String>(unitElements.size());
			for (Element unitElement : unitElements) {
				String tempUnit = unitElement.text();
				units.add(tempUnit);
			}

			// 获取 每个tr 信息
			String trCss = "div[id=erp_con_1]>div>table>tbody>tr";
			String floorCss = "td>input";
			String houseTdCss = "td>div>table>tbody>tr>td";
			String houseXpath = "span";
			Elements trElements = doc.select(trCss);
			if (trElements.size() > 1) {
				// 从i=1开始迭代 ,i=0为 单元信息
				for (int i = 1; i < trElements.size(); i++) {
					Element trElement = trElements.get(i);
					Element floorElement = trElement.select(floorCss).first();
					String floor = floorElement.attr("value");
					Elements houseTdElements = trElement.select(houseTdCss);
					for (int j = 0; j < houseTdElements.size(); j++) {
						String unit = "#";
						if (j < units.size()) {
							unit = units.get(j);
						}
						Element houseTdElement = houseTdElements.get(j);
						Elements houseElements = houseTdElement.select(houseXpath);
						for (Element houseElement : houseElements) {
							String houseID = houseElement.text();
							String tempClass = houseElement.attr("class");
							String status = "";
							if ("hstate01".equals(tempClass)) {
								status = "不可售";
							} else if ("hstate02".equals(tempClass)) {
								status = "未销售备案";
							} else if ("hstate03".equals(tempClass)) {
								status = "已销售备案";
							}
							String tempTitle = houseElement.attr("title");
							String[] tempTitles = tempTitle.split(",");
							String houseType = tempTitles[0];
							String buildArea = tempTitles[1];
							doingPage.getMetaMap().computeIfAbsent("houseID", mapKey -> new ArrayList<>()).add(houseID);// 房间号
							doingPage.getMetaMap().computeIfAbsent("floor", mapKey -> new ArrayList<>()).add(floor);// 层数
							doingPage.getMetaMap().computeIfAbsent("unit", mapKey -> new ArrayList<>()).add(unit);// 单元号
							doingPage.getMetaMap().computeIfAbsent("houseType", mapKey -> new ArrayList<>())
									.add(houseType);// 房屋户型
							// 少一个房屋用途
							doingPage.getMetaMap().computeIfAbsent("saleType", mapKey -> new ArrayList<>())
									.add(saleType);// 房屋类型（销售）
							doingPage.getMetaMap().computeIfAbsent("buildArea", mapKey -> new ArrayList<>())
									.add(buildArea);// 建筑面积
							doingPage.getMetaMap().computeIfAbsent("status", mapKey -> new ArrayList<>()).add(status);// 房屋状态
						}
					}
				}
				List<String> projectNames = doingPage.getMetaMap().remove("projectName");
				List<String> presalePermits = doingPage.getMetaMap().remove("presalePermit");
				List<String> loudongNames = doingPage.getMetaMap().remove("loudongName");
				List<String> saleLoudongs = doingPage.getMetaMap().remove("saleLoudong");
				String projectName = projectNames.get(0);
				String presalePermit = presalePermits.get(0);
				String loudongName = loudongNames.get(0);
				String saleLoudong = saleLoudongs.get(0);
				List<String> houseIDs = doingPage.getMetaMap().get("houseID");
				if (null != houseIDs) {
					projectNames = new ArrayList<>();
					presalePermits = new ArrayList<>();
					loudongNames = new ArrayList<>();
					saleLoudongs = new ArrayList<>();
					for (int i = 0; i < houseIDs.size(); i++) {
						projectNames.add(projectName);// 项目名称
						presalePermits.add(presalePermit);// 预售证
						loudongNames.add(loudongName);// 楼盘备案名称
						saleLoudongs.add(saleLoudong);// 销售楼栋
					}
					doingPage.getMetaMap().put("projectName", projectNames);
					doingPage.getMetaMap().put("presalePermit", presalePermits);
					doingPage.getMetaMap().put("loudongName", loudongNames);
					doingPage.getMetaMap().put("saleLoudong", saleLoudongs);
				}
			} else {
				// throw new RuntimeException("this page don't have house");
			}
		}

	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage,ResultContext resultContext) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

}
