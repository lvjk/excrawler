package six.com.crawler.dao.provider;

import java.util.List;
import java.util.Map;

import six.com.crawler.dao.BaseDao;
import six.com.crawler.dao.ExtractItemDao;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月13日 下午12:05:10
 */
public class ExtractItemDaoProvider extends BaseProvider{

	private String saveColumns="jobName,"
			+ "serialNub,"
			+ "pathName,"
			+ "`primary`,"
			+ "`type`,"
			+ "outputType,"
			+ "outputKey,"
			+ "mustHaveResult,"
			+ "`describe`";
	
	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		List<ExtractItem> extractItems = (List<ExtractItem>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String values="(#{list["+INDEX_FLAG+"].jobName},"
				+ "#{list["+INDEX_FLAG+"].serialNub},"
				+ "#{list["+INDEX_FLAG+"].pathName},"
				+ "#{list["+INDEX_FLAG+"].primary},"
				+ "#{list["+INDEX_FLAG+"].type},"
				+ "#{list["+INDEX_FLAG+"].outputType},"
				+ "#{list["+INDEX_FLAG+"].outputKey},"
				+ "#{list["+INDEX_FLAG+"].mustHaveResult},"
				+ "#{list["+INDEX_FLAG+"].describe})";
		StringBuilder sbd = new StringBuilder();  
		sbd.append("insert into ").append(ExtractItemDao.TABLE_NAME);  
		sbd.append("(").append(saveColumns).append(") ");  
		sbd.append("values");  
		sbd.append(setBatchSaveSql(values,extractItems));
		return sbd.toString();
	}
}
