package six.com.crawler.work;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.common.utils.JsoupUtils.TableResult;
import six.com.crawler.schedule.AbstractSchedulerManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月14日 下午3:16:39
 */
public class ShFangDiHouseInfoWorker extends HtmlCommonWorker {

	Map<String, String> fieldMap = new HashMap<String, String>();

	public ShFangDiHouseInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	public void onComplete(Page p) {
	}

	@Override
	public void insideOnError(Exception t, Page p) {

	}

	@Override
	protected void insideInit() {
		//            名义层/实际层
		fieldMap.put("名义层/实际层", "floor");
		fieldMap.put("室号", "houseID");
		fieldMap.put("房屋类型", "houseType");
		fieldMap.put("房型", "houseNature");
		fieldMap.put("预测建筑面积", "preBuildArea");
		fieldMap.put("预测套内面积", "preInnerArea");
		fieldMap.put("预测分摊面积", "preShareArea");
		fieldMap.put("预测地下面积", "preBelowArea");
		fieldMap.put("实测建筑面积", "bulidArea");
		fieldMap.put("实测套内面积", "innerArea");
		fieldMap.put("实测分摊面积", "shareArea");
		fieldMap.put("实测地下面积", "belowArea");
		fieldMap.put("状态", "status");
	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String tableXpath = "table[id=Table1]>tbody>tr>td>table";
		Document doc = doingPage.getDoc();
		if (null == doc) {
			String html = doingPage.getPageSrc();
			doc = Jsoup.parse(html);
		}
		Element tableElement = doc.select(tableXpath).first();
		if(null==tableElement){
			return;
		}
		List<TableResult> results = JsoupUtils.paserTable(tableElement);
		for (TableResult tableResult : results) {
			for (String key : fieldMap.keySet()) {
				if (tableResult.getKey().contains(key)) {
					String resultKey = fieldMap.get(key);
					doingPage.getMetaMap().computeIfAbsent(resultKey, mapKey -> new ArrayList<>())
							.add(tableResult.getValue());
				}
			}
		}

		List<String> projectNamelist = doingPage.getMeta("projectName");
		List<String> presalePremitlist = doingPage.getMeta("presalePermit");
		List<String> buildinglist = doingPage.getMeta("louDongName");
		String projectName = null;
		String presalePremit = null;
		String building = null;
		if (null != projectNamelist && !projectNamelist.isEmpty()) {
			projectName = projectNamelist.get(0);
		}
		doingPage.getMetaMap().put("projectName", Arrays.asList(projectName));
		if (null != presalePremitlist && !presalePremitlist.isEmpty()) {
			presalePremit = presalePremitlist.get(0);
		}
		doingPage.getMetaMap().put("presalePermit", Arrays.asList(presalePremit));
		if (null != buildinglist && !buildinglist.isEmpty()) {
			building = buildinglist.get(0);
		}
		doingPage.getMetaMap().put("louDongName", Arrays.asList(building));
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {

	}

}
