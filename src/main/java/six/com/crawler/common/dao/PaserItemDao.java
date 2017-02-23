package six.com.crawler.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Select;

import six.com.crawler.common.dao.provider.PaserComponentDaoProvider;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月13日 上午11:02:19
 */
public interface PaserItemDao extends BaseDao{

	@Select("select jobName,name,type,paserClassName,resultKey,mustHaveResult,depth,pageType,"
			+ "output,`describe` from ex_crawler_platform_paser_component where jobName=#{jobName} order by serialNub asc")
	public List<ExtractItem> query(String jobName);

	@InsertProvider(type = PaserComponentDaoProvider.class, method = "save")
	public int save(List<ExtractItem> paserResults);
}
