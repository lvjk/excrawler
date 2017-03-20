package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.utils.JsoupUtils.TableResult;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月3日 上午9:19:32
 */
public class SzplGovSuiteDetailWorker extends AbstractCrawlWorker {

	private Map<String, String> fieldMap = new HashMap<String, String>();


	@Override
	protected void insideInit() {
		fieldMap.put("项目名称", "projectName");
		fieldMap.put("楼名", "floorName");
		fieldMap.put("constructionPlanPermit", "constructionPlanPermit");
		fieldMap.put("constructionPermit", "constructionPermit");
		fieldMap.put("座号", "floorNo");
		fieldMap.put("房号", "houseNo");
		fieldMap.put("项目楼栋情况", "projectFloor");
		fieldMap.put("户型", "houseType");
		fieldMap.put("合同号", "contractNo");
		fieldMap.put("拟售价格", "preSalePrice");
		fieldMap.put("楼层", "floor");
		fieldMap.put("用途", "useage");
		fieldMap.put("建筑面积_预售", "presalebuildArea");
		fieldMap.put("户内面积_预售", "presaleInnerArea");
		fieldMap.put("分摊面积_预售", "presaleShareArea");
		fieldMap.put("建筑面积_竣工", "buildArea");
		fieldMap.put("户内面积_竣工", "InnerArea");
		fieldMap.put("分摊面积_竣工", "shareArea");
	}

	@Override
	protected void beforeDown(Page doingPage) {

	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String html = doingPage.getPageSrc();
		Document document = Jsoup.parse(html);
		Elements temp = document.select("tr[class=a1]");
		Element table = temp.first().parent();
		List<TableResult> results = JsoupUtils.paserTable(table);
		int index = 0;
		int count = 0;
		List<String> list = null;
		for (TableResult tableResult : results) {
			list = new ArrayList<>();
			if ("建筑面积".equals(tableResult.getKey())) {
				if (index == 0) {
					tableResult.setKey("建筑面积_预售");
					count++;
				} else {
					tableResult.setKey("建筑面积_竣工");
				}
			} else if ("户内面积".equals(tableResult.getKey())) {
				if (index == 0) {
					tableResult.setKey("户内面积_预售");
					count++;
				} else {
					tableResult.setKey("户内面积_竣工");
				}
			} else if ("分摊面积".equals(tableResult.getKey())) {
				if (index == 0) {
					tableResult.setKey("分摊面积_预售");
					count++;
				} else {
					tableResult.setKey("分摊面积_竣工");
				}
			}
			if (3 == count) {
				index++;
			}
			list.add(tableResult.getValue());
			String mapKey = fieldMap.get(tableResult.getKey());
			if (StringUtils.isBlank(mapKey)) {
				mapKey = tableResult.getKey();
			}
			doingPage.getMetaMap().put(mapKey, list);
		}

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
