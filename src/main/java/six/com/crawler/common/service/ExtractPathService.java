package six.com.crawler.common.service;

import java.util.List;

import six.com.crawler.common.entity.Page;
import six.com.crawler.work.extract.ExtractPath;
import six.com.crawler.work.extract.PathType;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午10:24:19
 */
public interface ExtractPathService {

	public List<ExtractPath> getPaserPath(String siteCode, PathType pathType, Page page);

	/**
	 * 通过siteCode pathType ranking 获取path
	 * 
	 * @param siteCode
	 *            网站code
	 * @param pathType
	 *            path类型
	 * @param ranking
	 *            排名
	 * @return
	 */
	public List<ExtractPath> query(String pathName,String siteCode);

	/**
	 * 更新解析path
	 * 
	 * @param siteCode
	 * @param path
	 */
	public void updatePaserPath(ExtractPath path);

	/**
	 * 添加指定站点path
	 * 
	 * @param siteCode
	 * @param path
	 */
	public void addPaserPath(ExtractPath path);

	/**
	 * 批量添加指定站点paths
	 * 
	 * @param siteCode
	 * @param paths
	 */
	public void addPaserPaths(List<ExtractPath> paths);

}
