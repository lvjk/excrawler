package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.RedisManager;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.ThreadUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.Constants;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月10日 下午5:20:38
 */
public class QichachaSearchWorker extends AbstractCrawlWorker {

	protected final static Logger LOG = LoggerFactory.getLogger(QichachaSearchWorker.class);
	RedisWorkQueue qichachaQueue;
	private RedisManager redisManager;
	private String showProvinceBt;
	private String clearProvinceBt;
	private String provinceList;
	private String clearCreateDate;
	private String showCreateDate;
	private String createDateList;
	private String clearCity;
	private String cityList;
	// 正在处理的省份
	private String doingProvince;
	// 正在处理的创建日期
	private String doingCreateDate;
	// 正在处理的城市
	private String doingCity;
	private String nextPage;
	Map<String, Province> map = new HashMap<>();
	private boolean hasNextPage;
	private boolean selectPreItem = false;

	public QichachaSearchWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
		redisManager = manager.getRedisManager();
		showProvinceBt = "//a[@id='show-province']";

		clearProvinceBt = "//span[@class='m-l label btn-primary provinceChoosen appendSpan clearSpan']";
		provinceList = "//dl[@id='provinceOld']/div[2]/dd";
		clearCreateDate = "//span[@class='m-l label btn-primary startDateChoosen appendSpan clearSpan']";
		showCreateDate = "//a[@id='show-date']";
		createDateList = "//dl[@id='startdateOld']/div[2]/dd";
		clearCity = "//span[@class='m-l label btn-primary cityChoosen appendSpan clearSpan']";
		cityList = "//div[@id='city_show']/dd/a";
		nextPage = "//a[@id='ajaxpage' and text()='>']";
	}

	@Override
	public void insideInit() {
		qichachaQueue = new RedisWorkQueue(getManager().getRedisManager(), "qichacha");
	}

	@Override
	public void onComplete(Page doingPage,ResultContext resultContext) {
		getWorkQueue().push(doingPage);
	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		// WebDriver webDriver = doingPage.getWebDriver();
		// 处理省份选项
		String isDoingProvince = null;
		// 处理创建日期选项
		String isDoingCreateDate = null;
		// 处理城市选项
		String isDoingCity = null;
		int retryCount = 3;
		if (!selectPreItem) {
			String searchInputXpath = "//input[@id='headerKey']";
			WebElement searchInputElement = getDowner().findWebElement(searchInputXpath);
			if (null != searchInputElement) {
				String value = searchInputElement.getAttribute("value");
				if (StringUtils.isBlank(value)) {
					searchInputElement.sendKeys("地产");
				}
				String btXpath = "//form[@id='tpsearch']/div/div/span/button";
				WebElement btElement = getDowner().findWebElement(btXpath);
				getDowner().click(btElement, btXpath);
			}
			String businessScopeXpath = "//div[@id='SearchBox']/dl/div/dd/a[@data-append='经营范围']";
			WebElement businessScopeElement = getDowner().findWebElement(businessScopeXpath);
			if (null != businessScopeElement) {
				getDowner().click(businessScopeElement, businessScopeXpath);
			}
			String industryXpath = "//div[@id='SearchBox']/dl/div/dd/a[@data-append='房地产业']";
			WebElement industryElement = getDowner().findWebElement(industryXpath);
			if (null != industryElement) {
				getDowner().click(industryElement, industryXpath);
			}
			selectPreItem = true;
		}
		String openSearchDivXpath = "//a[@id='hideSearchBox']";
		WebElement openSearchDivElement = getDowner().findWebElement(openSearchDivXpath);
		if (null != openSearchDivElement) {
			String html = openSearchDivElement.getText();
			if (StringUtils.contains(html, "展开")) {
				getDowner().click(openSearchDivElement, openSearchDivXpath);
			}
		}

		while (retryCount > 0) {
			retryCount--;
			try {
				// 处理省份选项
				isDoingProvince = selectProvince();
				break;
			} catch (StaleElementReferenceException e) {
				this.doingProvince = null;
				ThreadUtils.sleep(1000);
				if (retryCount <= 0) {
					throw new RuntimeException(e);
				}
			}
		}
		retryCount = 3;
		while (true) {
			retryCount--;
			try {
				// 处理创建日期选项
				isDoingCreateDate = selectCreateDate(isDoingProvince);
				break;
			} catch (StaleElementReferenceException e) {
				this.doingCreateDate = null;
				ThreadUtils.sleep(1000);
				if (retryCount <= 0) {
					throw new RuntimeException(e);
				}
			}
		}
		retryCount = 3;
		while (true) {
			retryCount--;
			try {
				// 处理 城市
				isDoingCity = selectCity(isDoingProvince, isDoingCreateDate);
				break;
			} catch (StaleElementReferenceException e) {
				this.doingCity = null;
				ThreadUtils.sleep(1000);
				if (retryCount <= 0) {
					throw new RuntimeException(e);
				}
			}
		}
		List<String> list = new ArrayList<String>();
		if (null != isDoingCity) {
			list.add(isDoingCity);
		} else {
			list.add(isDoingProvince);
		}
		doingPage.getMetaMap().put("city", list);
		doingPage.setPageSrc(getDowner().getWebDriver().getPageSource());
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
		List<String> dataUrlList = resultContext.getExtractResult("companyInfoUrl");
		List<String> list = resultContext.getExtractResult("city");
		if (null == dataUrlList || dataUrlList.isEmpty()) {
			hasNextPage = false;
		} else {
			Page newPage = null;
			for (String tempUrl : dataUrlList) {
				tempUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), tempUrl);
				newPage = new Page(doingPage.getSiteCode(), 1, tempUrl, tempUrl);
				newPage.setReferer(doingPage.getFinalUrl());
				newPage.setType(PageType.DATA.value());
				newPage.getMetaMap().put("city", list);
				if (!qichachaQueue.duplicateKey(newPage.getPageKey())) {
					qichachaQueue.push(newPage);
				}
			}
			WebElement nextElement = getDowner().findWebElement(nextPage);
			if (null != nextElement) {
				getDowner().click(nextElement, nextPage);
				hasNextPage = true;
			} else {
				hasNextPage = false;
			}
		}
	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

	private String selectProvince() {
		// 如果正在处理省份不为空
		if (null != doingProvince) {
			// 判断是否处理完 如果处理完那么 需要清除这个省份条件
			if (isFinishProvince(doingProvince)) {
				map.remove(doingProvince);
				WebElement clearProvinceBtElement = getDowner().findWebElement(clearProvinceBt);
				if (null != clearProvinceBtElement) {
					getDowner().click(clearProvinceBtElement, clearProvinceBt);
				}
				redisManager.rpush(Constants.REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY, doingProvince);
				doingProvince = doProvince();
			}
		} else {
			doingProvince = doProvince();
		}
		return doingProvince;
	}

	private String doProvince() {
		// 获取显示省份bt
		String isDoingProvince = null;
		WebElement showProvinceBtElement = getDowner().findWebElement(showProvinceBt);
		if (null != showProvinceBtElement) {
			getDowner().click(showProvinceBtElement, showProvinceBt);
		}
		// 获取省份列表
		List<WebElement> provinceElements = getDowner().findWebElements(provinceList);
		WebElement readyDoProvinceElement = null;
		WebElement tempProvinceElement = null;
		String province = null;
		if (null != provinceElements) {
			for (int i = 0; i < provinceElements.size(); i++) {
				tempProvinceElement = provinceElements.get(i);
				province = getProvinceText(tempProvinceElement);
				if (!isDoneProvice(province)) {
					if (!map.containsKey(province)) {
						map.put(province, new Province(province));
					}

					if (null == readyDoProvinceElement) {
						readyDoProvinceElement = tempProvinceElement;
						isDoingProvince = province;
					}
				}

			}
		}
		if (null != readyDoProvinceElement) {
			getDowner().click(readyDoProvinceElement, null);
		}
		return isDoingProvince;
	}

	private String selectCreateDate(String isDoingProvince) {
		// 如果正在处理省份不为空
		if (null != doingCreateDate) {
			// 判断是否处理完 如果处理完那么 需要清除这个省份条件
			if (isFinishDate(isDoingProvince, doingCreateDate)) {
				Province provinceVo = map.get(isDoingProvince);
				provinceVo.dates.remove(doingCreateDate);
				WebElement clearCreateDateBtElement = getDowner().findWebElement(clearCreateDate);
				if (null != clearCreateDateBtElement) {
					getDowner().click(clearCreateDateBtElement, clearCreateDate);
				}
				redisManager.rpush(Constants.REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY + "_" + isDoingProvince,
						doingCreateDate);
				doingCreateDate = doCreateDate(isDoingProvince);
			}
		} else {
			doingCreateDate = doCreateDate(isDoingProvince);
		}
		return doingCreateDate;

	}

	private String doCreateDate(String isDoingProvince) {
		Province provinceVo = map.get(isDoingProvince);
		String isDoingCreateDate = null;
		// 获取显示创建日期bt
		WebElement showCreateDateBtElement = getDowner().findWebElement(showCreateDate);
		if (null != showCreateDateBtElement) {
			getDowner().click(showCreateDateBtElement, showCreateDate);
		}
		// 获取创建日期列表
		List<WebElement> createDateElements = getDowner().findWebElements(createDateList);
		WebElement readyDoCreateDateElement = null;
		WebElement tempCreateDateElement = null;
		String createDate = null;
		if (null != createDateElements && createDateElements.size() > 0) {
			provinceVo.dates = new HashMap<>();
			for (int i = 0; i < createDateElements.size(); i++) {
				tempCreateDateElement = createDateElements.get(i);
				createDate = getCreateDateText(tempCreateDateElement);
				if (!isDoneCreateDate(isDoingProvince, createDate)) {
					if (!provinceVo.dates.containsKey(createDate)) {
						provinceVo.dates.put(createDate, new CreateDate(createDate));
					}
					if (null == readyDoCreateDateElement) {
						readyDoCreateDateElement = tempCreateDateElement;
						isDoingCreateDate = createDate;
					}
				}
			}
		}
		if (null != readyDoCreateDateElement) {
			getDowner().click(readyDoCreateDateElement, null);
		}
		return isDoingCreateDate;

	}

	private String selectCity(String isDoingProvince, String isDoingCreateDate) {
		// 如果正在处理省份不为空
		if (null != doingCity) {
			// 判断是否处理完 如果处理完那么 需要清除这个省份条件
			if (isFinishCity(isDoingProvince, isDoingCreateDate, doingCity)) {
				Province provinceVo = map.get(isDoingProvince);
				CreateDate createDateVo = provinceVo.dates.get(doingCreateDate);
				createDateVo.citys.remove(doingCity);
				WebElement clearCityBtElement = getDowner().findWebElement(clearCity);
				if (null != clearCityBtElement) {
					getDowner().click(clearCityBtElement, clearCity);
				}
				redisManager.rpush(Constants.REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY + "_" + isDoingProvince + "_"
						+ isDoingCreateDate, doingCity);
				doingCity = doCity(isDoingProvince, isDoingCreateDate);
			}
		} else {
			doingCity = doCity(isDoingProvince, isDoingCreateDate);
		}
		return doingCity;
	}

	private String doCity(String isDoingProvince, String isDoingCreateDate) {
		String isDoingCity = null;
		Province provinceVo = map.get(isDoingProvince);
		CreateDate createDateVo = provinceVo.dates.get(isDoingCreateDate);
		List<WebElement> cityElements = getDowner().findWebElements(cityList);
		WebElement readyDoCityElement = null;
		WebElement tempCityElement = null;
		String city = null;
		if (null != cityElements && cityElements.size() > 0) {
			createDateVo.citys = new HashMap<>();
			for (int i = 0; i < cityElements.size(); i++) {
				tempCityElement = cityElements.get(i);
				city = getCityText(tempCityElement);
				if (!isDoneCity(isDoingProvince, isDoingCreateDate, city)) {
					if (!createDateVo.citys.containsKey(city)) {
						createDateVo.citys.put(city, new City(city));
					}
					if (null == readyDoCityElement) {
						readyDoCityElement = tempCityElement;
						isDoingCity = city;
					}
				}
			}
		}
		if (null != readyDoCityElement) {
			getDowner().click(readyDoCityElement, null);
		} else {
			if (null != createDateVo.citys && !createDateVo.citys.isEmpty()) {
				createDateVo.citys.clear();
			}
		}
		return isDoingCity;
	}

	/**
	 * 获取省份
	 * 
	 * @return
	 */
	private String getProvinceText(WebElement provinceElement) {
		String province = "";
		if (null != provinceElement) {
			province = provinceElement.getText();
			province = subText(province);
		}
		return province;
	}

	/**
	 * 获取创建日期
	 * 
	 * @param province
	 * @return
	 */
	private String getCreateDateText(WebElement CreateDateElement) {
		String createDate = "";
		if (null != CreateDateElement) {
			createDate = CreateDateElement.getText();
			createDate = subText(createDate);
		}
		return createDate;
	}

	/**
	 * 获取city
	 * 
	 * @param province
	 * @param createDate
	 * @return
	 */
	private String getCityText(WebElement cityElement) {
		String city = "";
		if (null != cityElement) {
			city = cityElement.getText();
			city = subText(city);
		}
		return city;
	}

	/**
	 * 1
	 * 
	 * @param province
	 * @return
	 */
	private boolean isDoneProvice(String province) {
		List<String> doneProvinceList = redisManager.lrange(Constants.REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY, 0,
				-1, String.class);
		boolean isDone = false;
		for (String doneProvince : doneProvinceList) {
			if (doneProvince.contains(province) || doneProvince.contains(province)) {
				isDone = true;
			}
		}
		return isDone;
	}

	/**
	 * 1
	 * 
	 * @param province
	 * @return
	 */
	private boolean isDoneCreateDate(String province, String createDate) {
		List<String> doneProvinceList = redisManager
				.lrange(Constants.REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY + "_" + province, 0, -1, String.class);
		boolean isDone = false;
		for (String doneProvince : doneProvinceList) {
			if (doneProvince.contains(createDate) || doneProvince.contains(createDate)) {
				isDone = true;
			}
		}
		return isDone;
	}

	private boolean isDoneCity(String province, String createDate, String city) {
		List<String> doneProvinceList = redisManager.lrange(
				Constants.REDIS_PROCESSOR_OPERATION_DONE_PROVINCE_KEY + "_" + province + "_" + createDate, 0, -1,
				String.class);
		boolean isDone = false;
		for (String doneProvince : doneProvinceList) {
			if (doneProvince.contains(city) || doneProvince.contains(city)) {
				isDone = true;
			}
		}
		return isDone;
	}

	private boolean isFinishCity(String province, String createDate, String city) {
		return !hasNextPage;
	}

	private boolean isFinishDate(String province, String createDate) {
		Province provinceVo = map.get(province);
		CreateDate createDateVo = provinceVo.dates.get(createDate);
		if (createDateVo.citys != null) {
			if (createDateVo.citys.size() > 0) {
				return false;
			}
			if (createDateVo.citys.size() == 0) {
				return true;
			}
		}
		return !hasNextPage;
	}

	private boolean isFinishProvince(String province) {
		Province provinceVo = map.get(province);
		if (provinceVo.dates.size() > 0) {
			return false;
		}
		return !hasNextPage;
	}

	private static String subText(String text) {
		String[] startChars = new String[] { "(", "（" };
		String[] endChars = new String[] { ")", "）" };
		StringBuilder textSbd = new StringBuilder(text);
		int start = -1;
		for (String startChar : startChars) {
			int index = text.indexOf(startChar);
			if (index != -1) {
				start = index;
			}
		}
		int end = -1;
		for (String endChar : endChars) {
			int index = text.indexOf(endChar);
			if (index != -1) {
				end = index;
			}
		}
		String result = text;
		if (-1 != start && -1 != end) {
			result = textSbd.delete(start, end + 1).toString();
		}
		return StringUtils.trim(result);
	}

	class City {
		String city;

		City(String city) {
			this.city = city;
		}
	}

	class CreateDate {
		String date;
		Map<String, City> citys;

		CreateDate(String date) {
			this.date = date;
		}
	}

	class Province {
		String province;
		Map<String, CreateDate> dates;

		Province(String province) {
			this.province = province;
		}
	}

}
