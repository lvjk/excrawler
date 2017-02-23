package six.com.crawler.work;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.WebDriverUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月28日 上午10:37:57
 */
public class SzplGovPreSaleWorker extends HtmlCommonWorker {

	String 许可证号Xpath = "//td[@valign='top']/table/tbody/tr[2]/td[2]";
	String 项目名称Xpath = "//td[@valign='top']/table/tbody/tr[2]/td[4]";
	String 发展商Xpath = "//td[@valign='top']/table/tbody/tr[3]/td[2]";
	String 所在位置Xpath = "//td[@valign='top']/table/tbody/tr[3]/td[4]";
	String 栋数Xpath = "//td[@valign='top']/table/tbody/tr[4]/td[2]";
	String 地块编号Xpath = "//td[@valign='top']/table/tbody/tr[4]/td[4]";

	String 房产证编号Xpath = "//td[@valign='top']/table/tbody/tr[5]/td[2]";
	String 批准面积Xpath = "//td[@valign='top']/table/tbody/tr[5]/td[4]";
	String 土地出让合同Xpath = "//td[@valign='top']/table/tbody/tr[6]/td[2]";
	String 批准日期Xpath = "//td[@valign='top']/table/tbody/tr[6]/td[4]";
	String 发证日期Xpath = "//td[@valign='top']/table/tbody/tr[7]/td[2]";

	String 用途trXpath = "//td[@valign='top']/table/tbody/tr";
	String 备注trXpath = "//td[@valign='top']/table/tbody/tr";

	int findElementTimeout = 1000;

	public SzplGovPreSaleWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
	}



	private List<WebElement> find用途Element(WebDriver webDriver) {
		String 用途Xpath = 用途trXpath + "/td[1]";
		List<WebElement> 用途WebElements = WebDriverUtils.findElements(webDriver, 用途Xpath, findElementTimeout);
		List<WebElement> result = new ArrayList<>();
		for (int i = 0; i < 用途WebElements.size(); i++) {
			WebElement webElement = 用途WebElements.get(i);
			String text = webElement.getText();
			if ("用途".equalsIgnoreCase(text)) {
				String tempXpath = 用途trXpath + "[" + (i + 1) + "]/td[2]";
				WebElement find = WebDriverUtils.findElement(webDriver, tempXpath, findElementTimeout);
				text = find.getText();
				if (!"--".equalsIgnoreCase(text.trim())) {
					result.add(find);
				}
			}
		}
		return result;
	}

	private List<WebElement> find面积Element(WebDriver webDriver) {
		String 用途Xpath = 用途trXpath + "/td[1]";
		List<WebElement> 用途WebElements = WebDriverUtils.findElements(webDriver, 用途Xpath, findElementTimeout);
		List<WebElement> result = new ArrayList<>();
		for (int i = 0; i < 用途WebElements.size(); i++) {
			WebElement webElement = 用途WebElements.get(i);
			String text = webElement.getText();
			if ("用途".equalsIgnoreCase(text)) {
				String tempXpath = 用途trXpath + "[" + (i + 1) + "]/td[4]";
				WebElement find = WebDriverUtils.findElement(webDriver, tempXpath, findElementTimeout);
				text = find.getText();
				result.add(find);
			}
		}
		return result;
	}

	private List<WebElement> find套数Element(WebDriver webDriver) {
		String 用途Xpath = 用途trXpath + "/td[1]";
		List<WebElement> 用途WebElements = WebDriverUtils.findElements(webDriver, 用途Xpath, findElementTimeout);
		List<WebElement> result = new ArrayList<>();
		for (int i = 0; i < 用途WebElements.size(); i++) {
			WebElement webElement = 用途WebElements.get(i);
			String text = webElement.getText();
			if ("用途".equalsIgnoreCase(text)) {
				String 用途tempXpath = 用途trXpath + "[" + (i + 1) + "]/td[6]";
				WebElement find = WebDriverUtils.findElement(webDriver, 用途tempXpath, findElementTimeout);
				text = find.getText();
				result.add(find);
			}
		}
		return result;
	}

	private WebElement find备注Element(WebDriver webDriver) {
		String 用途Xpath = 备注trXpath + "/td[1]";
		List<WebElement> 用途WebElements = WebDriverUtils.findElements(webDriver, 用途Xpath, findElementTimeout);
		WebElement result = null;
		for (int i = 0; i < 用途WebElements.size(); i++) {
			WebElement webElement = 用途WebElements.get(i);
			String text = webElement.getText();
			if ("备注".equalsIgnoreCase(text)) {
				String 用途tempXpath = 用途trXpath + "[" + (i + 1) + "]/td[2]";
				WebElement find = WebDriverUtils.findElement(webDriver, 用途tempXpath, findElementTimeout);
				text = find.getText();
				result = find;
			}
		}
		return result;
	}

	@Override
	public void onComplete(Page p) {
		
	}

	@Override
	public void insideOnError(Exception t, Page p) {
	
	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {


		WebDriver driver = getDowner().getWebDriver();
		WebElement 许可证号WebElement = WebDriverUtils.findElement(driver, 许可证号Xpath, findElementTimeout);
		String 许可证号 = 许可证号WebElement.getText();
		WebElement 项目名称WebElement = WebDriverUtils.findElement(driver, 项目名称Xpath, findElementTimeout);
		String 项目名称 = 项目名称WebElement.getText();

		WebElement 发展商WebElement = WebDriverUtils.findElement(driver, 发展商Xpath, findElementTimeout);
		String 发展商 = 发展商WebElement.getText();

		WebElement 所在位置WebElement = WebDriverUtils.findElement(driver, 所在位置Xpath, findElementTimeout);
		String 所在位置 = 所在位置WebElement.getText();

		WebElement 栋数WebElement = WebDriverUtils.findElement(driver, 栋数Xpath, findElementTimeout);
		String 栋数 = 栋数WebElement.getText();

		WebElement 地块编号WebElement = WebDriverUtils.findElement(driver, 地块编号Xpath, findElementTimeout);
		String 地块编号 = 地块编号WebElement.getText();

		WebElement 房产证编号WebElement = WebDriverUtils.findElement(driver, 房产证编号Xpath, findElementTimeout);
		String 房产证编号 = 房产证编号WebElement.getText();

		WebElement 批准面积WebElement = WebDriverUtils.findElement(driver, 批准面积Xpath, findElementTimeout);
		String 批准面积 = 批准面积WebElement.getText();

		WebElement 土地出让合同WebElement = WebDriverUtils.findElement(driver, 土地出让合同Xpath, findElementTimeout);
		String 土地出让合同 = 土地出让合同WebElement.getText();

		WebElement 批准日期WebElement = WebDriverUtils.findElement(driver, 批准日期Xpath, findElementTimeout);
		String 批准日期 = 批准日期WebElement.getText();

		WebElement 发证日期WebElement = WebDriverUtils.findElement(driver, 发证日期Xpath, findElementTimeout);
		String 发证日期 = 发证日期WebElement.getText();

		WebElement 备注WebElement = find备注Element(driver);
		String 备注 = 备注WebElement.getText();

		List<WebElement> 用途WebElements = find用途Element(driver);
		List<WebElement> 面积WebElements = find面积Element(driver);
		List<WebElement> 套数WebElements = find套数Element(driver);
		List<String> 许可证号list = new ArrayList<>();
		List<String> 项目名称list = new ArrayList<>();
		List<String> 发展商list = new ArrayList<>();
		List<String> 所在位置list = new ArrayList<>();
		List<String> 栋数list = new ArrayList<>();
		List<String> 地块编号list = new ArrayList<>();
		List<String> 房产证编号list = new ArrayList<>();
		List<String> 批准面积list = new ArrayList<>();
		List<String> 土地出让合同list = new ArrayList<>();
		List<String> 批准日期list = new ArrayList<>();
		List<String> 发证日期list = new ArrayList<>();
		List<String> 备注list = new ArrayList<>();
		List<String> 用途list = new ArrayList<>();
		List<String> 面积list = new ArrayList<>();
		List<String> 套数list = new ArrayList<>();
		for (int i = 0; i < 用途WebElements.size(); i++) {
			WebElement 用途WebElement = 用途WebElements.get(i);
			WebElement 面积WebElement = 面积WebElements.get(i);
			WebElement 套数WebElement = 套数WebElements.get(i);
			String 用途 = 用途WebElement.getText();
			String 面积 = 面积WebElement.getText();
			String 套数 = 套数WebElement.getText();
			许可证号list.add(许可证号);
			项目名称list.add(项目名称);
			发展商list.add(发展商);
			所在位置list.add(所在位置);
			栋数list.add(栋数);
			地块编号list.add(地块编号);
			房产证编号list.add(房产证编号);
			批准面积list.add(批准面积);
			土地出让合同list.add(土地出让合同);
			批准日期list.add(批准日期);
			发证日期list.add(发证日期);
			备注list.add(备注);
			用途list.add(用途);
			面积list.add(面积);
			套数list.add(套数);
		}
		doingPage.getMetaMap().put("presellId", 许可证号list);
		doingPage.getMetaMap().put("projectName", 项目名称list);
		doingPage.getMetaMap().put("developer", 发展商list);
		doingPage.getMetaMap().put("address", 所在位置list);
		doingPage.getMetaMap().put("buildingNum", 栋数list);
		doingPage.getMetaMap().put("landId", 地块编号list);

		doingPage.getMetaMap().put("housePropertyId", 房产证编号list);
		doingPage.getMetaMap().put("approveArea", 批准面积list);
		doingPage.getMetaMap().put("LandContract", 土地出让合同list);
		doingPage.getMetaMap().put("approveDate", 批准日期list);

		doingPage.getMetaMap().put("IssuingDate", 发证日期list);
		doingPage.getMetaMap().put("useage", 用途list);
		doingPage.getMetaMap().put("area", 面积list);
		doingPage.getMetaMap().put("houseNum", 套数list);
		doingPage.getMetaMap().put("remark", 备注list);

	
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {
		
	}

}
