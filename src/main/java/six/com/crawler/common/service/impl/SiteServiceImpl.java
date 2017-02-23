package six.com.crawler.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import six.com.crawler.common.dao.SiteDao;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.service.SiteService;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月9日 下午1:15:09 
*/
@Service
public class SiteServiceImpl implements SiteService{

	@Autowired
	private SiteDao siteDao;
	
	@Override
	public Site query(String siteCode) {
		Site result=siteDao.query(siteCode);
		return result;
	}
	
	
	public SiteDao getSiteDao() {
		return siteDao;
	}

	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

}
