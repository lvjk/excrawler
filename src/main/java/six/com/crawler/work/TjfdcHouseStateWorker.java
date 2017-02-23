package six.com.crawler.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.http.HttpMethod;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.schedule.AbstractSchedulerManager;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年1月17日 下午2:57:10 
*/
public class TjfdcHouseStateWorker extends HtmlCommonWorker {

	RedisWorkQueue tjfdcHouseInfoQueue;
	String houseCss="table[id=LouDongInfo1_dgData]>tbody>tr>td:gt(0)";
	Map<String,String> stateMap;
	
	public TjfdcHouseStateWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue stored) {
		super(name, manager, job, site, stored);
	}

	@Override
	protected void insideOnError(Exception t, Page doingPage) {
		
	}

	@Override
	protected void insideInit() {
		tjfdcHouseInfoQueue = new RedisWorkQueue(getManager().getRedisManager(),
				"tjfdc_house_info");
		stateMap=new HashMap<>();
		stateMap.put("background-color:#ff5e59", "已售");
		stateMap.put("background-color:#aade9c", "未售");
		stateMap.put("background-color:#fff28c", "预定");
		stateMap.put("background-color:Gray", "其它不可售房");
		stateMap.put("background-color:#a0a0a0", "其它不可售房");

	}

	@Override
	protected void beforePaser(Page doingPage) throws Exception {
		String html = doingPage.getPageSrc();
		Document doc = Jsoup.parse(html);
		Elements houseTdElements=doc.select(houseCss);
		//List<String> projectNameList=doingPage.getMetaMap().get("projectName");
		//List<String> loudongNameList=doingPage.getMetaMap().get("loudongName");
		//List<String> loudongNoList=doingPage.getMetaMap().get("loudongNo");
		//List<String> presalePermitList=doingPage.getMetaMap().get("presalePermit");
		//List<String> doorNoList=doingPage.getMetaMap().get("doorNo");
		List<String> houseNoList = new ArrayList<>();
		List<String> houseStateList = new ArrayList<String>();
		for(int i=0;i<houseTdElements.size();i++){
			Element houseTdElement=houseTdElements.get(i);
			String houseNo=houseTdElement.text();
			if(StringUtils.isNotBlank(StringUtils.trim(houseNo))){
				String backGround=houseTdElement.attr("style");
				String houseState=null;
				for(String flagKey:stateMap.keySet()){
					if(StringUtils.contains(backGround, flagKey)){
						houseState=stateMap.get(flagKey);
						break;
					}
				}
				if(null==houseState){
					throw new RuntimeException("unkonw house's state["+backGround+"]:"+doingPage.getFinalUrl());
				}
				houseNoList.add(houseNo);
				houseStateList.add(houseState);
				Element houseAElement =houseTdElement.select("a").first();
				if(null!=houseAElement){
					String houseInfoUrl=houseAElement.attr("onclick");
					houseInfoUrl=StringUtils.substringBetween(houseInfoUrl, "window.showModalDialog(\"", "\",window,\"");
					houseInfoUrl = UrlUtils.paserUrl(doingPage.getBaseUrl(), doingPage.getFinalUrl(), houseInfoUrl);
					Page houseInfoPage = new Page(doingPage.getSiteCode(), 1, houseInfoUrl, houseInfoUrl);
					houseInfoPage.setMethod(HttpMethod.GET);
					houseInfoPage.setReferer(doingPage.getFinalUrl());
					houseInfoPage.setType(PageType.DATA.value());
					if(!tjfdcHouseInfoQueue.duplicateKey(houseInfoPage.getPageKey())){
						houseInfoPage.getMetaMap().putAll(doingPage.getMetaMap());
						tjfdcHouseInfoQueue.push(houseInfoPage);
					}
				}
			}
		}
		doingPage.getMetaMap().put("houseNo", houseNoList);
		doingPage.getMetaMap().put("status", houseStateList);
	}

	@Override
	protected void afterPaser(Page doingPage) throws Exception {
		
	}

	@Override
	protected void onComplete(Page doingPage) {
		
	}

}
