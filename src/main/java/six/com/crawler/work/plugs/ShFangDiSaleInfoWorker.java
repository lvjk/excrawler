package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.JsoupUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.HtmlCommonWorker;
import six.com.crawler.work.RedisWorkQueue;
import six.com.crawler.work.WorkQueue;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年12月9日 下午5:32:28 
*/
public class ShFangDiSaleInfoWorker extends HtmlCommonWorker{
	
	RedisWorkQueue buildingInfoQueue;
	Map<String, String> fieldMap = new HashMap<String, String>();

	public ShFangDiSaleInfoWorker(String name, AbstractSchedulerManager manager, Job job, Site site,
			WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	public void onComplete(Page p) {
		List<String> projectNamelist = p.getResultContext().getResult("projectName");
		List<String> presalePremitList = p.getResultContext().getResult("presalePermit");
		List<String> louDongNameList = p.getResultContext().getResult("louDongName");
		List<String> saleInfoUrlList = p.getResultContext().getResult("楼栋信息url_4");
		String saleInfoUrl=null;
		String louDongName=null;
		List<String> tempLouDongNameList=null;
		for(int i=0;i<saleInfoUrlList.size();i++){
			saleInfoUrl=saleInfoUrlList.get(i);
			louDongName=louDongNameList.get(i);
			tempLouDongNameList=new ArrayList<>();
			tempLouDongNameList.add(louDongName);
			Page preSaleInfoPage = new Page(p.getSiteCode(), 1, saleInfoUrl, saleInfoUrl);
			preSaleInfoPage.setType(PageType.DATA.value());
			preSaleInfoPage.getMetaMap().put("projectName", projectNamelist);
			preSaleInfoPage.getMetaMap().put("presalePermit", presalePremitList);
			preSaleInfoPage.getMetaMap().put("louDongName", tempLouDongNameList);
			preSaleInfoPage.setReferer(p.getFinalUrl());
			buildingInfoQueue.push(preSaleInfoPage);
		}
	}

	@Override
	public void insideOnError(Exception t, Page p) {
		
	}


	@Override
	protected void insideInit() {
		buildingInfoQueue = new RedisWorkQueue(getManager().getRedisManager(),
				"sh_fangdi_building_info");
		
		fieldMap.put("楼栋名称", "louDongName");
		fieldMap.put("最高报价/最低报价", "refPrice");
		fieldMap.put("参考价", "refPrice");
		fieldMap.put("报价可浮动幅度", "floateRnge");
		fieldMap.put("可浮动幅度", "floateRnge");
		fieldMap.put("总套数", "total");
		fieldMap.put("总面积", "totalArea");
		fieldMap.put("销售状态", "status");
	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String tableXpath = "table>tbody>tr>td>table>tbody>tr>td>table";
		String headCssSelect="table>tbody>tr:eq(0)>td";
		String dataCssSelect="table>tbody>tr[class=indextabletxt]";
		Document doc = doingPage.getDoc();
		if (null == doc) {
			String html = doingPage.getPageSrc();
			doc = Jsoup.parse(html);
		}
		Element tableElement = doc.select(tableXpath).first();
		if(null!=tableElement){
			Map<String,List<String>> resultMap=JsoupUtils.paserTable(tableElement, headCssSelect, dataCssSelect);
			for(String field:resultMap.keySet()){
				for (String key : fieldMap.keySet()) {
					if (field.contains(key)) {
						String resultKey = fieldMap.get(key);
						doingPage.getMetaMap().put(resultKey, resultMap.get(field));
					}
				}
			}
		}else{
			throw new RuntimeException("this page don't find table:"+tableXpath);
		}
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {
		List<String> projectNamelist = doingPage.getResultContext().getResult("projectName");
		List<String> presalePremitlist = doingPage.getResultContext().getResult("presalePermit");
		String projectName=null;
		String presalePremit=null;
		if(null!=projectNamelist&&!projectNamelist.isEmpty()){
			projectName=projectNamelist.get(0);
		} 
		if(null!=presalePremitlist&&!presalePremitlist.isEmpty()){
			presalePremit=presalePremitlist.get(0);
		} 
		List<String> loudongNameList =doingPage.getResultContext().getResult("louDongName");
		if(null!=loudongNameList){
			projectNamelist=new ArrayList<>(loudongNameList.size());
			presalePremitlist=new ArrayList<>(loudongNameList.size());
			for(int i=0;i<loudongNameList.size();i++){
				projectNamelist.add(projectName);
				presalePremitlist.add(presalePremit);
			}
			doingPage.getResultContext().addResult("projectName", projectNamelist);
			doingPage.getResultContext().addResult("presalePermit", presalePremitlist);
		}
	}

}
