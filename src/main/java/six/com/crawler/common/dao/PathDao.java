package six.com.crawler.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import six.com.crawler.common.dao.provider.PathDaoProvider;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午9:30:12
 */
public interface PathDao extends BaseDao{

	@SelectProvider(type = PathDaoProvider.class, method = "query")
	public List<ExtractPath> query(Map<String,Object> parameters);

	public int save(ExtractPath t);

	public int delete(String id);

	public int update(ExtractPath t);

}
