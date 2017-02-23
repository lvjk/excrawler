package six.com.crawler.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import six.com.crawler.common.dao.PathDao;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.service.PaserPathService;
import six.com.crawler.work.extract.ExtractPath;
import six.com.crawler.work.extract.PathType;

/**
 * @author six
 * @date 2016年8月18日 上午10:19:11 解析规则服务器
 */
@Component
public class PaserPathServiceImpl implements PaserPathService {

	final static Logger LOG = LoggerFactory.getLogger(PaserPathServiceImpl.class);
	@Autowired
	private PathDao pathdao;

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
	public ExtractPath queryPath(String siteCode, String name, int ranking) {
		// long start=System.currentTimeMillis();
		// System.out.println("queryPath start time:"+start);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("siteCode", siteCode);
		parameters.put("name", name);
		parameters.put("ranking", ranking);
		List<ExtractPath> result = pathdao.query(parameters);
		ExtractPath optimalPath = null;
		if (null != result && result.size() > 0) {
			optimalPath = result.get(0);
		}
		// long end=System.currentTimeMillis();
		// System.out.println("queryPath end time:"+end);
		// System.out.println("queryPath time:"+(end-start));
		return optimalPath;
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
		pathdao.save(path);
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

	public PathDao getPathdao() {
		return pathdao;
	}

	public void setPathdao(PathDao pathdao) {
		this.pathdao = pathdao;
	}
}
