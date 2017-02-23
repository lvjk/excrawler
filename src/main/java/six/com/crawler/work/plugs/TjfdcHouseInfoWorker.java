package six.com.crawler.work.plugs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.common.utils.JsoupUtils.TableResult;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.WorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年1月17日 下午2:57:27
 */
public class TjfdcHouseInfoWorker extends AbstractCrawlWorker {

	private String tableCss = "div>table";
	private Map<String, String> fieldMap;

	public TjfdcHouseInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideInit() {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("楼号", "loudongNo");
		fieldMap.put("门号", "doorNo");
		fieldMap.put("户号", "houseNo");
		fieldMap.put("地址缩写", "address");
		fieldMap.put("所在区", "district");
		fieldMap.put("房屋地址", "houseAddress");
		fieldMap.put("总层数", "totalFloor");
		fieldMap.put("所在层", "floor");
		fieldMap.put("建筑面积", "bulidArea");
		fieldMap.put("套内面积", "innerArea");
		fieldMap.put("公摊面积", "shareArea");
		fieldMap.put("房型", "houseType");
		fieldMap.put("用途", "use");
		fieldMap.put("朝向", "orientations");
		fieldMap.put("建筑结构", "buildingStructure");
		fieldMap.put("参考价格", "referencePrice");
	}

	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		Element tableElement = doc.select(tableCss).first();
		List<TableResult> results = JsoupUtils.paserTable(tableElement);
		for (TableResult result : results) {
			for (String field : fieldMap.keySet()) {
				if (result.getKey().contains(field)) {
					String realField = fieldMap.get(field);
					doingPage.getMetaMap().put(realField, Arrays.asList(result.getValue()));
					break;
				}
			}
		}
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext result) {

	}

	@Override
	protected void onComplete(Page doingPage) {

	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {

	}

}
