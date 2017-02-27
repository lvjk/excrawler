package six.com.crawler.common.dao.provider;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import six.com.crawler.common.dao.BaseDao;
import six.com.crawler.common.dao.ExtractItemDao;
import six.com.crawler.common.dao.ExtractPathDao;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 上午10:47:56
 */
public class ExtractPathDaoProvider extends BaseProvider{
	private String columns="`name`,"
			+ "sitecode,"
			+ "ranking,"
			+ "`path`,"
			+ "filterPath,"
			+ "extractAttName,"
			+ "appendHead,"
			+ "appendEnd,"			
			+ "compareAttName,"
			+ "containKeyWord,"
			+ "replaceWord,"
			+ "replaceValue,"
			+ "extractEmptyCount,"
			+ "`describe`";
	public String query(Map<String,Object> param) {
		SQL sql = new SQL();
		sql.SELECT(columns);
		sql.FROM(ExtractPathDao.TABLE_NAME);
		sql.WHERE("`name`=#{pathName}");
		sql.AND().WHERE("siteCode=#{siteCode}");
		sql.ORDER_BY("ranking asc");
		return sql.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String batchSave(Map<String, Object> map) {
		List<ExtractPath> extractPaths = (List<ExtractPath>) map.get(BaseDao.BATCH_SAVE_PARAM);
		String values="(#{list["+INDEX_FLAG+"].name},"
				+ "#{list["+INDEX_FLAG+"].sitecode},"
				+ "#{list["+INDEX_FLAG+"].ranking},"
				+ "#{list["+INDEX_FLAG+"].path},"
				+ "#{list["+INDEX_FLAG+"].filterPath},"
				+ "#{list["+INDEX_FLAG+"].extractAttName},"
				+ "#{list["+INDEX_FLAG+"].appendHead},"
				+ "#{list["+INDEX_FLAG+"].appendEnd},"
				+ "#{list["+INDEX_FLAG+"].compareAttName},"
				+ "#{list["+INDEX_FLAG+"].containKeyWord},"
				+ "#{list["+INDEX_FLAG+"].replaceWord},"
				+ "#{list["+INDEX_FLAG+"].replaceValue},"
				+ "#{list["+INDEX_FLAG+"].extractEmptyCount},"
				+ "#{list["+INDEX_FLAG+"].extractEmptyCount}";
		StringBuilder sbd = new StringBuilder();  
		sbd.append("insert into ").append(ExtractItemDao.TABLE_NAME);  
		sbd.append("(").append(columns).append(") ");  
		sbd.append("values");  
		sbd.append(setBatchSaveSql(values,extractPaths));
		return sbd.toString();
	}
}
