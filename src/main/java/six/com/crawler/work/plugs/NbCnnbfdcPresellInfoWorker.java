package six.com.crawler.work.plugs;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

public class NbCnnbfdcPresellInfoWorker extends AbstractCrawlWorker {

	@Override
	protected void insideInit() {
	}

	@Override
	protected void beforeDown(Page doingPage) {
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		String tableCss = "td>table:eq(1)";
		Element element = doingPage.getDoc().select(tableCss).first();
		if(null==element){
			throw new RuntimeException("tableCss isn't num:" + tableCss);
		}
		String location = element.select("tbody>tr:eq(0)>td:eq(1)>div").text() +
				element.select("tbody>tr:eq(0)>td:eq(2)>div").text() + " " +
				element.select("tbody>tr:eq(0)>td:eq(3)>div").text() +
				element.select("tbody>tr:eq(0)>td:eq(4)>div").text() + " " +
				element.select("tbody>tr:eq(1)>td:eq(0)>div").text() +
				element.select("tbody>tr:eq(1)>td:eq(1)>div").text() + " " +
				element.select("tbody>tr:eq(1)>td:eq(2)>div").text() +
				element.select("tbody>tr:eq(1)>td:eq(3)>div").text();
		location = StringUtils.trim(location);
		String presellTotalArea = StringUtils.trim(element.select("tbody>tr:eq(0)>td:eq(6)>div").text().replace(" ", ""));
		String totalHouseNum = StringUtils.trim(element.select("tbody>tr:eq(0)>td:eq(8)>div").text().replace(" ", ""));
		String houseNo = "";
		String licenseReleaseTime = "";
		String capitalRegulationBank = "";
		Elements elements = element.select("tbody>tr:eq(3)>td");
		Element element_4 = element.select("tbody>tr:eq(4)>td:eq(0)").first();
		Elements elements2 = null;
		if(element_4!=null){
			String s = StringUtils.trim(element_4.select("div").text()).replace("<br>", "").replace(" ", "");
			if(!s.equals("非住宅幢号套数")){
				elements2 = element.select("tbody>tr:eq(6)>td");
				Elements elements4 = element.select("tbody>tr:eq(4)>td");
				if(elements2!=null){
					elements2.addAll(elements4);
				}
			}else{
				elements2 = element.select("tbody>tr:eq(5)>td");
			}
		}
		Elements ets = null;
		if(null!=elements){
			if(null!=elements2){
				elements.addAll(elements2);
			}
			ets = elements;
		}else if(null!=elements2){
			ets = elements2;
		}
		if(null!=ets){
			for(int i=0;i<ets.size();i++){
				if(i%2==0){
					String tx = StringUtils.trim(ets.get(i).select("div").text());
					tx = tx.replace(" ", "");
					if(StringUtils.isNotEmpty(tx)){
						houseNo = houseNo + tx + " ";
					}
				}
			}
		}
		String ctCss = "table[width='100%']>tbody>tr:eq(0)>td[align='left']:eq(0)";
		Element ele = doingPage.getDoc().select(ctCss).get(1);
		String str = ele.html();
		if(str!=null && str.contains("<br>")){
			String[] sts = str.split("<br>");
			if(sts!=null && sts.length>0){
				licenseReleaseTime = sts[0];
				licenseReleaseTime = licenseReleaseTime.substring(licenseReleaseTime.indexOf("发放时间为")+"发放时间为".length());
				licenseReleaseTime = StringUtils.trim(licenseReleaseTime);
				capitalRegulationBank = sts[2];
				capitalRegulationBank = capitalRegulationBank.substring(capitalRegulationBank.indexOf("监管银行为")+"监管银行为".length());
				capitalRegulationBank = StringUtils.trim(capitalRegulationBank);
				if(capitalRegulationBank.equals(".")){
					capitalRegulationBank = "";
				}
			}
		}
		doingPage.getMetaMap().computeIfAbsent("location",mapKey->new ArrayList<>()).add(location);
		doingPage.getMetaMap().computeIfAbsent("presellTotalArea",mapKey->new ArrayList<>()).add(presellTotalArea);
		doingPage.getMetaMap().computeIfAbsent("totalHouseNum",mapKey->new ArrayList<>()).add(totalHouseNum);
		doingPage.getMetaMap().computeIfAbsent("houseNo",mapKey->new ArrayList<>()).add(StringUtils.trim(houseNo));
		doingPage.getMetaMap().computeIfAbsent("licenseReleaseTime",mapKey->new ArrayList<>()).add(licenseReleaseTime);
		doingPage.getMetaMap().computeIfAbsent("capitalRegulationBank",mapKey->new ArrayList<>()).add(capitalRegulationBank);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
	
	
}
