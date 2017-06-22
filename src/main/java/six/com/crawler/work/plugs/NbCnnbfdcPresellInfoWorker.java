package six.com.crawler.work.plugs;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.exception.ProcessWorkerCrawlerException;

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
			throw new ProcessWorkerCrawlerException("tableCss isn't num:" + tableCss);
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
		
		Elements unitElements= element.select("tbody>tr:gt(1)");
		Elements elements = new Elements();
		for (int i = 0; i < unitElements.size(); i++) {
			Element item=unitElements.get(i);
			String s=StringUtils.trim(item.select("td").first().text()).replace("<br>", "").replace(" ", "");
			if(s.equals("非住宅幢号套数") || s.equals("住宅幢号套数")){
				continue;
			}else if(s.equals("住宅类")){
				break;
			}else{
				elements.addAll(item.select("td"));
			}
		}
		
		if(null!=elements){
			for(int i=0;i<elements.size();i++){
				if(i%2==0){
					String tx = StringUtils.trim(elements.get(i).select("div").text());
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
