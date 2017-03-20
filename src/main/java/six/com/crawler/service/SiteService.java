package six.com.crawler.service;

import java.util.List;

import six.com.crawler.entity.Site;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午1:15:01
 */
public interface SiteService extends DownloadAndUploadService{

	
	/**
	 * 通过siteCode 查询site
	 * 
	 * @param siteCode
	 * @return
	 */
	public List<Site> querySites(int pageIndex,int pageSize);
	
	/**
	 * 通过siteCode 查询site
	 * 
	 * @param siteCode
	 * @return
	 */
	public Site querySite(String siteCode);
	
	
	/**
	 * 保存site
	 * @param siteCode
	 * @return
	 */
	public void save(Site site);
	
	
	/**
	 * del site
	 * @param siteCode
	 * @return
	 */
	public void del(String siteCode);

	
	/**
	 * 通过siteCode 获取该site的 抽取path
	 * @param siteCode
	 * @return
	 */
	public List<ExtractPath> queryExtractPathBySiteCode(String siteCode);
	
	
	/**
	 * 通过抽取path name 获取 抽取path
	 * @param siteCode
	 * @return
	 */
	public List<ExtractPath> queryExtractPathByName(String name);

	
}
