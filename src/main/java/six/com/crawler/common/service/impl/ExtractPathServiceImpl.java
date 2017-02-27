package six.com.crawler.common.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.dao.ExtractPathDao;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.service.ExtractPathService;
import six.com.crawler.work.extract.ExtractPath;
import six.com.crawler.work.extract.PathType;

/**
 * @author six
 * @date 2016年8月18日 上午10:19:11 解析规则服务器
 */
@Component
public class ExtractPathServiceImpl implements ExtractPathService {

	final static Logger LOG = LoggerFactory.getLogger(ExtractPathServiceImpl.class);
	@Autowired
	private ExtractPathDao pathdao;

	Map<String, Map<String, List<ExtractPath>>> maps = new HashMap<String, Map<String, List<ExtractPath>>>();

	public List<ExtractPath> getPaserPath(String siteCode, PathType pathType, Page page) {
		Map<String, List<ExtractPath>> map = maps.get(siteCode);
		List<ExtractPath> result = null;
		String key = pathType.toString() + page.getDepth();
		if (null != map) {
			result = map.get(key);
		}
		return result;
	}

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
	public List<ExtractPath> query(String pathName,String siteCode){
		List<ExtractPath> result = pathdao.query(pathName,siteCode);
		return result;
	}

	/**
	 * 更新解析path
	 * 
	 * @param siteCode
	 * @param path
	 */
	public void updatePaserPath(ExtractPath path) {
	}

	/**
	 * 添加指定站点path
	 * 
	 * @param siteCode
	 * @param path
	 */
	public void addPaserPath(ExtractPath path) {
		pathdao.batchSave(Arrays.asList(path));
	}

	/**
	 * 批量添加指定站点paths
	 * 
	 * @param siteCode
	 * @param paths
	 */
	public void addPaserPaths(List<ExtractPath> paths) {
		if (null != paths) {
			for (ExtractPath path : paths) {
				addPaserPath(path);
			}
		}
	}

	public ExtractPathDao getPathdao() {
		return pathdao;
	}

	public void setPathdao(ExtractPathDao pathdao) {
		this.pathdao = pathdao;
	}
}
