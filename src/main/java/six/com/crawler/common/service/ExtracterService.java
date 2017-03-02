package six.com.crawler.common.service;

import java.util.List;

import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午10:24:19
 */
public interface ExtracterService {

	/**
	 * 查询 抽取path
	 * 
	 * @param path
	 * @return
	 */
	public List<ExtractPath> query(String siteCode);
	
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
	 * 测试从html中提取 内容
	 * 
	 * @param path
	 * @param html
	 * @return
	 */
	public List<String> testExtract(ExtractPath extractPath, String testHtml,String testUrl);


	/**
	 * 添加 抽取path
	 * 
	 * @param path
	 * @return
	 */
	public void saveExtractPath(List<ExtractPath> path);

	/**
	 * 更新 抽取path
	 * 
	 * @param path
	 * @return
	 */
	public void updateExtractPath(ExtractPath path);

	public void delExtractPathBySiteCide(String siteCode);


	public void delExtractPathByName(String name);
}
