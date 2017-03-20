package six.com.crawler.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.PageType;
import six.com.crawler.work.downer.DownerType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午1:01:36
 */
public class PageDaoTest extends BaseTest {

	@Test
	public void test() {
		//buildPage("tjfdc");
	}

	public void buildPage(String siteCode) {
		List<Page> seedPages = new ArrayList<Page>();
		String url = "http://www.tjfdc.com.cn/pages/fcdt/fcdtlist.aspx?SelMnu=FCSJ_XMXX&KPZT=&strKPZT=&QY=&XZQH=&strXZQH=&BK=&XMMC=&e=0.5137118289336824";
		Page page = new Page(siteCode, 1, url, url);
		page.setDownerType(DownerType.CHROME.value());// 设置此页面下载类型
		page.setType(PageType.LISTING.value());
		seedPages.add(page);
		pageDao.save(seedPages);
	}
	
	public void query(String siteCode) {
		
	}
}
